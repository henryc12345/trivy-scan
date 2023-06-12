def shell(cmd) {
    sh('#!/bin/sh -e\n' + cmd)
}

def runTrivyScan(String dockerImage, String version, String severity, String jenkinsUrl) {
  def dockerImageFull = "$dockerImage:$version"
  def severityUpperCase = "${severity}".toUpperCase()
  def BUILD_URL = "${env.BUILD_URL}"
  env.SLACK_WEBHOOK = ''
  def updatedBuildUrl = BUILD_URL.replaceFirst('https://green-1-core-services.quantexa.com/jenkins/', "$jenkinsUrl")
  def trivyCommand = "./bin/trivy image --exit-code 1 --severity ${severityUpperCase} ${dockerImageFull}"
  def trivyOutput = sh(returnStatus: true, script: trivyCommand)
  
  // Install Trivy and HTML template
  shell "curl -sfL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/install.sh | sh -s -- v0.42.0"
  shell "curl -sfL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/html.tpl > html.tpl"
  
  // Trivy Scanning Container Image and generating HTML report
  shell "./bin/trivy image $dockerImageFull --format template --template \'@html.tpl\' -o cve_report.html"
  
  // Publish HTML report to Jenkins UI
  publishHTML(target: [
    allowMissing: false,
    alwaysLinkToLastBuild: false,
    keepAll: true,
    reportDir: ".",
    reportFiles: "cve_report.html",
    reportName: "Trivy Report",
  ])
    
  // If specified severity is found in the Container Image then send slack notification and fail pipeline  
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

    sh "$trivyCommand"
}
    
def call(String dockerImage, String version, String severity, String jenkinsUrl) {
    runTrivyScan(dockerImage, version, severity, jenkinsUrl)
}
