# Run docker containers (backend REST API & Frontend UI) 
docker compose up --build --remove-orphans

#### REST API:
localhost:8081/document



##  useful commands
docker exec -it <container_name> psql -U admin -d paperless_postgres

##### show dbs
\l
##### show tables
\dt




##### GET _cat/indices?v
##### GET /<index-name>


