output "public_ip" {
  value = aws_instance.demo.public_ip
}

output "gateway_url" {
  value = "http://${aws_instance.demo.public_ip}:8080"
}
