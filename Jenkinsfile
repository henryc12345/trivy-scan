pipeline {
  agent {
    label 'cloud-platform-cloud-utils'
  }
  parameters {
    string(name: "REGISTRY_NAME", defaultValue: "")
    string(name: "DOCKER_IMAGE", defaultValue: "")
    string(name: "VERSION", defaultValue: "")
    string(name: "SEVERITY", defaultValue: "critical")
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
            def format = "--format template --template \'@html.tpl\'"
            def imageName = null
            def sev = "${SEVERITY}".toUpperCase()
            // def slackWebhook = env.appsbroker-slack-webhook
            env.SLACK_WEBHOOK = ''
          
            if (params.REGISTRY_NAME == '') {
                imageName = "$DOCKER_IMAGE:$VERSION"
            } else {
                imageName = "$REGISTRY_NAME/$DOCKER_IMAGE:$VERSION"
            }
            echo "severity level input: $sev"
            echo "Trivy is scanning image: ${DOCKER_IMAGE} for vulnerabilities."
            sh "./bin/trivy image $imageName $format -o cve_report.html"
          
            publishHTML(target: [
                allowMissing: false,
                alwaysLinkToLastBuild: false,
                keepAll: true,
                reportDir: ".",
                reportFiles: "cve_report.html",
                reportName: "Trivy Report",
            ])
        
            // sh "./bin/trivy image --exit-code 1 --severity $sev $imageName"
        
            withCredentials([string(credentialsId: 'appsbroker-slack-webhook', variable: 'SLACK_WEBHOOK')]) {
                script {
                    sh """
                        curl -XPOST -H 'Content-type: application/json' --data '{
                            \"text\": \"Hello from Jenkins!\",
                            \"channel\": \"#appsbroker-alerts\"
                        }' \${SLACK_WEBHOOK}
                    """
                }
            }
        }
        
      }
      
    }
  }
}
