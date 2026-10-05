pipeline {
    agent any

    options {
        timeout(time: 20, unit: 'MINUTES')
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

        stage('Unit Tests') {
            steps {
                dir('backend') {
                    sh 'mvn test'
                }
            }
            post {
                always {
                    junit 'backend/target/surefire-reports/*.xml'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                dir('backend') {
                    withSonarQubeEnv('SonarQube') {
                        sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar'
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
                sh 'docker build -t mehdiboughdiri/appgestion-backend:latest ./backend'
                sh 'docker build -t mehdiboughdiri/appgestion-frontend:latest ./frontend'
            }
        }

        stage('Docker Push') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKER_USERNAME',
                    passwordVariable: 'DOCKER_PASSWORD'
                )]) {
                    sh 'echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin'
                    sh 'docker push mehdiboughdiri/appgestion-backend:latest'
                    sh 'docker push mehdiboughdiri/appgestion-frontend:latest'
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
