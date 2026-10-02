# Terraform state bootstrap

## What this is

A tiny, standalone Terraform config that creates exactly two things:

- An **S3 bucket** to hold Terraform remote state.
- A **DynamoDB table** to hold Terraform state locks.

That's it. It does not create any application infrastructure (no VPC, no
EKS, no RDS) -- that's the job of the main Terraform config elsewhere in
`infra/terraform/` (not this directory), which is tracked separately.

## Why this is a separate config

The main config is set up to store *its own* state remotely, in an S3
bucket with DynamoDB locking (see `aws-infrastructure-spec.md`, "Infrastructure
as Code"). But that creates a chicken-and-egg problem: the main config can't
store its state in a bucket that doesn't exist yet, and the very first
`terraform apply` that would create that bucket has nowhere to put its own
state, because the bucket it's trying to create isn't there yet either.

This bootstrap config breaks that cycle. It's deliberately simple, provisions
only the two resources above, and keeps **its own state local** (no
`backend` block -- Terraform's default). Once it's been applied, the bucket
and table it created become the remote backend that the main config points
its `backend "s3" {}` block at.

## How to run it

This is meant to be run **once, locally, by hand** -- not from CI, not
repeatedly.

```
cd infra/terraform/bootstrap
terraform init
terraform apply
```

Review the plan before confirming. After it applies, read the outputs:

```
terraform output
```

Copy `state_bucket_name`, `lock_table_name`, and `aws_region` into the main
config's `backend "s3" {}` block. Terraform does not allow variables or
interpolation inside a `backend` block, so this copy-paste step is a hard
Terraform limitation, not an oversight -- there's no way to wire these
values in automatically. If the main config uses partial backend
configuration (`-backend-config=...`), these are the same values, just
passed as flags/files instead of hardcoded in the block.

Optional: override the default names/region with `-var`, e.g.:

```
terraform apply -var="aws_region=us-west-2" -var="state_bucket_base_name=my-prefix-terraform-state"
```

## Don't lose this state file

This config's `terraform.tfstate` is **committed to this repo as a
deliberate, documented exception** to the usual "never commit `.tfstate`"
rule (see the `.gitignore` in this directory, which explicitly does *not*
ignore it).

Reasoning: this state only tracks an S3 bucket and a DynamoDB table -- their
IDs, ARNs, and configuration. It contains no secrets and no application
data. The risk of losing it (stranding real AWS resources that Terraform no
longer knows about, forcing a manual `terraform import` to recover) is more
dangerous for a solo project than the minor downside of a state file living
in git. A single local machine is also a single point of failure -- if it's
never backed up anywhere else, a disk failure or lost laptop loses this
state permanently along with it. Committing it piggybacks on git's existing
history/remote for durability, which is simpler than remembering to
maintain a separate backup copy.

If you'd rather not commit it, the alternative is to keep a deliberate
backup of `terraform.tfstate` somewhere outside this machine (e.g. a
password manager's secure notes, a private cloud drive) and update it after
every apply -- but nothing enforces that happening, which is why committing
is the recommended default here.

Either way: **do not** delete or hand-edit this state file, and do not run
`terraform apply` against it from more than one machine at a time without
first pulling the latest committed state.

## Never run this from CI

This config is intentionally outside the GitHub Actions `terraform.yaml`
workflow that applies the main config. It should never be invoked by any
CI/CD pipeline:

- It uses local state, which doesn't make sense on an ephemeral CI runner.
- It only needs to run once (or rarely, e.g. if the state bucket/lock table
  ever need to be recreated) -- not on every push.
- Running it automatically risks a CI job silently recreating or modifying
  the very backend the main config depends on.

If the state bucket or lock table are ever genuinely lost and need to be
recreated, that's a deliberate, human-run exception, done the same way as
the original apply -- not something to automate.
