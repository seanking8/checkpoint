pipeline {
    agent any

    environment {
        // This binds the username to GITHUB_USER and the token to GITHUB_TOKEN
        GITHUB_CREDS = credentials('github-token')
    }

    parameters {
        booleanParam(
            name: 'RUN_UI_TESTS',
            defaultValue: false,
            description: 'Run Selenium UI tests'
        )
    }

    stages {
//         stage('Checkout') {
//             steps {
//                 checkout scm
//             }
//         }

        stage('Secure Step') {
            steps {
                sh "echo Token length is \${#GITHUB_CREDS_PSW}"
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean compile'
            }
        }

        stage('Unit & API Tests') {
            steps {
                sh 'mvn test'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('LocalSonar') {
                    sh '''
                      mvn sonar:sonar \
                        -Dsonar.projectKey=checkpoint
                    '''
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 2, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('UI Tests (Selenium)') {
                    when {
                        expression { return params.RUN_UI_TESTS }
                    }
                    steps {
                        // Using -Dsurefire.skip=true ensures unit tests don't run twice
                        sh 'mvn -B verify -Dsurefire.skip=true'
                    }
                    post {
                        always {
                            archiveArtifacts allowEmptyArchive: true, artifacts: 'target/screenshots/**'
                        }
                    }
                }
    }

    post {
        always {
            // Capture both Surefire (Unit) and Failsafe (UI) reports
            junit 'target/surefire-reports/*.xml, target/failsafe-reports/*.xml'
            publishHTML(target: [
                        reportDir: 'target/site/jacoco',
                        reportFiles: 'index.html',
                        reportName: 'JaCoCo Code Coverage',
                        keepAll: true,
                        alwaysLinkToLastBuild: true,
                        allowMissing: true
            ])
        }
        failure {
            emailext(
              subject: "FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
              body: "Build failed.\nURL: ${env.BUILD_URL}",
              to: "a00335602@student.tus.ie"
            )
          }
          unstable {
            emailext(
              subject: "UNSTABLE: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
              body: "Build unstable (tests failing).\nURL: ${env.BUILD_URL}",
              to: "a00335602@student.tus.ie"
            )
          }
    }
}