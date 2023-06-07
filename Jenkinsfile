pipeline {
  agent {
      label 'cloud-platform-cloud-utils'
  }
  stages {
    stage('Build') {
      steps {
        //Build docker image
        //sh 'docker build -t sample/web-app:latest .'
        echo "Docker is pulling down ${DOCKER_IMAGE} with version: ${VERSION}."
        sh "docker pull ${DOCKER_IMAGE}:${VERSION}"
      }
    }
    stage('Scan') {
      steps {
        // Install trivy
        // sh "curl -sfL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/install.sh | sh -s -- -b /usr/local/bin v0.18.3"
        
        echo "Trivy is scanning image: ${DOCKER_IMAGE} for vulnerabilities."
        sh "docker run aquasec/trivy image --severity HIGH,CRITICAL ${DOCKER_IMAGE}:${VERSION}"
      }
    }
  }
}
