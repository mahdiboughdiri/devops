
pipeline {
    agent any

    options {
        timeout(time: 20, unit: 'MINUTES')
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
                            echo "$DOCKER_PASSWORD" |
                                docker login \
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

        stage('Deploy') {
            steps {
                sh 'docker compose pull'
                sh 'docker compose up -d'
            }
        }
    }

    post {
        success {
            echo '✅ Pipeline CI/CD terminé avec succès !'
        }

        failure {
            echo '❌ Pipeline CI/CD échoué.'
        }

        always {
            echo 'Pipeline terminé.'
        }
    }
}
