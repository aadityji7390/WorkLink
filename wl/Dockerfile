FROM eclipse-temurin:17-jdk
WORKDIR /app/backend
COPY backend/ /app/backend/
COPY frontend/ /app/frontend/
RUN javac *.java
EXPOSE 10000
CMD ["java", "Server"]
