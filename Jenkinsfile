pipeline {
  agent any

  environment {
    DOCKERHUB_REPO = 'aa309m/myapp'
    DOCKER_CREDS   = 'dockerhub_creds'   // Jenkins Username/Password cred for Docker Hub
    SSH_CREDS      = 'ansible_ssh'       // Jenkins SSH cred for ec2-user on K8s/Ansible host
    K8S_HOST       = '54.152.145.23'     // <-- put your K8s/Ansible host here (has kubectl + ansible)
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Maven Build') {
      steps {
        sh 'mvn -B clean package'
      }
    }

    stage('Docker Build & Push (on Jenkins)') {
      steps {
        withCredentials([usernamePassword(
          credentialsId: env.DOCKER_CREDS,
          usernameVariable: 'DOCKER_USER',
          passwordVariable: 'DOCKER_PASS'
        )]) {
          sh """
            echo "Logging in to Docker Hub"
            echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin

            echo "Building image tags: \${BUILD_NUMBER} and latest"
            docker build -t ${DOCKERHUB_REPO}:\${BUILD_NUMBER} -t ${DOCKERHUB_REPO}:latest .

            echo "Pushing image"
            docker push ${DOCKERHUB_REPO}:\${BUILD_NUMBER}
            docker push ${DOCKERHUB_REPO}:latest
          """
        }
      }
    }

    stage('Prepare Manifests on K8s Host') {
      steps {
        sshagent(credentials: [env.SSH_CREDS]) {
          sh """
            # Copy manifests to K8s host
            scp -o StrictHostKeyChecking=no k8s/deployment.yaml ec2-user@${K8S_HOST}:/home/ec2-user/deployment.yaml
            scp -o StrictHostKeyChecking=no k8s/service.yaml    ec2-user@${K8S_HOST}:/home/ec2-user/service.yaml

            # Replace the image tag placeholder with the current build number
            ssh -o StrictHostKeyChecking=no ec2-user@${K8S_HOST} \\
              "sed -i 's|BUILD_TAG|${BUILD_NUMBER}|g' /home/ec2-user/deployment.yaml"
          """
        }
      }
    }

    stage('Deploy to Kubernetes via Ansible') {
      steps {
        sshagent(credentials: [env.SSH_CREDS]) {
          sh """
            # Ensure inventory exists on remote host (create minimal one-time inline if missing)
            ssh -o StrictHostKeyChecking=no ec2-user@${K8S_HOST} \\
              'test -f ~/hosts.ini || echo -e "[eks]\\nlocalhost ansible_connection=local" > ~/hosts.ini'

            # Ensure k8s_deploy.yml exists on remote host (copy once if you prefer keeping it in repo)
            scp -o StrictHostKeyChecking=no ansible/k8s_deploy.yml ec2-user@${K8S_HOST}:/home/ec2-user/k8s_deploy.yml

            # Run the playbook on the K8s host
            ssh -o StrictHostKeyChecking=no ec2-user@${K8S_HOST} \\
              'ansible-playbook ~/k8s_deploy.yml -i ~/hosts.ini'
          """
        }
      }
    }

    stage('Post-Deployment Check') {
      steps {
        sshagent(credentials: [env.SSH_CREDS]) {
          sh """
            ssh -o StrictHostKeyChecking=no ec2-user@${K8S_HOST} \\
              "kubectl get deploy,po,svc -o wide"
          """
        }
      }
    }
  }
}

