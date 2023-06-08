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
        sh "curl -sfL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/install.sh | sh -s -- v0.42.0"
        sh "curl -sfL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/html.tpl > html.tpl"
        sh "./bin/trivy --version"
        
        echo "Trivy is scanning image: ${DOCKER_IMAGE} for vulnerabilities."
        sh "ls -al"
        sh "./bin/trivy image --format template --template \'@html.tpl\' -o cve_report.html --severity HIGH,CRITICAL ${DOCKER_IMAGE}:${VERSION}"
        sh "ls -al"
        
        publishHTML(target: [
          allowMissing: false,
          alwaysLinkToLastBuild: false,
          keepAll: true,
          reportDir: ".",
          reportFiles: "cve_report.html",
          reportName: "Trivy Report",
        ])
      }
      
    }
  }
}
