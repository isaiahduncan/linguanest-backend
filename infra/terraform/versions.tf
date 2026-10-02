terraform {
  required_version = ">= 1.5"

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

  # Partial backend config on purpose - a `backend "s3" {}` block can't use
  # variables or interpolation, so the actual bucket/key/region/dynamodb_table
  # values are supplied via `-backend-config` flags at `terraform init` time
  # (see .github/workflows/terraform.yaml), not hardcoded here. Those values
  # come from infra/terraform/bootstrap/'s output, applied by hand once.
  backend "s3" {}
}
