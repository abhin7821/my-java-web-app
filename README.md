## Java Web App CI/CD Pipeline on AWS  
### *(GitHub → Jenkins → Ansible → Docker → Kubernetes)*

---

## Overview
This project demonstrates a **complete end-to-end CI/CD pipeline** for a Java-based web application using leading DevOps tools — from source code to deployment on a live Kubernetes cluster.

It integrates **GitHub**, **Jenkins**, **Ansible**, **Docker**, **Docker Hub**, and **Kubernetes (EKS)** to automate the entire build, deployment, and release process.

---

## Architecture Workflow

Developer → GitHub → Jenkins → Ansible → Docker → Docker Hub → Kubernetes (EKS) → AWS LoadBalancer → User

---

## Tools and Technologies Used

|Tool / Service  |Purpose |
|----------------|----------|
| **GitHub** | Source Code Management (SCM) and webhook trigger |
| **Jenkins** | CI/CD orchestration and pipeline automation |
| **Maven** | Build automation tool for packaging Java WAR |
| **Docker** | Containerization of the application |
| **Docker Hub** | Central registry for storing Docker images |
| **Ansible** | Automates Docker build and Kubernetes deployment |
| **Kubernetes (EKS)** | Container orchestration and scaling |
| **Tomcat** | Java web server runtime for WAR |
| **AWS EC2** | Hosts Jenkins, Docker, Ansible, and K8s nodes |
| **AWS LoadBalancer** | Exposes the application publicly |

---

## Pipeline Workflow

### 1️⃣ Developer Stage
- Developer commits and pushes code to the main branch in GitHub.  
- A **GitHub Webhook** triggers Jenkins automatically on every commit.

---

### 2️⃣ Jenkins CI Stage
- Jenkins pulls the latest code from GitHub.  
- Jenkins connects to the **Ansible server** via SSH.  
- Ansible runs `playbook_docker.yml` to:
  - Build the Docker image using the project’s Dockerfile.  
  - Tag and push it to **Docker Hub** (`aa309m/myapp:latest`).

---

### 3️⃣ Jenkins CD Stage
- Jenkins triggers another Ansible playbook `k8s_deploy.yml`.  
- This deploys the latest image to **Kubernetes (EKS)** using:
  - `deployment.yml` (for Pods)  
  - `service.yml` (for LoadBalancer)

---

### 4️⃣ Kubernetes Stage
- Kubernetes pulls the latest image from Docker Hub.  
- Pods and Services are created.  
- AWS ELB exposes the application publicly.

---

### 5️⃣ Verification Stage
- Jenkins verifies deployment via:
  ```bash
  kubectl get pods
  kubectl get svc
  curl http://<load-balancer-dns>/myapp/HelloServlet

## Expected Output:

Hello from HelloServlet!
{"id": "1111", "name": "Up1"}

## Folder Structure
```text
my-java-web-app/
│
├── Dockerfile
├── Jenkinsfile
├── pom.xml
│
├── src/
│   ├── main/java/api/servlet/HelloServlet.java
│   └── main/webapp/WEB-INF/web.xml
│
├── ansible/
│   ├── playbook_docker.yml
│   ├── k8s_deploy.yml
│
├── k8s/
│   ├── deployment.yaml
│   └── service.yaml
│
└── README.md
```
## Key Jenkinsfile Stages
```text
pipeline {
    agent any
    stages {

        stage('Build & Push Docker Image') {
            steps {
                echo 'Building Docker image and pushing to DockerHub...'
                sshagent(['ansible_ssh']) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no ec2-user@<ansible-server-ip> "
                        ansible-playbook ~/playbook_docker.yml
                        "
                    '''
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                echo 'Deploying to Kubernetes...'
                sshagent(['ansible_ssh']) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no ec2-user@<ansible-server-ip> "
                        ansible-playbook ~/k8s_deploy.yml
                        "
                    '''
                }
            }
        }

        stage('Post-Deployment Check') {
            steps {
                echo 'Verifying deployed application...'
                sshagent(['ansible_ssh']) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no ec2-user@<k8s-server-ip> "
                        kubectl get svc
                        "
                    '''
                }
            }
        }
    }
}
```
## 🧩 Common Commands Used

| **Purpose** | **Command** |
|--------------|-------------|
| Build WAR file | `mvn clean package -DskipTests` |
| Build Docker image | `docker build -t aa309m/myapp:latest .` |
| Push to Docker Hub | `docker push aa309m/myapp:latest` |
| Get pods | `kubectl get pods -o wide` |
| Get services | `kubectl get svc` |
| Scale pods | `kubectl scale deployment myapp-deployment --replicas=2` |
| Restart deployment | `kubectl rollout restart deployment myapp-deployment` |
| Test endpoint | `curl http://<load-balancer-dns>/myapp/HelloServlet` |


## Sample Output

$ kubectl get pods
NAME                       READY   STATUS    RESTARTS   AGE
myapp-deployment-xxxx      1/1     Running   0          2m
myapp-deployment-yyyy      1/1     Running   0          2m

$ curl http://<load-balancer-dns>/myapp/HelloServlet
Hello from HelloServlet!
{"id": "1111", "name": "Up1"}

## Key Learnings
```text
✅ Built and automated a multi-server CI/CD pipeline from scratch
✅ Configured GitHub Webhook → Jenkins Trigger for automatic builds
✅ Implemented Ansible automation for Docker & Kubernetes deployment
✅ Debugged real-world issues (EC2 IP changes, credential fixes, etc.)
✅ Connected Jenkins → Ansible → EKS for full infrastructure delivery
```
## Result

Fully automated CI/CD pipeline

End-to-end integration: GitHub → Jenkins → Ansible → Docker → Kubernetes

Real-time build triggers via GitHub Webhooks

Application deployed & verified via AWS LoadBalancer

