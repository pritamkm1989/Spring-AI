# Spring Boot RAG Assistant with Ollama, PostgreSQL, and Spring AI

A Retrieval-Augmented Generation (RAG) application built using Spring Boot, Spring AI, Ollama, PostgreSQL, and PGVector.

This project demonstrates how to build a conversational AI assistant capable of answering questions from private documents while maintaining conversation history and minimizing hallucinations.
### Features
### Document Ingestion
For further reference, please consider the following sections:

* PDF support using Apache Tika
* Automatic document chunking
* Metadata enrichment
* Vector embedding generation
* Storage in PostgreSQL PGVector

### Retrieval-Augmented Generation (RAG)
The following guides illustrate how to use some features concretely:

* Semantic vector search
* Context-aware question answering
* Source document tracking
* Similarity threshold filtering

### Conversational AI

* Chat memory support
* Session-based conversations
* Context-aware follow-up questions

### Local AI Stack

Runs entirely on local infrastructure.

* Ollama
* Gemma 2B / Qwen 3
* PostgreSQL
* Spring Boot

### Ollama

Install Ollama:

curl -fsSL https://ollama.com/install.sh | sh

Verify installation:

ollama list
#### Pull Required Models

Embedding model:

ollama pull nomic-embed-text

ollama pull gemma2:2b

### PGVector Extension

docker run -d --name pgvector   -e POSTGRES_USER=postgres  -e POSTGRES_PASSWORD=postgres  -e POSTGRES_DB=springai  -p 5422:5432 pgvector/pgvector:pg17

psql -U postgres -d springai

\dx

CREATE EXTENSION IF NOT EXISTS vector;

\dt List of relations


## Application Configuration


    spring:
      datasource:
        url: jdbc:postgresql://localhost:5432/ragdb
        username: postgres
        password: password

      ai:
        ollama:
            chat:
                model: gemma2:2b
            embedding:
                model: nomic-embed-text

        vectorstore:
            pgvector:
            initialize-schema: true
            remove-existing-vector-store-table: true
            dimensions: 768
            distance-type: COSINE_DISTANCE


## Author

Pritam Mohapatra

Spring Boot Developer | Integration Developer | Exploring AI Engineering with Java, Spring AI, RAG, and Ollama
