
pipeline {
    agent any

    options {
        timeout(time: 30, unit: 'MINUTES')
    }

    environment {
        BACKEND_IMAGE = 'mehdiboughdiri/appgestion-backend'
        FRONTEND_IMAGE = 'mehdiboughdiri/appgestion-frontend'
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/mahdiboughdiri/devops.git'
            }
        }

        stage('Compile') {
            steps {
                dir('backend') {
                    sh 'mvn compile'
                }
            }
        }

        stage('Unit Tests & JaCoCo') {
            steps {
                dir('backend') {
                    sh 'mvn test jacoco:report'
                }
            }
            post {
                always {
                    junit allowEmptyResults: true,
                          testResults: 'backend/target/surefire-reports/*.xml'

                    archiveArtifacts(
                        artifacts: 'backend/target/site/jacoco/**',
                        allowEmptyArchive: true
                    )
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                dir('backend') {
                    withSonarQubeEnv('SonarQube') {
                        sh '''
                            mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
                            -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                        '''
                    }
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Package') {
            steps {
                dir('backend') {
                    sh 'mvn package -DskipTests'
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh '''
                    docker build \
                        -t ${BACKEND_IMAGE}:latest \
                        -t ${BACKEND_IMAGE}:build-${BUILD_NUMBER} \
                        ./backend

                    docker build \
                        -t ${FRONTEND_IMAGE}:latest \
                        -t ${FRONTEND_IMAGE}:build-${BUILD_NUMBER} \
                        ./frontend
                '''
            }
        }

        stage('Docker Push') {
            steps {
                script {
                    withCredentials([
                        usernamePassword(
                            credentialsId: 'dockerhub-credentials',
                            usernameVariable: 'DOCKER_USERNAME',
                            passwordVariable: 'DOCKER_PASSWORD'
                        )
                    ]) {
                        try {
                            sh '''
                                set +x

                                echo "$DOCKER_PASSWORD" | docker login \
                                    -u "$DOCKER_USERNAME" \
                                    --password-stdin

                                docker push ${BACKEND_IMAGE}:build-${BUILD_NUMBER}
                                docker push ${FRONTEND_IMAGE}:build-${BUILD_NUMBER}

                                docker push ${BACKEND_IMAGE}:latest
                                docker push ${FRONTEND_IMAGE}:latest
                            '''
                        } finally {
                            sh 'docker logout || true'
                        }
                    }
                }
            }
        }

        stage('Deploy Kubernetes') {
            steps {
                withCredentials([
                    file(
                        credentialsId: 'kubeconfig-credentials',
                        variable: 'KUBECONFIG'
                    )
                ]) {
                    sh '''
                        set -eu

                        echo "=== Vérification de kubectl ==="
                        kubectl version --client

                        echo "=== Vérification de la connexion au cluster ==="
                        kubectl get nodes

                        echo "=== Validation des manifests ==="
                        kubectl apply --dry-run=client \
                            -f k8s/appgestion.yaml

                        echo "=== Application des ressources Kubernetes ==="
                        kubectl apply -f k8s/appgestion.yaml

                        echo "=== Mise à jour de l'image Backend ==="
                        kubectl set image deployment/backend \
                            backend=${BACKEND_IMAGE}:build-${BUILD_NUMBER}

                        echo "=== Mise à jour de l'image Frontend ==="
                        kubectl set image deployment/frontend \
                            frontend=${FRONTEND_IMAGE}:build-${BUILD_NUMBER}

                        echo "=== Attente du déploiement MySQL ==="
                        kubectl rollout status deployment/mysql \
                            --timeout=180s

                        echo "=== Attente du déploiement Backend ==="
                        kubectl rollout status deployment/backend \
                            --timeout=180s

                        echo "=== Attente du déploiement Frontend ==="
                        kubectl rollout status deployment/frontend \
                            --timeout=180s

                        echo "=== Pods Kubernetes ==="
                        kubectl get pods

                        echo "=== Services Kubernetes ==="
                        kubectl get services

                        echo "=== PVC Kubernetes ==="
                        kubectl get pvc
                    '''
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline CI/CD terminé avec succès ! Déploiement Kubernetes effectué.'
        }

        failure {
            echo 'Pipeline CI/CD échoué. Vérifier les logs Jenkins et Kubernetes.'
        }

        always {
            echo 'Pipeline terminé.'
        }
    }
}
