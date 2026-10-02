module "eks" {
  source  = "terraform-aws-modules/eks/aws"
  version = "~> 20.0"

  cluster_name    = var.cluster_name
  cluster_version = var.kubernetes_version

  vpc_id     = module.vpc.vpc_id
  subnet_ids = module.vpc.private_subnets

  # module.vpc.private_subnets spans var.availability_zone_count (>= 2) AZs
  # to satisfy EKS's control-plane requirement - see the variable's comment.
  # The node group below still only schedules into one of them; multi-AZ
  # *node* distribution is explicitly a Phase 2 concern, not needed here.

  # Public (not restricted to a CIDR list) because the pipeline that will
  # eventually run `kubectl apply` is a GitHub-hosted Actions runner, not
  # inside this VPC, with no stable IP range to restrict access to - unlike
  # the RDS security group below, there's no fixed source to allow-list here.
  # Revisit if a self-hosted runner inside the VPC (or a VPN/peering setup)
  # ever replaces GitHub-hosted runners for the kubectl step.
  cluster_endpoint_public_access = true

  # Creates the cluster's OIDC identity provider and is required for any
  # IRSA role (e.g. the LB controller role below) to trust this cluster's
  # service accounts.
  enable_irsa = true

  # Without this, NO IAM principal has Kubernetes RBAC access to the
  # cluster this creates - not even the OIDC role this same pipeline
  # authenticates as, since v20 of this module defaults that to false. Grants
  # whichever principal actually runs `terraform apply` (our OIDC role)
  # cluster-admin access, so the deploy pipeline's own `kubectl apply` TODO
  # and manually Helm-installing the AWS Load Balancer Controller both have
  # somewhere to authenticate against once attempted.
  enable_cluster_creator_admin_permissions = true

  eks_managed_node_groups = {
    default = {
      instance_types = [var.node_instance_type]

      min_size     = var.node_group_min_size
      max_size     = var.node_group_max_size
      desired_size = var.node_group_desired_size

      # Pinned to a single private subnet (one AZ) on purpose - see the
      # comment above and the Phase 1 spec's "one default managed node
      # group" / no multi-AZ node spread note. Keeps node count (and cost)
      # minimal instead of EKS defaulting to one node per subnet.
      subnet_ids = [module.vpc.private_subnets[0]]
    }
  }

  # No explicit tags block here - the aws provider's default_tags (provider.tf)
  # already applies Environment (and Project/ManagedBy) to every resource
  # this config creates, including this cluster.
}

# IRSA role for the AWS Load Balancer Controller (installed into the cluster
# separately, via Helm - not Terraform's job). This submodule has first-
# class support for the controller's exact IAM policy/trust relationship,
# rather than hand-writing the OIDC trust policy JSON ourselves. As of the
# eks module's v20 line, this IRSA helper lives in the separate
# terraform-aws-modules/iam module, not under modules/eks/aws anymore.
module "lb_controller_irsa" {
  source  = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version = "~> 5.39"

  role_name = "${var.cluster_name}-lb-controller"

  attach_load_balancer_controller_policy = true

  oidc_providers = {
    main = {
      provider_arn = module.eks.oidc_provider_arn
      # Matches the service account name/namespace the AWS Load Balancer
      # Controller's Helm chart creates by default.
      namespace_service_accounts = ["kube-system:aws-load-balancer-controller"]
    }
  }
}
