variable "aws_region" {
  description = "AWS region to create the Terraform state bucket and lock table in. Must match the region the main config's backend \"s3\" block points at."
  type        = string
  default     = "us-east-1"
}

variable "state_bucket_base_name" {
  description = <<-EOT
    Base name for the Terraform remote state S3 bucket. A random hex suffix
    is appended automatically (see random_id.state_bucket_suffix in main.tf)
    because S3 bucket names must be globally unique across ALL AWS accounts,
    not just this one -- a plain name like "linguanest-terraform-state" on
    its own would risk colliding with some other AWS customer's bucket and
    failing on `terraform apply`.
  EOT
  type        = string
  default     = "linguanest-terraform-state"
}

variable "lock_table_name" {
  description = <<-EOT
    Name of the DynamoDB table used for Terraform state locking. Unlike S3
    bucket names, DynamoDB table names only need to be unique within this
    AWS account + region, so no random suffix is needed here.
  EOT
  type        = string
  default     = "linguanest-terraform-locks"
}
