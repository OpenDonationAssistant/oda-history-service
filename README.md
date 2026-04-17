# ODA History Service
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/OpenDonationAssistant/oda-history-service)
![Sonar Tech Debt](https://img.shields.io/sonar/tech_debt/OpenDonationAssistant_oda-history-service?server=https%3A%2F%2Fsonarcloud.io)
![Sonar Violations](https://img.shields.io/sonar/violations/OpenDonationAssistant_oda-history-service?server=https%3A%2F%2Fsonarcloud.io)
![Sonar Tests](https://img.shields.io/sonar/tests/OpenDonationAssistant_oda-history-service?server=https%3A%2F%2Fsonarcloud.io)
![Sonar Coverage](https://img.shields.io/sonar/coverage/OpenDonationAssistant_oda-history-service?server=https%3A%2F%2Fsonarcloud.io)

## Running with Docker

The service is published to GitHub Container Registry: `ghcr.io/opendonationassistant/oda-history-service`

### Required Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `RABBITMQ_HOST` | RabbitMQ host | `localhost` |
| `JDBC_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost/postgres?currentSchema=history` |
| `JDBC_USER` | PostgreSQL username | `postgres` |
| `JDBC_PASSWORD` | PostgreSQL password | `postgres` |

### Example Docker Run

```bash
docker run -d \
  --name oda-history-service \
  -p 8080:8080 \
  -e RABBITMQ_HOST=rabbitmq \
  -e JDBC_URL=jdbc:postgresql://postgres:5432/history?currentSchema=history \
  -e JDBC_USER=postgres \
  -e JDBC_PASSWORD=postgres \
  ghcr.io/opendonationassistant/oda-history-service:latest
```

### With Docker Compose

```yaml
services:
  history-service:
    image: ghcr.io/opendonationassistant/oda-history-service:latest
    ports:
      - "8080:8080"
    environment:
      - RABBITMQ_HOST=rabbitmq
      - JDBC_URL=jdbc:postgresql://postgres:5432/history?currentSchema=history
      - JDBC_USER=postgres
      - JDBC_PASSWORD=postgres
    depends_on:
      - postgres
      - rabbitmq

  postgres:
    image: postgres:16
    environment:
      - POSTGRES_DB=history
    volumes:
      - postgres_data:/var/lib/postgresql/data

  rabbitmq:
    image: rabbitmq:3-management

volumes:
  postgres_data:
```
