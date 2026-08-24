# ---- Build stage ----
    FROM eclipse-temurin:25-jdk AS build
    WORKDIR /workspace

    COPY .mvn .mvn
    COPY mvnw pom.xml ./
    RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

    COPY src ./src
    RUN ./mvnw -B clean package -DskipTests

# ---- Runtime stage ----
    
    FROM eclipse-temurin:25-jre
    RUN addgroup --system app && adduser --system --ingroup app app
    WORKDIR /app
    COPY --from=build /workspace/target/*.jar app.jar
    USER app
    
    # This expose is esssentially a courtesy note
    EXPOSE 8080
    ENTRYPOINT ["java", "-jar", "app.jar"]
