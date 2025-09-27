# ===== build =====
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x mvnw
RUN ./mvnw -DskipTests package

# ===== run =====
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENV PORT=8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
# Render injeta $PORT; aqui a app escuta nele
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar --server.port=${PORT}"]
