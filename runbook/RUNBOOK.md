# Runbook (copy/paste for assessment)

## A) Local cluster on this host (1 master + 1 worker)

This is the easiest “all-localhost” setup here: **kind** (Kubernetes-in-Docker) with 1 control-plane + 1 worker.

Prereqs:
- Docker installed and your user can run it (`docker ps` works), or use `sudo docker ...`.

Install kind (Linux):

```bash
curl -Lo ./kind https://kind.sigs.k8s.io/dl/latest/kind-linux-amd64
chmod +x ./kind
sudo mv ./kind /usr/local/bin/kind
kind version
```

Create cluster (maps host ports for Ingress + NodePort):

```bash
cat > kind-config.yaml <<'EOF'
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
  - role: control-plane
    extraPortMappings:
      - containerPort: 80
        hostPort: 8088
        protocol: TCP
      - containerPort: 30080
        hostPort: 30080
        protocol: TCP
  - role: worker
EOF

kind create cluster --name assessment --config kind-config.yaml
kubectl get nodes -o wide
```

### Evidence to capture

```bash
kubectl get nodes -o wide
kubectl get pods -A | head
```

Save output/screenshot showing **2 nodes** and cluster running.

## B) Build WAR + image

```bash
cd app
./mvnw -q -DskipTests package
ls -la target/java-war-demo.war
```

Build image:

```bash
cd ..
docker build -t java-war-demo:1.0.0 -f docker/Dockerfile .
```

Load image into the cluster (no registry needed):

```bash
kind load docker-image java-war-demo:1.0.0 --name assessment
```

## C) Deploy app (Deployment + Service + NodePort + Ingress)

```bash
kubectl apply -f k8s/app/namespace.yaml
kubectl apply -f k8s/app/deployment.yaml
kubectl apply -f k8s/app/service.yaml
kubectl apply -f k8s/app/service-nodeport.yaml
kubectl apply -f k8s/app/ingress.yaml
```

Verify:

```bash
kubectl -n demo get deploy,po,svc,ingress -o wide
kubectl -n demo describe ingress java-war-ingress
```

### NodePort test

```bash
kubectl -n demo get svc java-war-svc-nodeport -o wide
curl -sS http://localhost:30080/
curl -sS "http://localhost:30080/api/ping?name=nodeport"
```

### Ingress test

Add to `/etc/hosts`:

`127.0.0.1 java.demo.local`

Then:

```bash
curl -sS http://java.demo.local:8088/
curl -sS "http://java.demo.local:8088/api/ping?name=ingress"
```

### Install Ingress controller (ingress-nginx)

```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.11.2/deploy/static/provider/kind/deploy.yaml
kubectl -n ingress-nginx rollout status deploy/ingress-nginx-controller
```

## D) Deploy ELK + Filebeat

```bash
kubectl apply -f k8s/elk/namespace.yaml
kubectl apply -f k8s/elk/elasticsearch.yaml
kubectl apply -f k8s/elk/logstash.yaml
kubectl apply -f k8s/elk/kibana.yaml
kubectl apply -f k8s/elk/filebeat.yaml
```

Wait:

```bash
kubectl -n observability get pods -w
```

### Quick health checks

```bash
kubectl -n observability port-forward svc/elasticsearch 9200:9200
curl -sS http://localhost:9200 | head
```

Kibana:

```bash
kubectl -n observability port-forward svc/kibana 5601:5601
```

Open `http://localhost:5601`.

## E) Verify logs in Kibana

Generate traffic:

```bash
kubectl -n demo port-forward svc/java-war-svc 8080:8080
curl -sS "http://localhost:8080/api/ping?name=kibana"
```

Check app logs:

```bash
kubectl -n demo logs deploy/java-war --tail=200
```

Check Filebeat + Logstash:

```bash
kubectl -n observability logs ds/filebeat --tail=100
kubectl -n observability logs deploy/logstash --tail=200
```

In Kibana:

1. **Stack Management → Data Views → Create data view**
   - Name: `logstash-*`
   - Timestamp field: `@timestamp`
2. **Discover**
   - Filter: `kubernetes.labels.app : "java-war"`

### Evidence to capture

- Screenshot of Kibana Discover showing `event=ping` log lines from the app.
- (Optional) Create a dashboard with a saved search for those logs and screenshot it.

