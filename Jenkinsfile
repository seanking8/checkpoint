pipeline {
    agent any

    environment {
        // Jenkins credentials (Secret Text) used by application.yml: ${DB_USERNAME} / ${DB_PASSWORD}
        DB_USERNAME = credentials('checkpoint-db-username')
        DB_PASSWORD = credentials('checkpoint-db-password')
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

        stage('Build, Unit Tests & Coverage Check') {
            steps {
                sh 'mvn -B clean verify'
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

        stage('API Tests (Karate)') {
            steps {
                sh 'mvn -B verify -Papi-tests'
            }
        }

        stage('UI Tests (Selenium)') {
                    when {
                        expression { return params.RUN_UI_TESTS }
                    }
                    steps {
                        sh 'mvn -B verify -Pui-tests'
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