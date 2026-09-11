# Dockerfile for deploying Campus Academic Systems to Render / Railway / Fly.io / Cloud
FROM openjdk:17-jdk-slim AS builder
WORKDIR /app
COPY src/ ./src/
RUN mkdir -p bin && javac -d bin $(find src -name "*.java")

FROM openjdk:17-jre-slim
WORKDIR /app
COPY --from=builder /app/bin ./bin
COPY web/ ./web/
COPY index.html ./index.html
RUN mkdir -p data

EXPOSE 8080
ENV PORT=8080

CMD ["java", "-cp", "bin", "com.campus.main.WebLauncher"]
