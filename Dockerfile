# Dockerfile for deploying Campus Academic Systems to Render / Railway / Back4App / Cloud
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app
COPY src/ ./src/
RUN mkdir -p bin && javac -d bin $(find src -name "*.java")

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/bin ./bin
COPY web/ ./web/
COPY index.html ./index.html
RUN mkdir -p data

EXPOSE 8080 7860
ENV PORT=8080

CMD ["java", "-cp", "bin", "com.campus.main.WebLauncher"]
