# Terraform bootstrap for SSD dev server

## What it creates
- VPC with two public subnets
- EC2 application host (`t3.micro`) with Elastic IP
- RDS PostgreSQL
- S3 bucket for assets
- ECR repository for the Spring Boot image

## Usage
```bash
export AWS_PROFILE=capstone-ssd
cd infra/terraform
cp terraform.tfvars.example terraform.tfvars
terraform init
terraform plan
terraform apply
```

## Next manual steps
1. Build and push the Docker image to the output ECR repository URL.
2. SSH into the EC2 instance using the output Elastic IP.
3. Upload `deploy/ec2` assets and create `/opt/ssd/config/application-dev.yml`.
4. Run `install_infra.sh` and `blue_green_deploy.sh`.
