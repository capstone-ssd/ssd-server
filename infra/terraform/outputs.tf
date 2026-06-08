output "app_public_ip" {
  description = "Elastic IP attached to the application instance"
  value       = aws_eip.app.public_ip
}

output "app_public_dns" {
  description = "Public DNS name of the application instance"
  value       = aws_instance.app.public_dns
}

output "ssh_command" {
  description = "SSH command for the application instance"
  value       = "ssh ubuntu@${aws_eip.app.public_ip}"
}

output "ecr_repository_url" {
  description = "ECR repository URL for the application image"
  value       = aws_ecr_repository.app.repository_url
}

output "s3_bucket_name" {
  description = "S3 bucket name for application assets"
  value       = aws_s3_bucket.assets.bucket
}

output "db_endpoint" {
  description = "RDS PostgreSQL endpoint"
  value       = aws_db_instance.postgres.address
}

output "db_name" {
  description = "RDS PostgreSQL database name"
  value       = aws_db_instance.postgres.db_name
}

output "db_username" {
  description = "RDS PostgreSQL username"
  value       = aws_db_instance.postgres.username
}

output "db_password" {
  description = "RDS PostgreSQL password"
  value       = local.resolved_db_password
  sensitive   = true
}

output "aws_region" {
  description = "AWS region used by Terraform"
  value       = var.aws_region
}

output "external_ai_base_url" {
  description = "External AI base URL"
  value       = var.external_ai_base_url
}
