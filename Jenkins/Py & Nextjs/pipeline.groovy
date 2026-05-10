pipeline {
    agent {
        docker { 
            image 'ubuntu:24.04'
            // Maps the host docker socket so we can run the DB container regardless of host OS
            args '-u root -v /var/run/docker.sock:/var/run/docker.sock' 
        }
    }
    environment {
        // Universal DB config
        DB_URL = "postgresql://postgres:pass@localhost:5432/postgres"
    }
    stages {
        stage('Environment Audit') {
            steps {
                // This checks what is available on the "anywhere" host
                sh 'python3 --version && node -v && npm -v'
            }
        }
        stage('Install Dependencies') {
            parallel {
                stage('Backend') {
                    steps {
                        dir('backend') {
                            sh '''
                                python3 -m venv venv
                                . venv/bin/activate
                                pip install -r requirements.txt
                            '''
                        }
                    }
                }
                stage('Frontend') {
                    steps {
                        dir('frontend') {
                            sh 'npm install'
                        }
                    }
                }
            }
        }
        stage('Service Orchestration') {
            steps {
                // Kill old containers if they exist to avoid "name already in use" errors on different hosts
                sh 'docker rm -f pg-db || true'
                
                // Spin up Database
                sh 'docker run --name pg-db -e POSTGRES_PASSWORD=pass -p 5432:5432 -d postgres'
                
                parallel(
                    "Backend": {
                        dir('backend') {
                            sh '. venv/bin/activate && nohup uvicorn main:app --host 0.0.0.0 --port 8000 > backend.log 2>&1 &'
                        }
                    },
                    "Frontend": {
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
            echo "Deployment cleanup..."
            // sh 'docker stop pg-db && docker rm pg-db'
        }
    }
}
