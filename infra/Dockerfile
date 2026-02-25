# =============================================================================
# VP-UPLOAD — Dockerfile
# Multi-stage build: Java 21 + FFmpeg
# Build context: raiz do projeto (../  a partir de infra/)
# =============================================================================

# -----------------------------------------------------------------------------
# Stage 1: Build
# -----------------------------------------------------------------------------
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copia apenas os arquivos de dependências primeiro (cache de layer otimizado)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x ./mvnw \
    && ./mvnw dependency:go-offline -B --no-transfer-progress

# Copia o código-fonte e compila
COPY src/ ./src/

RUN ./mvnw package -DskipTests -B --no-transfer-progress

# -----------------------------------------------------------------------------
# Stage 2: Runtime — JRE slim + FFmpeg
# -----------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runtime

LABEL maintainer="FIAP Hackathon <fiap@fiap.com.br>"
LABEL app="vp-upload"
LABEL version="0.0.1-SNAPSHOT"

# Instala FFmpeg e dependências mínimas
RUN apk add --no-cache \
        ffmpeg \
        curl \
    && rm -rf /var/cache/apk/*

# Usuário não-root para segurança
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copia o JAR gerado no stage de build
COPY --from=builder /build/target/vp-upload-*.jar app.jar

RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

# Healthcheck usando a rota de UI (sem actuator)
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=5 \
    CMD curl -sf http://localhost:8080/upload || exit 1

# Flags JVM otimizadas para container
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+UseG1GC", \
    "-XX:+ExitOnOutOfMemoryError", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
