pipeline {
  agent {
    label 'cloud-platform-cloud-utils'
  }
  parameters {
    string(name: "REGISTRY_NAME", defaultValue: "")
    string(name: "DOCKER_IMAGE", defaultValue: "")
    string(name: "VERSION", defaultValue: "")
  }
  stages {
    stage('Build') {
      steps {
        //sh 'docker build -t sample/web-app:latest .'
        echo "Docker is building image: ${DOCKER_IMAGE}."
      }
    }
    stage('Scan') {
      steps {
        sh "curl -sfL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/install.sh | sh -s -- v0.42.0"
        sh "curl -sfL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/html.tpl > html.tpl"

        script {
          def formatOption = "--format template --template \'@html.tpl\'"
          def imageName = null
          if (params.REGISTRY_NAME == '') {
            imageName = "$DOCKER_IMAGE:$VERSION"
          } else {
            imageName = "$REGISTRY_NAME/$DOCKER_IMAGE:$VERSION"
          }
          echo "Trivy is scanning image: ${DOCKER_IMAGE} for vulnerabilities."
          sh "ls -al"
          sh "./bin/trivy image $imageName $formatOption -o cve_report.html"
          sh "ls -al"
        }

        
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
