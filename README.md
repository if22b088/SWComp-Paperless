# Run docker containers (backend REST API & Frontend UI) 
docker compose up --build --remove-orphans

## Ports

### REST API:
localhost:8081/document
### Adminer (Postgres WebUI)
localhost:9091
### RabbitMQ WebUI
localhost:9093
### MinIO WebUI:
localhost:9090
### Kibana (Elastic Search WebUI)
localhost:9092


##  Useful Commands
docker exec -it <container_name> psql -U admin -d paperless_postgres

#### show dbs
\l
#### show tables
\dt


### Elastic Search Tools Query Commands
#### GET _cat/indices?v
#### GET /<index-name>