# 🏨 hotel-service

Reactive microservice for managing hotel room availability and bookings.

---

## 💻 Local Development
### ✅ Required Tools
- **Java 21**: Use [sdkman](https://sdkman.io/) for installation.
- **Maven 3.x.x**: Alternatively, use the wrapper included in the root project.
- Docker & Docker Compose

## 🌱 Environment Variables (`:default`)

| Variable                     | Description                      | Default            |
|-----------------------------|----------------------------------|--------------------|
| `POSTGRES_HOST`             | PostgreSQL host                  | `localhost`        |
| `POSTGRES_PORT`             | PostgreSQL port                  | `5433`             |
| `POSTGRES_DB_NAME`          | PostgreSQL database name         | `hoteldb`          |
| `POSTGRES_DB_USER`          | PostgreSQL username              | `hoteluser`        |
| `POSTGRES_DB_PASSWORD`      | PostgreSQL password              | `secret`           |
| `GRPC_SERVER_ENABLED`       | Enable gRPC server               | `true`             |
| `GRPC_SERVER_PORT`          | gRPC server port                 | `9090`             |

---

## 🐳 Docker Image Build

Builds Docker image with `:latest` and Git `:sha` tags.

### Build and Push to Docker Hub

> Requires Docker credential helper. See: [jib documentation](https://github.com/GoogleContainerTools/jib)

```shell
 ./mvnw compile jib:build -Dimage=docker.io/motpansergiu/hotel-service
```

## Run Using Docker
```shell
    docker run --name hotel-service \
      -p 8080:8080 \
      -e POSTGRES_HOST=localhost \
      -e POSTGRES_PORT=5433 \
      -e POSTGRES_DB_NAME=hoteldb \
      -e POSTGRES_DB_USER=hoteluser \
      -e POSTGRES_DB_PASSWORD=secret \
      -e GRPC_SERVER_ENABLED=true \
      -e GRPC_SERVER_PORT=9090 \
      motpansergiu/hotel-service:latest
```
