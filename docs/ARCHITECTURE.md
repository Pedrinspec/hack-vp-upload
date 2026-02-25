# VP-Upload — Documentação de Arquitetura

## Visão Geral

**VP-Upload** é um microsserviço responsável pelo **upload fragmentado (multipart)** de arquivos de vídeo para Amazon S3. Após a conclusão do upload, publica um evento no Apache Kafka para notificar o serviço downstream de processamento de vídeo.

| Atributo        | Valor                          |
|----------------|-------------------------------|
| Linguagem       | Java 21                        |
| Framework       | Spring Boot 4.0.2              |
| Arquitetura     | Hexagonal (Ports & Adapters)   |
| Banco de Dados  | MySQL 8.0                      |
| Cache           | Redis 7                        |
| Mensageria      | Apache Kafka (latest)          |
| Storage         | Amazon S3                      |
| Documentação    | SpringDoc OpenAPI (Swagger)    |

---

## Diagrama de Arquitetura

```
┌──────────────────────────────────────────────────────────────────────────┐
│                         VP-UPLOAD SERVICE                                │
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────────┐  │
│  │                   ADAPTER LAYER — INPUT                            │  │
│  │                                                                    │  │
│  │   ┌──────────────────────────────┐   ┌────────────────────────┐   │  │
│  │   │       UploadController       │   │    ViewController      │   │  │
│  │   │   POST /api/upload/start     │   │    GET /upload         │   │  │
│  │   │   POST /api/upload/:id/part  │   │    (HTML Thymeleaf)    │   │  │
│  │   │   POST /api/upload/:id/compl │   └────────────────────────┘   │  │
│  │   └──────────────┬───────────────┘                                │  │
│  └──────────────────┼───────────────────────────────────────────────-│  │
│                     │                                                    │
│  ┌──────────────────▼──────────────────────────────────────────────┐    │
│  │                  APPLICATION LAYER (Use Cases)                   │    │
│  │                                                                  │    │
│  │              ┌─────────────────────────────┐                     │    │
│  │              │      uploadInputPort         │                     │    │
│  │              │  (implements uploadUseCase)  │                     │    │
│  │              └──────────┬──────────────────┘                     │    │
│  └─────────────────────────┼──────────────────────────────────────-─│    │
│                            │                                              │
│  ┌─────────────────────────▼──────────────────────────────────────┐      │
│  │                      DOMAIN LAYER                               │      │
│  │                                                                  │      │
│  │   ┌───────────────────────┐   ┌──────────────────────────────┐  │      │
│  │   │    UploadService      │   │     UploadPartService        │  │      │
│  │   │  - startUpload()      │   │  - confirmPartUpload()       │  │      │
│  │   │  - completeUpload()   │   │  - findByUploadId()          │  │      │
│  │   └──────────┬────────────┘   └────────────┬─────────────────┘  │      │
│  └─────────────-┼────────────────────────────-┼───────────────────-│      │
│                 │   OUTPUT PORTS               │                          │
│  ┌──────────────▼─────────────────────────────▼──────────────────┐       │
│  │                 ADAPTER LAYER — OUTPUT                          │       │
│  │                                                                  │       │
│  │  ┌───────────┐ ┌────────────┐ ┌─────────────┐ ┌─────────────┐  │       │
│  │  │S3Upload   │ │KafkaMessage│ │UploadData   │ │UploadCache  │  │       │
│  │  │Adapter    │ │OutputImpl  │ │OutputImpl   │ │OutputImpl   │  │       │
│  │  └─────┬─────┘ └─────┬──────┘ └──────┬──────┘ └──────┬──────┘  │       │
│  └────────┼─────────────┼───────────────┼────────────────┼────────-│       │
└───────────┼─────────────┼───────────────┼────────────────┼─────────┘
            │             │               │                │
            ▼             ▼               ▼                ▼
      ┌──────────┐  ┌───────────┐  ┌──────────┐   ┌──────────────┐
      │  AWS S3  │  │   Kafka   │  │  MySQL   │   │    Redis     │
      │(Vídeos)  │  │ (Eventos) │  │  (Data)  │   │   (Cache)    │
      └──────────┘  └───────────┘  └──────────┘   └──────────────┘
```

---

## Fluxo de Upload (Sequência)

```
Cliente           vp-upload           AWS S3          Kafka
   │                  │                  │               │
   │── POST /start ──►│                  │               │
   │                  │── CreateMultipart►│               │
   │                  │◄── uploadId ─────│               │
   │                  │── GeneratePresigned x N ─────────│
   │◄── {uploadId,    │                  │               │
   │     presignedUrls}│                 │               │
   │                  │                  │               │
   │──── PUT chunk1 ──────────────────►  │               │
   │◄─── ETag1 ───────────────────────── │               │
   │── POST /part/confirm (partNum, eTag)►│               │
   │◄── 202 Accepted ─│                  │               │
   │                  │                  │               │
   │   (repete para cada chunk)          │               │
   │                  │                  │               │
   │── POST /complete ►│                 │               │
   │                  │── CompleteMultipart ────────────►│
   │                  │◄── Confirmação ──│               │
   │                  │─────── ProcessRequest ──────────►│
   │◄── 202 Accepted ─│                  │               │
```

---

## Padrão Arquitetural: Hexagonal (Ports & Adapters)

O projeto implementa a **Arquitetura Hexagonal** (também chamada de Ports & Adapters), garantindo que o domínio de negócio seja independente de frameworks e tecnologias externas.

### Camadas

| Camada | Pacote | Responsabilidade |
|--------|--------|-----------------|
| **Domain** | `com.fiap.vp_upload.domain` | Regras de negócio, entidades, exceções, serviços |
| **Application** | `com.fiap.vp_upload.application` | Orquestração dos casos de uso (Use Cases) |
| **Infra/Input** | `com.fiap.vp_upload.infra.adapter.input` | Controllers REST, DTOs de entrada |
| **Infra/Output** | `com.fiap.vp_upload.infra.adapter.output` | Integrações externas: S3, Kafka, MySQL, Redis |
| **Infra/Config** | `com.fiap.vp_upload.infra.config` | Configuração de beans Spring (S3, Redis, Kafka, Gson) |

### Ports (Interfaces de saída)

| Interface | Adaptador Implementador | Tecnologia |
|-----------|------------------------|------------|
| `S3UploadOutput` | `S3UploadAdapter` | AWS SDK v2 S3 |
| `MessageOutput` | `KafkaMessageOutputImpl` | Spring Kafka |
| `UploadDataOutput` | `UploadDataOutputImpl` | Spring Data JPA + MySQL |
| `UploadCacheOutput` | `UploadCacheOutputImpl` | Spring Data Redis |
| `UploadPartCacheOutput` | `UploadPartCacheOutputImpl` | Spring Data Redis |

---

## Estrutura de Diretórios

```
hack-vp-upload/
├── infra/
│   ├── Dockerfile              # Build multi-stage (Java 21 + FFmpeg)
│   ├── docker-compose.yml      # Orquestração completa de serviços
│   └── .env.example            # Template de variáveis de ambiente
├── docs/
│   ├── ARCHITECTURE.md         # Este documento
│   └── API.md                  # Documentação das APIs (estilo Swagger)
├── src/
│   └── main/
│       ├── java/com/fiap/vp_upload/
│       │   ├── VpUploadApplication.java
│       │   ├── application/
│       │   │   ├── ports/
│       │   │   │   ├── input/
│       │   │   │   │   └── uploadInputPort.java    # Orquestrador de casos de uso
│       │   │   │   └── output/
│       │   │   │       ├── S3UploadOutput.java
│       │   │   │       ├── MessageOutput.java
│       │   │   │       ├── UploadDataOutput.java
│       │   │   │       ├── UploadCacheOutput.java
│       │   │   │       └── UploadPartCacheOutput.java
│       │   │   └── usecase/
│       │   │       └── uploadUseCase.java          # Interface do caso de uso
│       │   ├── domain/
│       │   │   ├── exceptions/                     # Exceções de negócio
│       │   │   │   ├── InvalidFileException.java
│       │   │   │   ├── InvalidPartNumberException.java
│       │   │   │   ├── NoSuchChunkException.java
│       │   │   │   └── UploadNotExistException.java
│       │   │   ├── model/                          # Modelos de domínio
│       │   │   │   ├── Video.java
│       │   │   │   ├── ProcessRequest.java
│       │   │   │   └── FinishUpload.java
│       │   │   └── service/
│       │   │       ├── UploadService.java
│       │   │       ├── UploadPartService.java
│       │   │       └── impl/
│       │   │           ├── UploadServiceImpl.java
│       │   │           └── UploadPartServiceImpl.java
│       │   └── infra/
│       │       ├── adapter/
│       │       │   ├── input/                      # Adaptadores REST
│       │       │   │   ├── UploadController.java
│       │       │   │   ├── ViewController.java
│       │       │   │   ├── dto/request/
│       │       │   │   │   ├── StartUploadRequest.java
│       │       │   │   │   ├── UploadPartConfirmRequest.java
│       │       │   │   │   └── FinishUploadRequest.java
│       │       │   │   ├── dto/response/
│       │       │   │   │   ├── StartUploadResponse.java
│       │       │   │   │   └── CustomErrorResponseDto.java
│       │       │   │   └── mapper/
│       │       │   │       └── FinishUploadMapper.java
│       │       │   └── output/                     # Adaptadores externos
│       │       │       ├── S3UploadAdapter.java
│       │       │       ├── KafkaMessageOutputImpl.java
│       │       │       ├── UploadDataOutputImpl.java
│       │       │       ├── UploadCacheOutputImpl.java
│       │       │       ├── UploadPartCacheOutputImpl.java
│       │       │       └── repository/
│       │       │           ├── UploadRepository.java
│       │       │           ├── UploadRedisRepository.java
│       │       │           ├── UploadPartCacheRepository.java
│       │       │           ├── entities/
│       │       │           │   ├── Upload.java
│       │       │           │   └── UploadPart.java
│       │       │           └── impl/
│       │       │               ├── UploadRedisRepositoryImpl.java
│       │       │               └── UploadPartCacheRepositoryImpl.java
│       │       ├── config/
│       │       │   ├── S3Config.java
│       │       │   ├── RedisConfig.java
│       │       │   ├── KafkaConfig.java
│       │       │   └── GsonConfig.java
│       │       └── exceptions/
│       │           └── ChunkUploadExceptionHandlerController.java
│       └── resources/
│           ├── application.yaml
│           └── templates/
│               └── upload.html                     # UI de upload web
└── pom.xml
```

---

## Estratégia de Cache (Redis)

O Redis é utilizado em duas estruturas:

| Chave | Tipo | Valor | TTL |
|-------|------|-------|-----|
| `vp-upload:{uploadId}` | String | JSON serializado da entidade `Upload` | 48h |
| `vp-upload:{uploadId}:parts` | Hash | `{partNumber} → eTag` | 48h |

**Política Cache-First:** Os serviços consultam o Redis antes do MySQL. Em caso de miss, consultam o banco e repopulam o cache.

---

## Estratégia de Armazenamento S3

- **Chave do objeto (key):** `user/{userId}/upload/{uploadId}/video/{fileName}.{extensão}`
- **Mecanismo:** Multipart Upload nativo do S3
- **URLs Pré-assinadas:** Validade de **10 minutos** por chunk
- **Máximo de chunksimultâneos:** 8 workers paralelos (client-side)
- **Tamanho padrão de chunk:** 16MB

---

## Schema do Banco de Dados

```sql
CREATE TABLE upload (
    upload_id    CHAR(36)     NOT NULL UNIQUE,
    s3_upload_id VARCHAR(255) NOT NULL,
    file_key     VARCHAR(255) NOT NULL,
    user_id      VARCHAR(255) NOT NULL,
    status_id    VARCHAR(50)  NOT NULL,
    PRIMARY KEY (upload_id)
);
```

**Status possíveis:**
- `STARTED` — upload iniciado
- `COMPLETED` — upload finalizado e evento Kafka publicado

---

## Mensageria Kafka

| Propriedade | Valor |
|------------|-------|
| Tópico | `${VIDEO_PROCESSOR_TOPIC}` (padrão: `video-processor-topic`) |
| Partições | 3 |
| Chave da mensagem | `uploadId` (para particionamento) |
| Formato | JSON (via Gson) |

**Payload da mensagem:**
```json
{
  "uploadId": "550e8400-e29b-41d4-a716-446655440000",
  "key": "user/user-123/upload/550e.../video/video.mp4"
}
```

---

## Variáveis de Ambiente

| Variável | Padrão | Obrigatória | Descrição |
|----------|--------|:-----------:|-----------|
| `DATABASE_URL` | `jdbc:mysql://localhost:3306/hackvp` | Não | URL JDBC do MySQL |
| `DATABASE_USER` | `root` | Não | Usuário do banco |
| `DATABASE_PASSWORD` | _(vazio)_ | Sim | Senha do banco |
| `REDIS_HOST` | `localhost` | Não | Host do Redis |
| `REDIS_PORT` | `6379` | Não | Porta do Redis |
| `REDIS_TIMEOUT` | `60000` | Não | Timeout em ms |
| `AWS_REGION` | `sa-east-1` | Não | Região AWS |
| `ACCESS_KEY` | — | **Sim** | AWS Access Key ID |
| `ACCESS_SECRET` | — | **Sim** | AWS Secret Access Key |
| `S3_VIDEO_BUCKET` | `hack-vp-videos` | Não | Nome do bucket S3 |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Não | Servidores Kafka |
| `VIDEO_PROCESSOR_TOPIC` | `video-processor-topic` | Não | Tópico Kafka de saída |

---

## Stack de Tecnologias

| Tecnologia | Versão | Uso |
|-----------|--------|-----|
| Java | 21 | Runtime |
| Spring Boot | 4.0.2 | Framework principal |
| Spring Web MVC | 4.0.2 | Endpoints REST |
| Spring Data JPA | 4.0.2 | Persistência ORM |
| Spring Data Redis | 4.0.2 | Cache |
| Spring Kafka | 4.0.2 | Produção de mensagens |
| Hibernate | (via Spring) | ORM |
| MySQL Connector/J | (via Spring) | Driver JDBC |
| AWS SDK S3 | 2.41.31 | Multipart upload + Presigner |
| Lettuce | (via Spring) | Cliente Redis |
| Gson | (via Spring) | Serialização JSON |
| Lombok | (via Spring) | Redução de boilerplate |
| SpringDoc OpenAPI | 2.3.0 | Documentação Swagger |
| Thymeleaf | (via Spring) | UI de upload |
| FFmpeg | latest | Processamento de vídeo |
| Docker | 24+ | Containerização |

---

## Instruções de Execução

> Consulte o arquivo `docs/API.md` para a documentação detalhada das APIs.

### Pré-requisitos

- Docker Engine 24+
- Docker Compose v2.20+
- Credenciais AWS válidas (Access Key + Secret)
- Bucket S3 criado na região configurada

### 1. Configurar variáveis de ambiente

```bash
cd infra/
cp .env.example .env
# Edite .env com suas credenciais reais
nano .env
```

### 2. Build e inicialização completa

```bash
# A partir da raiz do projeto
docker compose -f infra/docker-compose.yml --env-file infra/.env up -d --build
```

### 3. Verificar status dos serviços

```bash
docker compose -f infra/docker-compose.yml ps
docker compose -f infra/docker-compose.yml logs -f vp-upload
```

### 4. Aguardar inicialização

```bash
# Aguarda o app ficar saudável (pode levar ~90s na primeira execução)
docker compose -f infra/docker-compose.yml wait vp-upload
```

### 5. Acessar os serviços

| Serviço | URL |
|---------|-----|
| API REST | http://localhost:8080/api/upload |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Upload UI | http://localhost:8080/upload |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |

### 6. Parar e remover containers

```bash
# Parar (mantém volumes)
docker compose -f infra/docker-compose.yml down

# Parar e remover volumes (reset completo)
docker compose -f infra/docker-compose.yml down -v
```

### Execução sem Docker (desenvolvimento local)

```bash
# Dependências externas devem estar rodando (MySQL, Redis, Kafka)
export ACCESS_KEY=sua-chave
export ACCESS_SECRET=seu-segredo
./mvnw spring-boot:run
```

---

## Considerações de Segurança

1. **Credenciais AWS:** Usar IAM Role em produção em vez de Access Key/Secret estáticos
2. **Banco de dados:** Nunca usar `root` sem senha em produção
3. **Usuário não-root:** O container executa com usuário `appuser` (sem privilégios)
4. **Rede isolada:** Os serviços comunicam-se na rede interna `vp-network`
5. **CORS:** Atualmente configurado com `*` — restringir em produção para domínios específicos
6. **TTL de URLs:** URLs pré-assinadas expiram em 10 minutos por design
