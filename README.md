# Gatherfy

### 🚀 Tech Stack

**Language**: Kotlin  
**Framework**: Spring Boot  
**Database**: MySQL  
**Build Tool**: Gradle  

### 🛠️ Installation & Setup
**1. Clone the repository**  
```
git clone https://github.com/SomSanTech/gatherfy-back.git  
cd gatherfy-back
```

**2. Update application properties**  
> _src/main/resources/application-prod.properties_  
```
spring.datasource.url=jdbc:mysql://localhost:3306/your_database  
spring.datasource.username=your_username  
spring.datasource.password=your_password
```

**3. Run the application**  
```./gradlew bootRun```  
*or*  
```./mvnw spring-boot:run```

**📚 API Documentation**  
The application runs on http://localhost:8080

**Access API documentation at:**  
http://localhost:8080/swagger-ui/index.html#/
