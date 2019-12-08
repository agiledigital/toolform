library 'play26-jenkins-library'

project = "se-swip"
projectShort = "se-swip"

@NonCPS def uniqueBy(List items, String key) {
  return items.unique { a,b -> a[key] <=> b[key]}
}

def notifySlack(String buildStatus = 'STARTED') {
    // Build status of null means success.
    buildStatus = buildStatus ?: 'SUCCESS'

    def color

    if (buildStatus == 'STARTED') {
        color = '#D4DADF'
    } else if (buildStatus == 'SUCCESS') {
        color = '#BDFFC3'
    } else if (buildStatus == 'UNSTABLE') {
        color = '#FFFE89'
    } else {
        color = '#FF9FA1'
    }

    def msg = "${buildStatus}: `${env.JOB_NAME}` #${env.BUILD_NUMBER}: <${env.BUILD_URL}|Build Details>"

    slackSend(channel: "${projectShort}-dev", color: color, message: msg)
}

def volumes = [
  play26BuilderPersistentVolumes(project: "${project}")
].flatten()

def volumeClaims = uniqueBy(volumes, 'path').collect { volume ->
  persistentVolumeClaim(
    claimName: volume.claimName,
    mountPath: volume.path
  )
}

def containers = [
  [containerTemplate(
    name: 'jnlp',
    image: 'openshift/jenkins-slave-base-centos7:v3.7',
    args: '${computer.jnlpmac} ${computer.name}'
  )],
  play26BuilderContainerTemplate()
].flatten()

def buildNumber = env.BUILD_NUMBER;

podTemplate(label: "se-swip-build-pod", cloud: 'openshift', containers: containers, volumes: volumeClaims, runAsUser: '1000') {
  node("se-swip-build-pod") {

    final builds = [:]
    final buildStage = (env.BRANCH_NAME == 'master') ? 'dist' : 'test'
    def gitCommitHash = "unknown"

    builds["Build Play26 Components"] = {
buildPlay26Component(
   baseDir: "some/component",
   project: "${project}",
   component: "some-component",
   buildNumber: buildNumber,
   stage: buildStage
   
   
 )
}

    try {
      stage('Notify Slack') {
        notifySlack()
      }
      stage('Checkout code') {
        checkout scm
      }
      stage('Run parallel builds') {
        parallel(builds)
      }

      if (env.BRANCH_NAME == "master") {
        stage('Trigger staging deployment') {
          archiveArtifacts '.cicd/deploy.yml'
          build job: '../Deploy Staging', parameters: [[$class: 'StringParameterValue', name: 'BUILD_ARTIFACT_NUMBER', value: BUILD_NUMBER]], wait: false
        }
      }
    } catch (InterruptedException e) {
      // Build interupted
      currentBuild.result = "ABORTED"
      throw e
    } catch (e) {
      // If there was an exception thrown, the build failed
      currentBuild.result = "FAILED"
      throw e
    } finally {
      // Success or failure, always send notifications
      notifySlack(currentBuild.result)
    }
  }
}