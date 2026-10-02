# Picks the first N "available" AZs in the region rather than hardcoding
# AZ names, so this config isn't tied to one specific region's AZ naming.
data "aws_availability_zones" "available" {
  state = "available"
}

locals {
  azs = slice(data.aws_availability_zones.available.names, 0, var.availability_zone_count)
}

module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "~> 5.0"

  name = "${var.cluster_name}-vpc"
  cidr = var.vpc_cidr

  azs = local.azs
  # One /24 private + one /24 public subnet per AZ - small VPC, plenty of
  # room for Phase 1 (public subnets only host the ALB; EKS nodes and RDS
  # sit in private subnets).
  private_subnets = [for i, az in local.azs : cidrsubnet(var.vpc_cidr, 8, i)]
  public_subnets  = [for i, az in local.azs : cidrsubnet(var.vpc_cidr, 8, i + 100)]

  # NAT gateways bill hourly plus per-GB data processing continuously,
  # regardless of traffic - one of the pricier always-on pieces of a VPC.
  # A single shared NAT (not one per AZ) is the cost-efficient choice for
  # Phase 1, at the cost of private-subnet egress having a single point of
  # failure - acceptable given the single-AZ node group anyway.
  enable_nat_gateway     = true
  single_nat_gateway     = true
  one_nat_gateway_per_az = false

  # No explicit enable_dns_hostnames/enable_dns_support here - both already
  # default to true in this module version; setting them again changes
  # nothing and just invites a future reader to go check the module source.

  # Required so EKS and the AWS Load Balancer Controller can auto-discover
  # which subnets to place internal vs. internet-facing ELBs/ALBs into.
  public_subnet_tags = {
    "kubernetes.io/role/elb"                    = "1"
    "kubernetes.io/cluster/${var.cluster_name}" = "shared"
  }
  private_subnet_tags = {
    "kubernetes.io/role/internal-elb"           = "1"
    "kubernetes.io/cluster/${var.cluster_name}" = "shared"
  }
}
