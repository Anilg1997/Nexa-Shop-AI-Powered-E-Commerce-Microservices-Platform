variable "aws_region" {
  type    = string
  default = "ap-south-1"
}

variable "ami_id" {
  type        = string
  description = "Use a current Amazon Linux AMI ID for your region."
}

variable "instance_type" {
  type    = string
  default = "t3.micro"
}

variable "key_name" {
  type        = string
  description = "Existing EC2 key pair name."
}

variable "admin_cidr" {
  type        = string
  description = "Your IP CIDR for SSH, for example 203.0.113.10/32."
}
