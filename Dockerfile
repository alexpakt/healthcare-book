FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy pom.xml and download dependencies (cacheable layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:17-jre

WORKDIR /app

# Run as non-root user for security best practices
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

# Copy built artifact from builder stage
COPY --from=builder /build/target/child-healthcare-records-*.jar app.jar

EXPOSE 8080

# Essential for Render: Memory limits (-Xmx384m) fit Spring Boot inside 512MB RAM
ENTRYPOINT ["java", "-Xmx384m", "-Xms128m", "-jar", "app.jar"]
