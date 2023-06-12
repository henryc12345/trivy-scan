def shell(cmd) {
    sh('#!/bin/sh -e\n' + cmd)
}

def runTrivyScan(String dockerImage, String version, String severity, String jenkinsUrl) {
  def dockerImageFull = "$dockerImage:$version"
  def sev = "${severity}".toUpperCase()
  env.SLACK_WEBHOOK = ''
  def updatedBuildUrl = BUILD_URL.replaceFirst('https://green-1-core-services.quantexa.com/jenkins/', "$JENKINS_URL")
  
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
        
}
