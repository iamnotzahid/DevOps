pipeline {
    agent {
        docker { 
            image 'ubuntu:24.04'
            args '-u root -v /var/run/docker.sock:/var/run/docker.sock' 
        }
    }
    stages {
        stage('Install System Tools') {
            steps {
                sh '''
                    apt-get update && apt-get install -y \
                    python3 python3-venv python3-pip \
                    nodejs npm docker.io curl
                '''
            }
        }
        stage('Setup & Install') {
            parallel {
                stage('Backend (Python)') {
                    steps {
                        dir('backend') { // Enter the backend folder
                            sh '''
                                python3 -m venv venv
                                . venv/bin/activate
                                pip install -r requirements.txt
                            '''
                        }
                    }
                }
                stage('Frontend (Next.js)') {
                    steps {
                        dir('frontend') { // Enter the frontend folder
                            sh 'npm install'
                        }
                    }
                }
            }
        }
        stage('Database & Launch') {
            steps {
                // 1. Start Postgres
                sh 'docker run --name pg-db -e POSTGRES_PASSWORD=pass -p 5432:5432 -d postgres'
                
                // 2. Run Backend & Frontend in parallel
                parallel(
                    "Uvicorn": {
                        dir('backend') {
                            sh '. venv/bin/activate && nohup uvicorn main:app --host 0.0.0.0 --port 8000 > backend.log 2>&1 &'
                        }
                    },
                    "NextJS": {
                        dir('frontend') {
                            sh 'nohup npm run dev -- -p 3000 > frontend.log 2>&1 &'
                        }
                    }
                )
            }
        }
    }
    post {
        always {
            sh 'docker stop pg-db && docker rm pg-db || true'
        }
    }
}
