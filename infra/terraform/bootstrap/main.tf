terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.6"
    }
  }

  # Deliberately no backend block. Local state (Terraform's default) is
  # correct here -- see README.md for why. Do NOT add a `backend "s3" {}`
  # block to this config; that would recreate the exact chicken-and-egg
  # problem this bootstrap exists to solve (this config can't store its
  # state in a bucket that doesn't exist yet, because this config is the
  # thing that creates that bucket).
}

provider "aws" {
  region = var.aws_region
}

# S3 bucket names are global across ALL AWS accounts, not just this one.
# A random suffix avoids a naming collision with some other AWS customer's
# bucket on `terraform apply`, without making the user guess a unique name
# by hand.
resource "random_id" "state_bucket_suffix" {
  byte_length = 4
}

resource "aws_s3_bucket" "terraform_state" {
  bucket = "${var.state_bucket_base_name}-${random_id.state_bucket_suffix.hex}"

  tags = {
    Project   = "linguanest"
    Purpose   = "terraform-remote-state"
    ManagedBy = "terraform-bootstrap"
  }
}

# Versioning so a bad/corrupt state write can be recovered by rolling back
# to a previous object version instead of losing state entirely.
resource "aws_s3_bucket_versioning" "terraform_state" {
  bucket = aws_s3_bucket.terraform_state.id

  versioning_configuration {
    status = "Enabled"
  }
}

# SSE-S3 (AES256) is sufficient here -- this bucket holds Terraform state
# (resource IDs/ARNs, not application secrets), so the added complexity of
# a customer-managed KMS key isn't warranted.
resource "aws_s3_bucket_server_side_encryption_configuration" "terraform_state" {
  bucket = aws_s3_bucket.terraform_state.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

# Block all public access -- this bucket should never be reachable from
# outside the AWS account.
resource "aws_s3_bucket_public_access_block" "terraform_state" {
  bucket = aws_s3_bucket.terraform_state.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# Terraform's S3 backend requires this exact schema for state locking: a
# single primary key attribute named "LockID" of type String.
resource "aws_dynamodb_table" "terraform_locks" {
  name = var.lock_table_name

  # On-demand billing, not provisioned capacity: this table sees minimal
  # traffic (one lock/unlock per `terraform apply`), so on-demand avoids
  # paying for idle provisioned throughput.
  billing_mode = "PAY_PER_REQUEST"

  hash_key = "LockID"

  attribute {
    name = "LockID"
    type = "S"
  }

  tags = {
    Project   = "linguanest"
    Purpose   = "terraform-state-locking"
    ManagedBy = "terraform-bootstrap"
  }
}
