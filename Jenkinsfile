pipeline {
    agent any

    stages {

        stage('Build & Push Docker Image') {
            steps {
                echo 'Building Docker image and pushing to DockerHub...'
                sshagent(['ansible_ssh']) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no ec2-user@54.91.24.159 "
                        ansible-playbook ~/playbook_docker.yml
                        "
                    '''
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                echo 'Deploying to Kubernetes (EKS Cluster)...'
                sshagent(['ansible_ssh']) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no ec2-user@54.91.24.159 "
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
                        ssh -o StrictHostKeyChecking=no ec2-user@54.91.24.159 "
                        kubectl get svc
                        "
                    '''
                }
            }
        }
    }
}
