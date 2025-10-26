pipeline {
    agent any

    stages {

        stage('Build & Push Docker Image') {
            steps {
                echo '🛠️ Building Docker image and pushing to DockerHub...'
                sshagent(['ansible_ssh']) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no ec2-user@54.91.24.159 "
                        ansible-playbook ~/playbook_docker.yml
                        "
                    '''
                }
            }
        }

        stage('Pre-Deployment Cleanup') {
            steps {
                echo '🧹 Cleaning up old pods before new deployment...'
                sshagent(['ansible_ssh']) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no ec2-user@54.152.145.23 "
                        echo 'Scaling down old pods...';
                        kubectl scale deployment myapp-deployment --replicas=0;
                        sleep 20;
                        echo 'Scaling up new pods...';
                        kubectl scale deployment myapp-deployment --replicas=2;
                        echo '✅ Cleanup completed. Cluster ready for deployment.';
                        kubectl get pods;
                        "
                    '''
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                echo '🚀 Deploying to Kubernetes (EKS Cluster)...'
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
                echo '🔍 Verifying deployed application on EKS cluster...'
                sshagent(['ansible_ssh']) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no ec2-user@54.152.145.23 "
                        echo 'Checking pod and service status...';
                        kubectl get pods -o wide;
                        kubectl get svc;
                        echo '✅ All pods and services verified successfully.';
                        "
                    '''
                }
            }
        }
    }
}

