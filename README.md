# Spring Boot Agentic RAG Assistant

A Retrieval-Augmented Generation (RAG) application with **agentic tool calling** built using Spring Boot 4, Spring AI 2.0.0-M2, Ollama, PostgreSQL/PGVector.

The LLM decides which tool to invoke — vector search, web search, weather lookup, database query, or file write — based on the user's question. A rate limiter enforces per-tool call limits automatically.

---

## Architecture

```
User Question
      │
      ▼
┌─────────────────────┐
│   RagController     │  POST /api/rag/agentic/query?question=...
└────────┬────────────┘
         │
         ▼
┌─────────────────────┐
│ AgenticRagService   │  delegates to AgentToolExecutor
└────────┬────────────┘
         │
         ▼
┌──────────────────────────────────────────────────┐
│              AgentToolExecutor                    │
│                                                  │
│  1. Resets rate limiter counters                  │
│  2. Sends question + system prompt to LLM         │
│  3. LLM decides which tool(s) to call             │
│  4. Spring AI invokes ToolCallback.call()         │
│     └─ RateLimitedToolCallback intercepts         │
│        └─ ToolRateLimiter checks per-tool limit   │
│        └─ If OK → delegate.call(toolInput)        │
│        └─ If over → error message returned        │
│  5. LLM processes tool results                    │
│  6. Returns JSON: { questions, answer, listOfToolsUsed }
└──────────────────────────────────────────────────┘
```

---

## Available Tools

| Tool | Description | Rate Limit | Priority |
|------|-------------|------------|----------|
| `searchKnowledgeBase(query)` | Vector search across all internal docs | 5 | 1 (always first) |
| `searchKnowledgeBaseByCategory(query, category)` | Search filtered by category | 5 | 1 |
| `webSearch(query)` | DuckDuckGo search for external info | 2 | 3 (fallback) |
| `getCoordinatesForCity(cityName)` | Geocoding for weather lookups | 5 | 2 |
| `getCurrentWeather(latitude, longitude)` | Current weather data | 5 | 2 |
| `getCustomerByEmail(email)` | Database customer lookup | 5 | 4 |
| `writeToFile(filename, content)` | Write content to disk | 5 | 5 |
| `appendToFile(filename, content)` | Append content to file | 5 | 5 |

---

## LLM Tool Selection Rules

The system prompt enforces this decision order:

1. **KB First** — any factual question about the company → `searchKnowledgeBase`
2. **Category Search** — if user specifies a category → `searchKnowledgeBaseByCategory`
3. **Web Fallback** — only if KB returns nothing → `webSearch`
4. **Weather Chain** — `getCoordinatesForCity` → `getCurrentWeather`
5. **DB Lookup** — only when user asks about a specific customer by email
6. **File Write** — only when user explicitly asks to save/write to file

---

## Rate Limiting

Every tool is wrapped with `RateLimitedToolCallback` which intercepts `ToolCallback.call()`:

```java
// Adding a new tool — rate limiting is automatic
@Component
public class EmailTool implements AgenticTool {

    @Override
    public int getMaxCallsPerQuestion() { return 3; }

    @Tool(description = "Send an email")
    public String sendEmail(String to, String body) {
        return "Sent to " + to;
    }
}
```

- Default limit: 5 calls per question (configurable via `getMaxCallsPerQuestion()`)
- `WebSearchTool` override: 2 calls per question
- Counters reset on each new question

---

## Project Structure

```
src/main/java/com/pkm/SpringAI/
├── agent/
│   └── AgentToolExecutor.java      # LLM + tool orchestration
├── tool/
│   ├── AgenticTool.java            # Interface: getMaxCallsPerQuestion()
│   ├── ToolRateLimiter.java        # Shared counter, reset per question
│   ├── RateLimitedToolCallback.java # Wraps ToolCallback, checks limit
│   ├── VectorTool.java             # KB search (searchKnowledgeBase, searchKnowledgeBaseByCategory)
│   ├── WebSearchTool.java          # DuckDuckGo web search
│   ├── WeatherTool.java            # Open-Meteo weather + geocoding
│   ├── DataBaseTool.java           # JDBC customer lookup
│   └── FileWriteTool.java          # File write/append
├── controller/
│   └── RagController.java          # REST endpoints
├── service/
│   ├── AgenticRagService.java      # Interface
│   └── impl/
│       ├── AgenticRagServiceImpl.java
│       ├── DocumentIngestionService.java
│       └── RagQueryServiceImpl.java
├── config/
│   ├── MultiModelConfig.java       # Ollama model config (qwen2.5:7b tool-calling, gemma2:2b)
│   ├── RagConfig.java              # VectorStore, ChatModel beans
│   └── PdfIngestionOnStartup.java  # Auto-ingest PDFs from classpath
└── payload/
    └── RagResponse.java
```

---

## Prerequisites

### 1. Install Ollama

```bash
curl -fsSL https://ollama.com/install.sh | sh
```

### 2. Pull Models

```bash
# Embedding model
ollama pull nomic-embed-text

# Primary tool-calling model (local)
ollama pull qwen2.5:7b

# Fallback models
ollama pull gemma2:2b
ollama pull phi3.5:3.8b
```

### 3. Start PostgreSQL with PGVector

```bash
docker run -d --name pgvector \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=springai \
  -p 5432:5432 \
  pgvector/pgvector:pg17
```

```bash
psql -U postgres -d springai
CREATE EXTENSION IF NOT EXISTS vector;
\q
```

---

## How to Run

```bash
./gradlew bootRun
```

Application starts on `http://localhost:8080`.

---

## How to Test

### 1. Ingest Documents

Place PDF files in `src/main/resources/pdfs/` — they auto-ingest on startup.

Or manually ingest via API:

```bash
curl -X POST http://localhost:8080/api/rag/ingest \
  -F "file=@path/to/your-document.pdf"
```

### 2. Query — KB Search (Vector)

```bash
curl "http://localhost:8080/api/rag/agentic/query?question=What+is+the+company+remote+work+policy?"
```

Expected flow: LLM calls `searchKnowledgeBase` → returns answer from ingested docs.

### 3. Query — Web Search Fallback

```bash
curl "http://localhost:8080/api/rag/agentic/query?question=What+are+the+latest+AWS+region+announcements+in+2026?"
```

Expected flow: LLM calls `searchKnowledgeBase` (no results) → calls `webSearch` → returns answer.

### 4. Query — Weather

```bash
curl "http://localhost:8080/api/rag/agentic/query?question=What+is+the+current+weather+in+Berlin?"
```

Expected flow: LLM calls `getCoordinatesForCity("Berlin")` → calls `getCurrentWeather(lat, lon)` → returns weather.

### 5. Query — Database Lookup

```bash
curl "http://localhost:8080/api/rag/agentic/query?question=Show+me+the+account+details+for+jane@example.com"
```

Expected flow: LLM calls `getCustomerByEmail("jane@example.com")` → returns customer data.

### 6. Query — File Write

```bash
curl "http://localhost:8080/api/rag/agentic/query?question=Save+a+summary+of+the+remote+work+policy+to+remote-work-summary.txt"
```

Expected flow: LLM calls `searchKnowledgeBase` → then calls `writeToFile("remote-work-summary.txt", ...)`.

### 7. Verify Rate Limiting

Ask a question that triggers multiple tool calls:

```bash
curl "http://localhost:8080/api/rag/agentic/query?question=What+is+the+company+holiday+policy+and+what+is+the+weather+in+London?"
```

Check logs for `Tool call limit reached` messages if any tool exceeds its limit.

---

## Response Format

All agentic queries return raw JSON:

```json
{
  "questions": "What is the company remote work policy?",
  "answer": "The company allows remote work up to 3 days per week...",
  "listOfToolsUsed": ["searchKnowledgeBase"]
}
```

---

## Configuration

### Rate Limits (application.yaml)

```yaml
app:
  tools:
    file-output-dir: ./agent-output
    web-search:
      max-calls-per-question: 2
```

### Timeouts (application.yaml)

```yaml
spring:
  http:
    client:
      connect-timeout: 30s
      read-timeout: 600s
```

### Model Config (MultiModelConfig.java)

- **qwen2.5:7b** — tool-calling model (numPredict=1024, numCtx=8192)
- **gemma2:2b** — primary general model
- **phi3.5:3.8b** — secondary general model

---

## Tech Stack

- Spring Boot 4.0.3
- Spring AI 2.0.0-M2
- Java 25
- Ollama (local LLM)
- PostgreSQL + PGVector
- Apache Tika (PDF parsing)
- Lombok
