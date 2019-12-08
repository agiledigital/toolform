final buildVersion = "${BUILD_ARTIFACT_NUMBER}"
final releaseTag = "release-${BUILD_NUMBER}"
project = "se-swip"
projectShort = "se-swip"

final components = [
  [component: "some-component" , runner: "play26" ]
]

// You almost certainly do not want to edit anything below this line.

final lastSlashInJobName = JOB_NAME.lastIndexOf('/')
assert lastSlashInJobName > 0
final jenkinsProjectName = JOB_NAME.substring(0, lastSlashInJobName)
final jobName = JOB_NAME.substring(lastSlashInJobName + 1)

final jobNameParts = jobName.toLowerCase().split('[ _\\+\\-]+')

assert jobNameParts[0] == 'deploy'

final environment = jobNameParts[1]

projectEnvironment = "${project}-${environment}"

final generateDockerfile(config) {
  return """
    |FROM ${projectEnvironment}/${config.runner}-runner
    |USER root
    |ENV TZ Australia/NSW
    |COPY artifacts /home/runner/artifacts
    |RUN chmod g+w /etc/passwd
    |RUN chmod 777 /home/runner/artifacts
    |RUN chgrp -Rf root /home/runner/ && chmod -Rf g+w /home/runner/
    |USER runner
    |""".stripMargin()
}

def notifySlack(String buildStatus = 'STARTED', String additionalMsg = '') {
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

    def msg = "${buildStatus}: `${env.JOB_NAME}` #${env.BUILD_NUMBER}: <${env.BUILD_URL}|Deployment Details>${additionalMsg}"

    slackSend(channel: "${projectShort}-dev", color: color, message: msg)
}

def buildComponent = {config ->
  sh "mkdir ${config.component}"
  dir(config.component) {
    sh "tar -xf ../${project}-${config.component}-${buildVersion}.tar.gz"

    if(fileExists('Dockerfile')) {
      sh "cat Dockerfile"
      sh "ls"

      final name = "${project}-${config.component}".replaceAll('[ _\\+\\-]+', '-')

      final imageName = "$name-image"
      final buildName = "$name-build"
      echo "Configuring ImageStream and BuildConfig"

      if (openshift.selector('ImageStream', imageName ).exists()) {
        openshift.apply([
          kind: 'ImageStream',
          metadata: [
            labels: [app: name],
            name: imageName
          ],
          spec: [:]
        ])
      }
      else {
        openshift.create([
          kind: 'ImageStream',
          metadata: [
            labels: [app: name],
            name: imageName
          ],
          spec: [:]
        ])
      }

      if (!(config.runner == null || openshift.selector('ImageStream', "${config.runner}-runner").exists())) {
        openshift.raw(["import-image", "${config.runner}-runner", "--from=agiledigital/${config.runner}-runner", "--confirm=true"])
        openshift.raw(["tag", "--source=docker", "agiledigital/${config.runner}-runner:latest",
                      "${projectEnvironment}/${config.runner}-runner:latest", "--scheduled=true", "--reference-policy=local"])
      }

      if (config.runner != null) {
        openshift.apply([
          kind: 'BuildConfig',
          metadata: [
            labels: [app: name],
            name: buildName
          ],
          spec: [
            strategy:[
              dockerStrategy: [
                forcePull: true,
                from: [
                  kind: 'ImageStreamTag',
                  name: "${config.runner}-runner:latest"
                ]
              ]
            ],
            source: [
              binary: [:]
            ],
            output: [
              to: [kind: 'ImageStreamTag', name: "$imageName:$releaseTag"]
            ]
          ]
        ])
      }
      else {
        openshift.apply([
          kind: 'BuildConfig',
          metadata: [
            labels: [app: name],
            name: buildName
          ],
          spec: [
            strategy:[
              dockerStrategy: [
                forcePull: true
              ]
            ],
            source: [
              binary: [:]
            ],
            output: [
              to: [kind: 'ImageStreamTag', name: "$imageName:$releaseTag"]
            ]
          ]
        ])
      }

      echo 'Starting Build'
      def build = openshift.startBuild(buildName, '--from-dir', '.').object()
      build.metadata.labels['release']=releaseTag
      openshift.apply(build)
    }
    else {
      final componentKey = config.component.replaceAll('[-\\+\\-]+', '_')

      // Upload artifact to S3 bucket.
      s3Upload(
        bucket: "${project}-artifacts",
        path: "${componentKey}/${releaseTag}/",
        file: "../${project}-${config.component}-${buildVersion}.tar.gz")
    }
  }
}

timeout(time: 120, unit: 'MINUTES') {
  node() {
    try {
      stage('Notify Slack') {
        notifySlack()
      }

      stage('Clean workspace') {
        cleanWs()
      }

      stage('Pull build artifacts') {
        copyArtifacts(projectName: "${jenkinsProjectName}/Build/master", selector: specific("${buildVersion}"))
        sh "ls"
      }

      openshift.withCluster() {
        openshift.withProject(projectEnvironment) {
          withAWS(region: 'ap-southeast-2', credentials: 'sourceip-artifact-management') {
            // Components build in parallel on Server, since its async.
            for(config in components) {
              stage("Build component [${config.component}]") {
                buildComponent(config)
              }
            }
          }
          stage('Wait for builds') {
            final buildsSelector = openshift.selector('build', [ release: releaseTag ])

            timeout(time: 15, unit: 'MINUTES') {
                buildsSelector.untilEach(1) {
                  return it.object().status.phase == "Complete" || it.object().status.phase == "Failed"
                }
            }

            def allDone = true
            buildsSelector.withEach {
                if ( it.object().status.phase == "Failed" ) {
                    allDone = false
                }
            }

            return allDone

          }

          stage('Create deployment') {
            if (!(openshift.selector("ConfigMap", 'config-overrides').exists())) {
              openshift.create([
                kind: 'ConfigMap',
                metadata: [
                  name: 'config-overrides'
                ],
                data: [:]
              ])
            }

            final template = readFile file: ".cicd/deploy.yml"

            final processed = openshift.process(template, '-p', "RELEASE_TAG=${releaseTag}", '-p', "ENVIRONMENT=${environment}")

            def filteredProcessed = []
            for ( o in processed ) {
              echo "Creating ${o.metadata.name} ${o}"
              def pvcSelector = openshift.selector("pvc", o.metadata.name)

              // Prepare for the deployment steps for the DC (DeploymentConfig).
              if (o.kind == "DeploymentConfig") {

                // Fetching the existing DC with the resource version and replace it with current DC from deploy.yml.
                if (openshift.selector("dc", o.metadata.name).exists()) {
                  def dc = openshift.selector("dc", o.metadata.name).object()

                  if (o.metadata.labels == null) {
                      o.metadata.labels = [:]
                  }

                  o.metadata.labels[ "release" ] = releaseTag
                  o.metadata.labels[ "environment" ] = environment
                  o.metadata.resourceVersion = dc.metadata.resourceVersion;

                  // Suppress auto triggering of deployment for https://github.com/openshift/origin/issues/18406
                  // So we can manually force in a later step
                  for (t in o.spec.triggers) {
                    if (t.type == "ImageChange" && o.spec?.template?.spec?.containers != null) {
                      for (tc in t.imageChangeParams.containerNames) {
                        for ( oc in o.spec.template.spec.containers ) {
                          if (oc.name == tc) {
                            for ( c in o.spec.template.spec.containers ) {
                              if (c.name == tc) {
                                c.image = "docker-registry.default.svc:5000/${projectEnvironment}/${oc.image}"
                                echo "Switching image of ${o.metadata.name} to [${c.image}]"
                              }
                            }
                          }
                        }
                      }
                    }
                  }

                  openshift.apply(o)

                }
                // Creaing new DC if none exists on the OpenShift.
                else {
                  if (o.metadata.labels == null) {
                      o.metadata.labels = [:]
                  }

                  o.metadata.labels[ "release" ] = releaseTag
                  o.metadata.labels[ "environment" ] = environment

                  // Suppress auto triggering of deployment for https://github.com/openshift/origin/issues/18406
                  // So we can manually force in a later step
                  for (t in o.spec.triggers) {
                    if (t.type == "ImageChange" && o.spec?.template?.spec?.containers != null) {
                      for (tc in t.imageChangeParams.containerNames) {
                        for ( oc in o.spec.template.spec.containers ) {
                          if (oc.name == tc) {
                            if (oc.name == tc) {
                              oc.image = "docker-registry.default.svc:5000/${projectEnvironment}/${oc.image}"
                              echo "Switching image of ${o.metadata.name} to [${oc.image}]"
                            }
                          }
                        }
                      }
                    }
                  }

                  // go fully mannual
                  if (o.spec?.triggers != null) {
                    o.spec.remove("triggers")
                  }

                  openshift.create(o)
                }
              }
              else if (o.kind != "PersistentVolumeClaim" || !pvcSelector.exists()) {
              // Exclude PVCs that already exist. Kubernetes doesnt permit them to be mutated
                 if (openshift.selector(o.kind, o.metadata.name).exists()) {
                   openshift.apply(o)
                 }
                 else {
                   openshift.create(o)
                 }
              }
            }
          }

          stage('Wait for deployments') {

            final dcsSelector = openshift.selector('dc', [ release: releaseTag ])

            timeout(time: 20, unit: 'MINUTES') {
              dcsSelector.withEach {

                // Wait for them all to Complete
                echo "Waiting for the pods of DeploymentConfig ${it.object().metadata.name} to be running."

                it.related('pods').untilEach(1) {
                    return (it.object().status.phase == "Running" && it.object().status.containerStatuses != null)
                }
              }
            }

            echo "Replications Controllers have completed. Validating they are all Active and all is good."
            def allDone = true
            dcsSelector.withEach {
              it.related('rc').withEach {
                echo "Checking for the ReplicationControllers of DeploymentConfig ${it.object().metadata.name} are running."

                if ( it.object().status.phase != "Running" ) {
                    allDone = false
                }
              }
            }

            return allDone
          }

          stage('Show End Points') {

            // Determine the defined end-Points
            // Assume these are public
            final template = readFile file: ".cicd/deploy.yml"
            final processed = openshift.process(template, '-p', "RELEASE_TAG=${releaseTag}", '-p', "ENVIRONMENT=${environment}")
            final routeSelector = openshift.selector('Route')

            def publicroutes = []
            def privateroutes = []

            for ( r in routeSelector.objects() ) {
              def ispublic = false
              for ( pr in processed ) {
                if (pr.kind == "Route" && pr.metadata.name == r.metadata.name) {
                  ispublic = true
                }
              }

              def path = ""
              if (r.spec.path != null) {
                path = r.spec.path
              }

              if (ispublic) {
                publicroutes << "<https://${r.spec.host}${path}|${r.metadata.name}>"
              }
              else {
                privateroutes << "<https://${r.spec.host}${path}|${r.metadata.name}>"
              }
            }

            def additionalMsg = ""

            if (publicroutes.size() > 0) {
              additionalMsg += "\n\nProject External Portals:"
              for (link in publicroutes) {
                additionalMsg += "\n${link}"
              }
            }

            if (privateroutes.size() > 0) {
              additionalMsg += "\n\nPlatform Internal Portals:"
              for (link in privateroutes) {
                additionalMsg += "\n${link}"
              }
            }

            notifySlack(currentBuild.result, additionalMsg)
          }
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
      if (currentBuild.result) {
        notifySlack(currentBuild.result)
      }
    }
  }
}

