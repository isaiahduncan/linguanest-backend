variable "aws_region" {
  description = "AWS region all resources are provisioned in."
  type        = string
  default     = "us-east-1"
}

variable "environment" {
  # The EKS cluster itself is shared across environments (dev/prod live as
  # separate k8s namespaces inside it - see k8s/overlays/). RDS is the
  # resource that actually gets one instance per environment; this variable
  # exists mainly to name/tag that instance distinctly per apply. Running
  # this config for a second environment means a second `terraform apply`
  # with this variable (and a distinct backend state key) set accordingly -
  # no prod overlay/tfvars exists yet, matching the k8s side's current state.
  description = "Environment name, used to name/tag the per-environment RDS instance (e.g. dev, prod)."
  type        = string
  default     = "dev"
}

variable "cluster_name" {
  description = "Name of the shared EKS cluster."
  type        = string
  default     = "linguanest"
}

variable "kubernetes_version" {
  description = "EKS control plane Kubernetes version."
  type        = string
  default     = "1.31"
}

variable "vpc_cidr" {
  description = "CIDR block for the VPC."
  type        = string
  default     = "10.0.0.0/16"
}

variable "availability_zone_count" {
  # This is an EKS control-plane requirement, not a style choice - cluster
  # creation fails without subnets in at least 2 AZs. The node group itself
  # (see node_subnet below) still only schedules into one AZ - multi-AZ node
  # distribution is a deliberate Phase 2 concern per the spec.
  description = "Number of AZs the VPC's subnets span. EKS requires at least 2."
  type        = number
  default     = 2

  validation {
    condition     = var.availability_zone_count >= 2
    error_message = "EKS requires subnets across at least 2 Availability Zones."
  }
}

variable "node_instance_type" {
  description = "Instance type for the single Phase 1 managed node group. Kept small/cheap by default per the cost-efficiency target."
  type        = string
  default     = "t3.medium"
}

variable "node_group_min_size" {
  type    = number
  default = 1
}

variable "node_group_max_size" {
  type    = number
  default = 3
}

variable "node_group_desired_size" {
  # 2 nodes so the Deployment's 2 pod replicas (k8s/base/deployment.yaml)
  # aren't both forced onto a single node - still modest/cheap for Phase 1.
  type    = number
  default = 2
}

variable "db_engine_version" {
  description = "PostgreSQL major version for RDS."
  type        = string
  default     = "16"
}

variable "db_instance_class" {
  # db.t4g.micro is the smallest Graviton (ARM) burstable class - cheapest
  # sensible default for Phase 1's single small instance per environment.
  description = "RDS instance class."
  type        = string
  default     = "db.t4g.micro"
}

variable "db_allocated_storage" {
  description = "Allocated storage for the RDS instance, in GiB."
  type        = number
  default     = 20
}

variable "db_name" {
  description = "Initial database name created on the RDS instance."
  type        = string
  default     = "linguanest"
}

variable "db_username" {
  description = "Master username for the RDS instance."
  type        = string
  default     = "linguanest"
}
