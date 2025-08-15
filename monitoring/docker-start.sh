docker network create -d bridge monitoring
docker run --name prometheus --network=monitoring -d -p 9090:9090 -v /home/tdelev/projects/personal/robust-resiliant-api/monitoring/prometheus.yml:/etc/prometheus/prometheus.yml prom/prometheus
docker run --name grafana --network=monitoring -d -p 3000:3000 grafana/grafana
