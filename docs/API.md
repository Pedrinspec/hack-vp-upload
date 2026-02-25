# VP-Upload — Documentação de API (OpenAPI 3.0)

> **Base URL:** `http://localhost:8080`
> **Swagger UI:** `http://localhost:8080/swagger-ui.html`
> **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

---

## Sumário

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/upload` | Interface web de upload |
| `POST` | `/api/upload/start` | Inicia um upload multipart |
| `POST` | `/api/upload/{uploadId}/part/confirm` | Confirma o upload de um chunk |
| `POST` | `/api/upload/{uploadId}/complete` | Finaliza o upload multipart |

---

## Schemas de Dados

### StartUploadRequest

```json
{
  "userId": "string",
  "originalFileName": "string",
  "fileSize": "long",
  "chunkSize": "long"
}
```

| Campo | Tipo | Obrigatório | Descrição |
|-------|------|:-----------:|-----------|
| `userId` | string | Sim | Identificador do usuário proprietário do vídeo |
| `originalFileName` | string | Sim | Nome original do arquivo (ex: `video.mp4`) |
| `fileSize` | long | Sim | Tamanho total do arquivo em bytes |
| `chunkSize` | long | Sim | Tamanho de cada chunk em bytes (recomendado: 16MB = 16777216) |

---

### StartUploadResponse

```json
{
  "uploadId": "string (UUID)",
  "presignedUrls": ["string"]
}
```

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `uploadId` | string (UUID) | Identificador único do upload — use nas chamadas seguintes |
| `presignedUrls` | string[] | Lista de URLs pré-assinadas do S3, uma por chunk. Ordem é indexada (1-based) |

---

### UploadPartConfirmRequest

```json
{
  "partNumber": "int",
  "eTag": "string"
}
```

| Campo | Tipo | Obrigatório | Descrição |
|-------|------|:-----------:|-----------|
| `partNumber` | int | Sim | Número do chunk (começa em 1) |
| `eTag` | string | Sim | Valor do header `ETag` retornado pelo S3 após o PUT do chunk |

---

### CustomErrorResponseDto

```json
{
  "statusCode": "int",
  "message": "string",
  "description": "string"
}
```

---

## Endpoints

---

### `GET /upload`

**Descrição:** Serve a interface web (HTML/Thymeleaf) de upload de vídeos.

**Tag:** View

**Responses:**

| Status | Descrição |
|--------|-----------|
| `200 OK` | Retorna a página HTML de upload |

---

### `POST /api/upload/start`

**Descrição:** Inicia um upload multipart no Amazon S3. Retorna o `uploadId` e a lista de URLs pré-assinadas (uma por chunk). O cliente deve fazer PUT de cada chunk diretamente no S3 usando as URLs fornecidas.

**Tag:** Upload

**Request:**

```http
POST /api/upload/start HTTP/1.1
Content-Type: application/json
```

```json
{
  "userId": "user-abc-123",
  "originalFileName": "meu-video.mp4",
  "fileSize": 1073741824,
  "chunkSize": 16777216
}
```

**Response — 200 OK:**

```json
{
  "uploadId": "550e8400-e29b-41d4-a716-446655440000",
  "presignedUrls": [
    "https://hack-vp-videos.s3.sa-east-1.amazonaws.com/user/user-abc-123/upload/550e.../video/meu-video.mp4?X-Amz-Algorithm=...&partNumber=1&uploadId=...",
    "https://hack-vp-videos.s3.sa-east-1.amazonaws.com/user/user-abc-123/upload/550e.../video/meu-video.mp4?X-Amz-Algorithm=...&partNumber=2&uploadId=...",
    "..."
  ]
}
```

**Exemplo cURL:**

```bash
curl -X POST http://localhost:8080/api/upload/start \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user-abc-123",
    "originalFileName": "meu-video.mp4",
    "fileSize": 1073741824,
    "chunkSize": 16777216
  }'
```

**Notas:**
- O número de URLs retornadas é calculado automaticamente: `ceil(fileSize / chunkSize)`
- Cada URL tem validade de **10 minutos**
- O arquivo é armazenado em S3 na chave: `user/{userId}/upload/{uploadId}/video/{fileName}.{ext}`

---

### `POST /api/upload/{uploadId}/part/confirm`

**Descrição:** Registra a confirmação de que um chunk foi carregado com sucesso no S3. O cliente deve enviar o `ETag` retornado pelo S3 no header da resposta do PUT.

**Tag:** Upload

**Path Parameters:**

| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| `uploadId` | UUID | Identificador do upload (retornado em `/start`) |

**Request:**

```http
POST /api/upload/{uploadId}/part/confirm HTTP/1.1
Content-Type: application/json
```

```json
{
  "partNumber": 1,
  "eTag": "\"5d41402abc4b2a76b9719d911017c592\""
}
```

**Response — 202 Accepted:**

_(corpo vazio)_

**Exemplo cURL:**

```bash
curl -X POST http://localhost:8080/api/upload/550e8400-e29b-41d4-a716-446655440000/part/confirm \
  -H "Content-Type: application/json" \
  -d '{
    "partNumber": 1,
    "eTag": "\"5d41402abc4b2a76b9719d911017c592\""
  }'
```

**Notas:**
- Deve ser chamado **após cada chunk** enviado ao S3 com sucesso
- O `ETag` vem no header `ETag` da resposta HTTP do PUT no S3
- Os dados são armazenados no Redis com TTL de 48 horas
- `partNumber` deve ser >= 1; caso contrário retorna 400

---

### `POST /api/upload/{uploadId}/complete`

**Descrição:** Finaliza o upload multipart no S3, consolida todos os chunks em um único objeto e publica um evento no Kafka para o serviço de processamento de vídeo.

**Tag:** Upload

**Path Parameters:**

| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| `uploadId` | UUID | Identificador do upload |

**Request:**

```http
POST /api/upload/{uploadId}/complete HTTP/1.1
```

_(sem corpo)_

**Response — 202 Accepted:**

```text
Upload finalizado com sucesso!
```

**Exemplo cURL:**

```bash
curl -X POST http://localhost:8080/api/upload/550e8400-e29b-41d4-a716-446655440000/complete
```

**Ações executadas:**
1. Recupera todos os chunks confirmados do Redis
2. Chama a API de `CompleteMultipartUpload` no S3
3. Atualiza o status do upload para `COMPLETED` no MySQL
4. Publica `ProcessRequest` no tópico Kafka `video-processor-topic`
5. Remove os dados de cache do Redis

---

## Fluxo Completo — Exemplo Integrado

```bash
# 1. Iniciar upload de um arquivo de 32MB com chunks de 16MB (2 partes)
RESPONSE=$(curl -s -X POST http://localhost:8080/api/upload/start \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user-123",
    "originalFileName": "demo.mp4",
    "fileSize": 33554432,
    "chunkSize": 16777216
  }')

UPLOAD_ID=$(echo $RESPONSE | jq -r '.uploadId')
URL_PART_1=$(echo $RESPONSE | jq -r '.presignedUrls[0]')
URL_PART_2=$(echo $RESPONSE | jq -r '.presignedUrls[1]')

echo "Upload ID: $UPLOAD_ID"

# 2. Enviar chunk 1 diretamente ao S3
ETAG_1=$(curl -s -o /dev/null -D - -X PUT "$URL_PART_1" \
  --data-binary @<(dd if=demo.mp4 bs=16M count=1 skip=0 2>/dev/null) \
  | grep -i etag | awk '{print $2}' | tr -d '\r')

# 3. Confirmar chunk 1
curl -X POST "http://localhost:8080/api/upload/$UPLOAD_ID/part/confirm" \
  -H "Content-Type: application/json" \
  -d "{\"partNumber\": 1, \"eTag\": $ETAG_1}"

# 4. Enviar e confirmar chunk 2
ETAG_2=$(curl -s -o /dev/null -D - -X PUT "$URL_PART_2" \
  --data-binary @<(dd if=demo.mp4 bs=16M count=1 skip=1 2>/dev/null) \
  | grep -i etag | awk '{print $2}' | tr -d '\r')

curl -X POST "http://localhost:8080/api/upload/$UPLOAD_ID/part/confirm" \
  -H "Content-Type: application/json" \
  -d "{\"partNumber\": 2, \"eTag\": $ETAG_2}"

# 5. Completar upload
curl -X POST "http://localhost:8080/api/upload/$UPLOAD_ID/complete"
```

---

## Tratamento de Erros

### Códigos de Status

| Status | Condição |
|--------|----------|
| `200 OK` | Operação bem-sucedida (POST /start) |
| `202 Accepted` | Operação aceita (confirm / complete) |
| `400 Bad Request` | Dados inválidos ou contagem de chunks divergente |
| `404 Not Found` | Upload ID não encontrado |
| `500 Internal Server Error` | Erros internos (S3, Kafka, etc.) |

### Respostas de Erro (CustomErrorResponseDto)

**400 — Chunk inválido (`NoSuchChunkException`):**
```json
{
  "statusCode": 400,
  "message": "Contagem de chunks divergente",
  "description": "O número de partes confirmadas não corresponde ao esperado"
}
```

**404 — Upload não encontrado (`UploadNotExistException`):**
```json
{
  "statusCode": 404,
  "message": "Upload não existe",
  "description": "Upload 550e8400-e29b-41d4-a716-446655440000 não existe"
}
```

---

## Exceções de Domínio

| Exceção | HTTP | Mensagem |
|---------|------|----------|
| `NoSuchChunkException` | 400 | "Contagem de chunks divergente" |
| `UploadNotExistException` | 404 | "Upload não existe" |
| `InvalidPartNumberException` | 400 | "Quantidade de chunk inválida" |
| `InvalidFileException` | 400 | Mensagem customizada |

---

## Interface Web

| Recurso | URL |
|---------|-----|
| Upload UI | `http://localhost:8080/upload` |

A interface web suporta:
- Seleção de arquivo de vídeo
- Geração automática de User ID (UUID)
- Upload paralelo com até **8 workers simultâneos**
- Chunk size padrão de **16MB**
- Retry automático com backoff exponencial (máx. 3 tentativas)
- Barra de progresso em tempo real (%, MB/s, MB transferidos)

---

## Notas de Integração

### Header ETag do S3
O S3 retorna o `ETag` no header HTTP após cada PUT bem-sucedido. O valor deve ser enviado **com as aspas duplas** na confirmação:

```
ETag: "5d41402abc4b2a76b9719d911017c592"
```

```json
{ "eTag": "\"5d41402abc4b2a76b9719d911017c592\"" }
```

### Tamanho mínimo de chunk
O S3 exige que cada parte (exceto a última) tenha no mínimo **5MB**. O valor recomendado é **16MB** para otimizar performance e throughput.

### CORS
A API está configurada com `@CrossOrigin(origins = "*")`. Em produção, restrinja para os domínios da aplicação cliente.
