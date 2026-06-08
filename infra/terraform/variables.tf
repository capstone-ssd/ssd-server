variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "ap-northeast-2"
}

variable "project" {
  description = "Project name prefix"
  type        = string
  default     = "ssd"
}

variable "environment" {
  description = "Deployment environment"
  type        = string
  default     = "dev"
}

variable "vpc_cidr" {
  description = "VPC CIDR block"
  type        = string
  default     = "10.20.0.0/16"
}

variable "ssh_allowed_cidrs" {
  description = "CIDRs allowed to SSH into the EC2 instance"
  type        = list(string)
  default     = ["118.221.254.20/32"]
}

variable "ssh_public_key_path" {
  description = "Path to the local SSH public key used for EC2 access"
  type        = string
  default     = "~/.ssh/id_rsa.pub"
}

variable "instance_type" {
  description = "EC2 instance type"
  type        = string
  default     = "t3.small"
}

variable "db_instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.t3.micro"
}

variable "db_name" {
  description = "Initial PostgreSQL database name"
  type        = string
  default     = "ssd"
}

variable "db_username" {
  description = "PostgreSQL master username"
  type        = string
  default     = "ssd_admin"
}

variable "db_password" {
  description = "PostgreSQL master password. Leave blank to auto-generate."
  type        = string
  sensitive   = true
  default     = ""
}

variable "ecr_repository_name" {
  description = "ECR repository name"
  type        = string
  default     = "ssd-api"
}

variable "external_ai_base_url" {
  description = "External AI service base URL"
  type        = string
  default     = "http://166.104.223.33:8080"
}

variable "kakao_client_id" {
  description = "Kakao OAuth client id"
  type        = string
  default     = "b93385a5496e64dd00060400787950fb"
}

variable "jwt_secret" {
  description = "JWT signing secret"
  type        = string
  sensitive   = true
  default     = "sdvjnfdjnkdvsfjvnjvdksfdljnkvfljovnkf"
}

variable "discord_webhook_url" {
  description = "Discord webhook URL"
  type        = string
  sensitive   = true
  default     = "https://discord.com/api/webhooks/1481239816069971988/qgcacvS-zm6gdbwCbK4aLpt_hsm34UEnL_pQZQrwa-Vd4Bez3JLpeg1dmFytO4MH5P51"
}

variable "sentry_dsn" {
  description = "Sentry DSN"
  type        = string
  sensitive   = true
  default     = "https://46b8f558adc9be8be498848c3ca1cf7b@o4508596177076224.ingest.de.sentry.io/4511025301684304"
}

variable "app_allowed_origins" {
  description = "Allowed frontend origins"
  type        = list(string)
  default = [
    "http://localhost:5173",
    "http://localhost:8080",
    "https://dev-api.simsaimdang.shop",
    "https://dev.simsaimdang.shop",
    "https://ssd-client-zfl2.vercel.app"
  ]
}
