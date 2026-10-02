provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "linguanest"
      Environment = var.environment
      ManagedBy   = "terraform"
    }
  }
}
