docker build --platform linux/amd64/v2 -t management-service .
docker tag management-service:latest andrewsynkov/management-service:latest
docker push andrewsynkov/management-service:latest 