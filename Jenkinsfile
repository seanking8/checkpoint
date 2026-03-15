pipeline {
    agent any

    environment {
            GITHUB_TOKEN = credentials('github-token')
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
                sh 'echo "Token length is ${#GITHUB_TOKEN}"'
            }
        }

        stage('Build and Test') {
            steps {
                // This runs unit tests (Surefire) and packages the JAR
                sh 'mvn clean package'
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
        }
    }
}