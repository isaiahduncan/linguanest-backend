output "vpc_id" {
  value = module.vpc.vpc_id
}

output "private_subnet_ids" {
  value = module.vpc.private_subnets
}

output "public_subnet_ids" {
  value = module.vpc.public_subnets
}

output "cluster_name" {
  value = module.eks.cluster_name
}

output "cluster_endpoint" {
  description = "EKS API server endpoint - used by `aws eks update-kubeconfig` in deploy.yaml."
  value       = module.eks.cluster_endpoint
}

output "cluster_certificate_authority_data" {
  value     = module.eks.cluster_certificate_authority_data
  sensitive = true
}

output "node_security_group_id" {
  value = module.eks.node_security_group_id
}

output "lb_controller_irsa_role_arn" {
  description = "IAM role ARN to annotate the aws-load-balancer-controller k8s ServiceAccount with (eks.amazonaws.com/role-arn), once the Helm chart is installed."
  value       = module.lb_controller_irsa.iam_role_arn
}

output "rds_endpoint" {
  description = "RDS connection endpoint (host:port), for the k8s Secret the Deployment reads DB connection details from."
  value       = module.rds.db_instance_endpoint
}

output "rds_address" {
  value = module.rds.db_instance_address
}

output "rds_database_name" {
  value = var.db_name
}

output "rds_username" {
  value = var.db_username
}

output "rds_password" {
  description = "Master password for the RDS instance - read this by hand (`terraform output -raw rds_password`) to populate the k8s Secret. Never logged or committed."
  value       = random_password.rds_master.result
  sensitive   = true
}
