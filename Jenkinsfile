pipeline {
  agent {
    // 👇 This ensures Jenkins always uses a fixed workspace (no @2/@tmp issues)
    customWorkspace '/var/lib/jenkins/workspace/Pipeline'
  }

  environment {
    // 🔧 Docker & Ansible configuration
    DOCKERHUB_REPO = 'aa309m/myapp'
    DOCKER_CREDS   = 'dockerhub_new'   // Jenkins credentials ID for DockerHub (username/password)
    SSH_CREDS      = 'ansible_ssh'     // Jenkins SSH credentials ID for ec2-user on Ansible/K8s host
    K8S_HOST       = '13.220.117.130'  // EC2 host having Ansible + kubectl configured
  }

  options {
    // Safety: stop old builds if a new one starts
    disableConcurrentBuilds()
    // Keep only 10 last builds to save space
    buildDiscarder(logRotator(numToKeepStr: '10'))
  }

  stages {

    stage('Checkout') {
      steps {
        echo "📦 Checking out source code from GitHub..."
        checkout scm
      }
    }

    stage('Maven Build') {
      steps {
        echo "🔨 Building Java WAR package using Maven..."
        sh 'mvn -B clean package'
      }
    }

    stage('Docker Build & Push (on Jenkins)') {
      steps {
        echo "🐳 Building and pushing Docker image to DockerHub..."
        withCredentials([usernamePassword(
          credentialsId: env.DOCKER_CREDS,
          usernameVariable: 'DOCKER_USER',
          passwordVariable: 'DOCKER_PASS'
        )]) {
          sh '''
            echo "Logging in to Docker Hub..."
            echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin

            echo "Building Docker image..."
            docker build -t ${DOCKERHUB_REPO}:${BUILD_NUMBER} -t ${DOCKERHUB_REPO}:latest .

            echo "Pushing Docker image to DockerHub..."
            docker push ${DOCKERHUB_REPO}:${BUILD_NUMBER}
            docker push ${DOCKERHUB_REPO}:latest

            echo "✅ Docker image build & push successful!"
          '''
        }
      }
    }

    stage('Prepare Manifests on K8s Host') {
      steps {
        echo "🧩 Preparing Kubernetes manifests on K8s host..."
        sshagent(credentials: [env.SSH_CREDS]) {
          sh '''
            echo "Copying manifests to remote host..."
            scp -o StrictHostKeyChecking=no k8s/deployment.yaml ec2-user@${K8S_HOST}:/home/ec2-user/deployment.yaml
            scp -o StrictHostKeyChecking=no k8s/service.yaml ec2-user@${K8S_HOST}:/home/ec2-user/service.yaml

            echo "Updating deployment.yaml with current image tag..."
            ssh -o StrictHostKeyChecking=no ec2-user@${K8S_HOST} \
              "sed -i 's|aa309m/myapp:latest|aa309m/myapp:${BUILD_NUMBER}|g' /home/ec2-user/deployment.yaml"
          '''
        }
      }
    }

    stage('Deploy to Kubernetes via Ansible') {
      steps {
        echo "🚀 Deploying application to Kubernetes via Ansible..."
        sshagent(credentials: [env.SSH_CREDS]) {
          sh '''
            echo "Ensuring Ansible inventory exists..."
            ssh -o StrictHostKeyChecking=no ec2-user@${K8S_HOST} \
              'test -f ~/hosts.ini || echo -e "[eks]\\nlocalhost ansible_connection=local" > ~/hosts.ini'

            echo "Copying playbook to remote host..."
            scp -o StrictHostKeyChecking=no ansible/k8s_deploy.yml ec2-user@${K8S_HOST}:/home/ec2-user/k8s_deploy.yml

            echo "Running Ansible playbook..."
            ssh -o StrictHostKeyChecking=no ec2-user@${K8S_HOST} \
              'ansible-playbook ~/k8s_deploy.yml -i ~/hosts.ini'

            echo "✅ Ansible deployment completed."
          '''
        }
      }
    }

    stage('Post-Deployment Check') {
      steps {
        echo "🔍 Validating deployed Kubernetes resources..."
        sshagent(credentials: [env.SSH_CREDS]) {
          sh '''
            ssh -o StrictHostKeyChecking=no ec2-user@${K8S_HOST} \
              "kubectl get deploy,po,svc -o wide"
          '''
        }
      }
    }
  }

  post {
    success {
      echo "✅ Pipeline completed successfully! Application deployed."
    }
    failure {
      echo "❌ Pipeline failed. Please check logs above for the failed stage."
    }
  }
}

