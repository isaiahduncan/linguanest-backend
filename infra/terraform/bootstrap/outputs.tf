# These outputs are the values the main ("real") Terraform config needs for
# its own `backend "s3" {}` block. Terraform does not allow variables or
# interpolation inside a `backend` block, so these can't just be wired in
# automatically -- after `terraform apply` here, copy them by hand into the
# main config's backend configuration (or into the `-backend-config` file /
# flags it's set up to read, if it uses partial backend configuration).

output "state_bucket_name" {
  description = "Name of the S3 bucket for Terraform remote state. Use this as the backend's `bucket` value."
  value       = aws_s3_bucket.terraform_state.id
}

output "state_bucket_arn" {
  description = "ARN of the S3 bucket for Terraform remote state (for reference / IAM policies, not needed by the backend block itself)."
  value       = aws_s3_bucket.terraform_state.arn
}

output "lock_table_name" {
  description = "Name of the DynamoDB table for Terraform state locking. Use this as the backend's `dynamodb_table` value."
  value       = aws_dynamodb_table.terraform_locks.name
}

output "aws_region" {
  description = "AWS region these resources were created in. Use this as the backend's `region` value -- it must match."
  value       = var.aws_region
}
