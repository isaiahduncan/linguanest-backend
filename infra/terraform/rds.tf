# Generated rather than passed in as a variable - avoids needing yet another
# GitHub Actions secret on top of the AWS OIDC role. The k8s Secret holding
# this (per the spec, a plain Secret filled in by hand for the Phase 1 POC)
# gets populated from `terraform output -raw rds_password` after apply; it
# only lives in Terraform state (in S3, same as everything else here) and
# this output, never committed to git.
#
# This module supports `manage_master_user_password = true` instead - an
# AWS-managed, auto-rotated Secrets Manager secret, never exposed as a
# plaintext Terraform output at all. Deliberately not used yet: the spec's
# Kubernetes Setup section explicitly calls a plain K8s Secret "sufficient
# for the POC" and names Secrets Manager + the Secrets Store CSI driver as a
# later upgrade, not a Phase 1 requirement - switching now would need that
# CSI driver wired up before the app could read the password at all, which
# is exactly the work the spec defers. Revisit together when that happens.
resource "random_password" "rds_master" {
  length  = 20
  special = false
}

# Postgres, inbound only from the EKS node security group - not open to the
# internet, not open broadly within the VPC.
resource "aws_security_group" "rds" {
  name_prefix = "${var.cluster_name}-${var.environment}-rds-"
  description = "Allows inbound Postgres only from the EKS node security group."
  vpc_id      = module.vpc.vpc_id

  ingress {
    description     = "Postgres from EKS nodes"
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [module.eks.node_security_group_id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  lifecycle {
    create_before_destroy = true
  }
}

module "rds" {
  source  = "terraform-aws-modules/rds/aws"
  version = "~> 6.0"

  # Includes var.environment so dev/prod applies (separate state, separate
  # `-var environment=...`) don't collide on identifier - see the Database >
  # Environment separation section of the spec: a dedicated small instance
  # per environment, not one shared instance with separate schemas.
  identifier = "${var.cluster_name}-${var.environment}"

  engine               = "postgres"
  engine_version       = var.db_engine_version
  family               = "postgres${var.db_engine_version}"
  major_engine_version = var.db_engine_version
  instance_class       = var.db_instance_class

  allocated_storage = var.db_allocated_storage
  storage_encrypted = true

  db_name  = var.db_name
  username = var.db_username
  port     = 5432

  password                    = random_password.rds_master.result
  manage_master_user_password = false

  vpc_security_group_ids = [aws_security_group.rds.id]
  create_db_subnet_group = true
  subnet_ids             = module.vpc.private_subnets

  multi_az = false

  # Cost-efficiency priority for Phase 1 - no Multi-AZ standby, minimal
  # backup retention, no final snapshot on destroy (a throwaway dev/POC
  # instance isn't worth the snapshot cost/clutter). Revisit before any
  # real prod data is ever at risk.
  backup_retention_period = 1
  deletion_protection     = false
  skip_final_snapshot     = true

  # pgvector (Phase 2) needs no RDS-level change here - it's a CREATE
  # EXTENSION run against this same instance once that work starts, not a
  # different parameter group/engine choice.
}
