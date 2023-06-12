pipeline {
  agent {
    label 'cloud-platform-cloud-utils'
  }
  parameters {
    string(name: "DOCKER_IMAGE", defaultValue: "", description: 'Name of Docker image.')
    string(name: "VERSION", defaultValue: "", description: 'Version or tag of docker image.')
    string(name: "SEVERITY", defaultValue: "critical", description: 'Severity level of scanned image to be alerted.')
    string(name: "JENKINS_URL", defaultValue: "https://green-1-odin.quantexa.com/jenkins/", description: 'Jenkins URL.')
  }
  environment {
    DOCKER_IMAGE_WITH_VERSION = "$DOCKER_IMAGE:$VERSION"
    BUILD_URL = "${env.BUILD_URL}"
}
  stages {
    stage('Build') {
      steps {
        //sh 'docker build -t $DOCKER_IMAGE:$VERSION .'
        echo "Docker is building image: $DOCKER_IMAGE_WITH_VERSION."
      }
    }
    stage('Scan') {
      steps {
        sh "curl -sfL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/install.sh | sh -s -- v0.42.0"
        sh "curl -sfL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/html.tpl > html.tpl"

        script {
            //def format = "--format template --template \'@html.tpl\'"
            def sev = "${SEVERITY}".toUpperCase()
            env.SLACK_WEBHOOK = ''
            def updatedBuildUrl = BUILD_URL.replaceFirst('https://green-1-core-services.quantexa.com/jenkins/', "$JENKINS_URL")
            
            echo "Trivy is scanning and generating report for image: ${DOCKER_IMAGE_WITH_VERSION} for vulnerabilities."
            sh "./bin/trivy image $DOCKER_IMAGE_WITH_VERSION --format template --template \'@html.tpl\' -o cve_report.html"
          
            
            publishHTML(target: [
                allowMissing: false,
                alwaysLinkToLastBuild: false,
                keepAll: true,
                reportDir: ".",
                reportFiles: "cve_report.html",
                reportName: "Trivy Report",
            ])
        
            
            def trivyCommand = "./bin/trivy image --exit-code 1 --severity ${sev} ${DOCKER_IMAGE_WITH_VERSION}"
            def trivyOutput = sh(returnStatus: true, script: trivyCommand)
            
            if (trivyOutput != 0) {

                
                def slackMessage = """
            {
                "text": "High or critical vulnerabilities found by Trivy scan for ${DOCKER_IMAGE_WITH_VERSION}!",
                "channel": "#appsbroker-alerts",
                "attachments": [
                    {
                        "title": "Trivy Output Report",
                        "text": "${updatedBuildUrl}"
                    }
                ]
            }
            """
            
            
            echo "Updated Build URL: ${updatedBuildUrl}"
            


            withCredentials([string(credentialsId: 'appsbroker-slack-webhook', variable: 'SLACK_WEBHOOK')]) {
                script {
                
                    sh """
                        curl -X POST \
                        -H 'Content-type: application/json' \
                        --data '${slackMessage}' \
                        \${SLACK_WEBHOOK}
                    """
                } 
            }
            
            // Fail pipeline if severity specified is found in image.
            sh "$trivyCommand"
            

        }
        }
      }
      
    }
  }
}
