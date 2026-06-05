$products = @(
  @{
    name = "AI Starter Laptop"
    description = "Developer laptop for Java, Angular, Docker, and local AI demos."
    price = 899
    stock = 12
    imageUrl = "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=900&q=80"
  },
  @{
    name = "Kafka Event Kit"
    description = "A learning bundle for event-driven ecommerce workflows."
    price = 149
    stock = 40
    imageUrl = "https://images.unsplash.com/photo-1558494949-ef010cbdcc31?auto=format&fit=crop&w=900&q=80"
  },
  @{
    name = "RAG Knowledge Pack"
    description = "Demo dataset and prompts for retrieval augmented ecommerce assistants."
    price = 79
    stock = 100
    imageUrl = "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=900&q=80"
  }
)

foreach ($product in $products) {
  Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/catalog/products" -ContentType "application/json" -Body ($product | ConvertTo-Json)
}
