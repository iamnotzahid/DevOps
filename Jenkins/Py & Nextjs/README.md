------------------------------
## Full-Stack Python & Next.js CI/CD Pipeline
This repository contains a decoupled full-stack application featuring a FastAPI/Python backend and a Next.js frontend. It includes a fully automated CI/CD pipeline defined in a Jenkinsfile to handle environment setup, dependency management, and service orchestration using Docker.
## 🚀 Architecture Overview

* Backend: Python 3 (FastAPI/Uvicorn) with SQLAlchemy.
* Frontend: Next.js (Node.js).
* Database: PostgreSQL (Ephemeral Docker container).
* CI/CD: Jenkins Pipeline running inside an Ubuntu 24.04 Docker Agent on AWS EC2.

------------------------------
## 📁 Project Structure

.
├── backend/
│   ├── main.py            # Entry point for Uvicorn
│   ├── requirements.txt   # Python dependencies
│   └── .env               # Database credentials (injected via CI)
├── frontend/
│   ├── package.json       # Node.js dependencies
│   └── src/               # Next.js source code
└── Jenkinsfile            # Pipeline definition

------------------------------
## 🛠 CI/CD Pipeline Stages
The pipeline is designed to be idempotent and isolated, running entirely within a Docker container to ensure environment consistency.

   1. System Preparation: Installs Python, Node.js, and Docker CLI inside the Ubuntu agent.
   2. Parallel Installation:
   * Backend: Creates a venv, upgrades pip, and installs requirements.txt.
      * Frontend: Runs npm install.
   3. Infrastructure & Launch:
   * Spins up a PostgreSQL container on port 5432.
      * Starts the Uvicorn server (:8000).
      * Starts the Next.js dev server (:3000).
   4. Post-Build Cleanup: Automatically stops and removes the PostgreSQL container to free up EC2 resources.

------------------------------
## ⚙️ Setup & Deployment## 1. AWS Security Group Configuration
Ensure your EC2 instance allows inbound traffic on the following ports:

* 8080 (Jenkins UI)
* 8000 (Backend API)
* 3000 (Frontend Web)

## 2. Database Connection
The backend expects a DB_URL environment variable. In this pipeline, it connects to:
postgresql://postgres:pass@localhost:5432/postgres
## 3. Running the Pipeline

   1. Connect your GitHub repository to [Jenkins](https://www.jenkins.io/doc/book/pipeline/syntax/).
   2. Ensure the jenkins user on your EC2 has permission to access the Docker socket:
   
   sudo usermod -aG docker jenkins
   
   3. Click Build Now in the Jenkins UI.

------------------------------
## 📝 Logs & Debugging
Since the services run in the background via nohup, check the following files within the Jenkins workspace for troubleshooting:

* backend/backend.log
* frontend/frontend.log

------------------------------
