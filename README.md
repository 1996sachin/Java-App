# Kubernetes + Java WAR + Ingress/NodePort + ELK/Filebeat (Assessment Pack)

This repo contains **all artifacts** you can submit as deliverables:

- **Java WAR app** (simple servlet + JSP) that writes logs to stdout
- **Dockerfile** to run the WAR on Tomcat
- **Kubernetes manifests** for:
  - Java app `Deployment` + `Service` (+ `NodePort`)
  - `Ingress` (nginx-compatible)
  - ELK stack: Elasticsearch + Logstash + Kibana
  - Filebeat `DaemonSet` collecting container logs and shipping to Logstash

## 0) Folder layout

- `app/` – Java WAR source + Maven build
- `docker/` – Dockerfile for WAR on Tomcat
- `k8s/app/` – app deployment/service/nodeport/ingress
- `k8s/elk/` – elasticsearch/logstash/kibana/filebeat
- `runbook/` – commands and “what to screenshot/capture”

## 1) Cluster setup (Rancher-managed)

Your assessment asks for a Rancher-managed cluster (k3s or k8s) with **1 control-plane (master)** and **1 worker**.

Use Rancher UI to:

- Create cluster (RKE2 or k3s are common)
- Add 2 nodes:
  - Node A roles: **Control Plane + etcd** (and optionally Worker disabled)
  - Node B roles: **Worker**
- Confirm nodes are Ready.

### Capture for deliverable

Run:

```bash
kubectl get nodes -o wide
```

Take a screenshot or copy the output showing **two nodes** and their roles/status.

## 2) Build the WAR + container image

### Build WAR locally (example)

```bash
cd app
./mvnw -q -DskipTests package
ls -la target/*.war
```

### Build container image

From repo root:

```bash
docker build -t java-war-demo:1.0.0 -f docker/Dockerfile .
```

Push to your registry (example):

```bash
docker tag java-war-demo:1.0.0 <your-registry>/java-war-demo:1.0.0
docker push <your-registry>/java-war-demo:1.0.0
```

Then update the image in `k8s/app/deployment.yaml`.

## 3) Deploy app + expose via NodePort + Ingress

```bash
kubectl apply -f k8s/app/namespace.yaml
kubectl apply -f k8s/app/
kubectl -n demo get deploy,po,svc,ingress -o wide
```

### NodePort access

```bash
kubectl -n demo get svc java-war-svc-nodeport
```

Use any node’s IP:

`http://<any-node-ip>:30080/java-war-demo/`

### Ingress access

This pack assumes an nginx ingress controller is installed in your cluster.

Add a DNS record (or local `/etc/hosts`) for:

- `java.demo.local` → your ingress external IP / load balancer IP / node IP (depending on your setup)

Then:

`http://java.demo.local/java-war-demo/`

## 4) Deploy ELK + Filebeat (in-cluster)

Apply manifests:

```bash
kubectl apply -f k8s/elk/namespace.yaml
kubectl apply -f k8s/elk/
kubectl -n observability get all
```

Port-forward Kibana:

```bash
kubectl -n observability port-forward svc/kibana 5601:5601
```

Open:

`http://localhost:5601`

### Kibana data view

Create a Data View:

- Name: `logstash-*`
- Timestamp field: `@timestamp`

Then open **Discover** and filter:

- `kubernetes.labels.app : "java-war"`

## 5) Generate traffic + verify logs

```bash
kubectl -n demo port-forward svc/java-war-svc 8080:8080
curl -sS http://localhost:8080/java-war-demo/
curl -sS "http://localhost:8080/java-war-demo/api/ping?name=assessment"
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

## Notes / assumptions

- This pack uses:
  - **Elasticsearch single-node** (dev/test sizing) with `emptyDir` storage (OK for assessment demos).
  - Filebeat reads **container logs** from `/var/log/containers/*.log` on each node.
  - Filebeat ships to **Logstash**, Logstash writes to **Elasticsearch index** `logstash-YYYY.MM.dd`.

