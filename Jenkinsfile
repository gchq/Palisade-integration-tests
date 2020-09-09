/*
 * Copyright 2020 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

timestamps {

podTemplate(yaml: '''
apiVersion: v1
kind: Pod
metadata: 
    name: dind 
spec:
  affinity:
    nodeAffinity:
      preferredDuringSchedulingIgnoredDuringExecution:
      - weight: 1
        preference:
          matchExpressions:
          - key: palisade-node-name
            operator: In
            values: 
            - node1
            - node2
            - node3
  containers:
  - name: jnlp
    image: jenkins/jnlp-slave
    imagePullPolicy: Always
    args: 
    - $(JENKINS_SECRET)
    - $(JENKINS_NAME)
    resources:
      requests:
        ephemeral-storage: "4Gi"
      limits:
        ephemeral-storage: "8Gi"

  - name: docker-cmds
    image: 779921734503.dkr.ecr.eu-west-1.amazonaws.com/jnlp-did:200608
    imagePullPolicy: IfNotPresent
    command:
    - sleep
    args:
    - 99d
    env:
      - name: DOCKER_HOST
        value: tcp://localhost:2375
    resources:
      requests:
        ephemeral-storage: "4Gi"
      limits:
        ephemeral-storage: "8Gi"
  
  - name: hadolint
    image: hadolint/hadolint:v1.18.0-6-ga0d655d-alpine@sha256:e0f960b5acf09ccbf092ec1e8f250cd6b5c9a586a0e9783c53618d76590b6aec
    imagePullPolicy: IfNotPresent
    command:
        - cat
    tty: true  
    resources:
      requests:
        ephemeral-storage: "1Gi"
      limits:
        ephemeral-storage: "2Gi"

  - name: dind-daemon
    image: docker:1.12.6-dind
    imagePullPolicy: IfNotPresent
    resources:
      requests:
        cpu: 20m
        memory: 512Mi
    securityContext:
      privileged: true
    volumeMounts:
      - name: docker-graph-storage
        mountPath: /var/lib/docker
    resources:
      requests:
        ephemeral-storage: "1Gi"
      limits:
        ephemeral-storage: "2Gi"

  - name: maven
    image: 779921734503.dkr.ecr.eu-west-1.amazonaws.com/jnlp-dood-new-infra:200608
    imagePullPolicy: IfNotPresent
    command: ['docker', 'run', '-p', '80:80', 'httpd:latest']
    tty: true
    volumeMounts:
      - mountPath: /var/run
        name: docker-sock
    resources:
      requests:
        ephemeral-storage: "4Gi"
      limits:
        ephemeral-storage: "8Gi"

  volumes:
    - name: docker-graph-storage
      emptyDir: {}
    - name: docker-sock
      hostPath:
         path: /var/run
''') {
    node(POD_LABEL) {
        def GIT_BRANCH_NAME

        stage('Bootstrap') {
            if (env.CHANGE_BRANCH) {
                GIT_BRANCH_NAME=env.CHANGE_BRANCH
            } else {
                GIT_BRANCH_NAME=env.BRANCH_NAME
            }
            echo sh(script: 'env | sort', returnStdout: true)
        }

        stage('Prerequisites') {
            dir('Palisade-common') {
                git branch: 'develop', url: 'https://github.com/gchq/Palisade-common.git'
                if (sh(script: "git checkout ${GIT_BRANCH_NAME}", returnStatus: true) == 0) {
                    container('docker-cmds') {
                        configFileProvider([configFile(fileId: "${env.CONFIG_FILE}", variable: 'MAVEN_SETTINGS')]) {
                            sh 'mvn -s $MAVEN_SETTINGS install -P quick'
                        }
                    }
                }
            }
            dir('Palisade-readers') {
                git branch: 'develop', url: 'https://github.com/gchq/Palisade-readers.git'
                if (sh(script: "git checkout ${GIT_BRANCH_NAME}", returnStatus: true) == 0) {
                    container('docker-cmds') {
                        configFileProvider([configFile(fileId: "${env.CONFIG_FILE}", variable: 'MAVEN_SETTINGS')]) {
                            sh 'mvn -s $MAVEN_SETTINGS install -P quick'
                        }
                    }
                }
            }
            dir('Palisade-clients') {
                git branch: 'develop', url: 'https://github.com/gchq/Palisade-clients.git'
                if (sh(script: "git checkout ${GIT_BRANCH_NAME}", returnStatus: true) == 0) {
                    container('docker-cmds') {
                        configFileProvider([configFile(fileId: "${env.CONFIG_FILE}", variable: 'MAVEN_SETTINGS')]) {
                            sh 'mvn -s $MAVEN_SETTINGS install -P quick'
                        }
                    }
                }
            }
            dir('Palisade-services') {
                git branch: 'develop', url: 'https://github.com/gchq/Palisade-services.git'
                // Checkout services if a similarly-named branch exists
                // If this is a PR, a example smoke-test will be run, so checkout services develop if no similarly-named branch was found
                // This will be needed to build the jars
                if (sh(script: "git checkout ${GIT_BRANCH_NAME}", returnStatus: true) == 0 || (env.BRANCH_NAME.substring(0, 2) == "PR" && sh(script: "git checkout develop", returnStatus: true) == 0)) {
                    container('docker-cmds') {
                        configFileProvider([configFile(fileId: "${env.CONFIG_FILE}", variable: 'MAVEN_SETTINGS')]) {
                            sh 'mvn -s $MAVEN_SETTINGS install -P quick'
                        }
                    }
                }
            }
            dir('Palisade-HR-data-gen') {
                git branch: 'develop', url: 'https://github.com/gchq/Palisade-examples.git'
                if (sh(script: "git checkout ${GIT_BRANCH_NAME}", returnStatus: true) == 0 || (env.BRANCH_NAME.substring(0, 2) == "PR" && sh(script: "git checkout develop", returnStatus: true) == 0)) {
                    container('docker-cmds') {
                        configFileProvider([configFile(fileId: "${env.CONFIG_FILE}", variable: 'MAVEN_SETTINGS')]) {
                            sh 'mvn -s $MAVEN_SETTINGS install'
                        }
                    }
                }
            }
        }

        stage('Integration Tests, Checkstyle') {
            dir('Palisade-integration-tests') {
                git branch: GIT_BRANCH_NAME, url: 'https://github.com/gchq/Palisade-integration-tests.git'
                container('docker-cmds') {
                    configFileProvider([configFile(fileId: "${env.CONFIG_FILE}", variable: 'MAVEN_SETTINGS')]) {
                        sh 'mvn -s $MAVEN_SETTINGS install -Dmaven.test.skip=true'
                    }
                }
            }
        }

        stage('Hadolinting') {
            dir("Palisade-integration-tests") {
                container('hadolint') {
                    sh 'hadolint */Dockerfile'
                }
            }
        }

        stage('Run the JVM Example') {
            // Always run some sort of End-to-End test if this is a Pull Request or from develop or main
            if (env.BRANCH_NAME.substring(0, 2) == "PR" || env.BRANCH_NAME == "develop" || env.BRANCH_NAME == "main") {
                // If this branch name exists in examples, use that
                // Otherwise, default to examples/develop
                dir ('Palisade-examples') {
                    git branch: 'develop', url: 'https://github.com/gchq/Palisade-examples.git'
                    sh(script: "git checkout ${GIT_BRANCH_NAME}", returnStatus: true)
                    container('docker-cmds') {
                        configFileProvider([configFile(fileId: "${env.CONFIG_FILE}", variable: 'MAVEN_SETTINGS')]) {
                            sh 'mvn -s $MAVEN_SETTINGS install -P quick'
                            sh '''
                                bash deployment/local-jvm/example-model/startServices.sh
                                bash deployment/local-jvm/example-model/runFormattedLocalJVMExample.sh | tee deployment/local-jvm/example-model/exampleOutput.txt
                                bash deployment/local-jvm/example-model/stopServices.sh
                            '''
                            sh 'bash deployment/local-jvm/example-model/verify.sh'
                        }
                    }
                }
            }
        }

        stage('Run the K8s Example') {
             dir('Palisade-examples') {
                 container('docker-cmds') {
                     def GIT_BRANCH_NAME_LOWER = GIT_BRANCH_NAME.toLowerCase().take(24)
                     sh "palisade-login"
                     sh "\$(aws ecr get-login --no-include-email --region eu-west-1) > /dev/null"
                     sh "aws eks update-kubeconfig --name pipeline-eks-cluster --region eu-west-1"
                     sh "kubectl delete ns ${GIT_BRANCH_NAME_LOWER} || true"
                     sh "kubectl delete pv palisade-classpath-jars-example-${GIT_BRANCH_NAME_LOWER} || true"
                     sh "kubectl delete pv palisade-data-store-${GIT_BRANCH_NAME_LOWER} || true"
                     //sh "kubectl describe clusterrole.rbac || true"
                     sh "kubectl auth can-i create pvc"
                     //sh "kubectl get pvc --all-namespaces"
                     sh "ls"
                     sh "pwd"
                     sh "helm dep up"
                     if (sh(script: "helm install palisade ." +
                             " --set global.hosting=aws" +
                             " --set global.repository=${ECR_REGISTRY}" +
                             " --set global.hostname=${EGRESS_ELB}" +
                             " --set global.persistence.classpathJars.aws.volumeHandle=${VOLUME_HANDLE_CLASSPATH_JARS}" +
                             " --set global.persistence.dataStores.palisade-data-store.aws.volumeHandle=${VOLUME_HANDLE_DATA_STORE} " +
                             " --set global.persistence.storageClassDeploy=true " +
                             " --set global.persistence.dataStores.palisade-data-store.local.hostPath=\$(pwd)/resources/data," +
                             " --set global.persistence.classpathJars.local.hostPath=\$(pwd)/deployment/target" +
                             " --set global.deployment=example" +
                             " --namespace ${GIT_BRANCH_NAME_LOWER} --create-namespace", returnStatus: true) == 0) {
                         echo("successfully deployed")
                         sleep(time: 2, unit: 'MINUTES')
                         sh "kubectl get pod --namespace=${GIT_BRANCH_NAME_LOWER} && kubectl describe pod --namespace=${GIT_BRANCH_NAME_LOWER}"
                         sh "kubectl get pvc --namespace=${GIT_BRANCH_NAME_LOWER} && kubectl describe pvc --namespace=${GIT_BRANCH_NAME_LOWER}"
                         sh "kubectl get pv  --namespace=${GIT_BRANCH_NAME_LOWER} && kubectl describe pv  --namespace=${GIT_BRANCH_NAME_LOWER}"
                         sh "kubectl get sc  --namespace=${GIT_BRANCH_NAME_LOWER} && kubectl describe pv  --namespace=${GIT_BRANCH_NAME_LOWER}"
                         sh "helm delete palisade --namespace ${GIT_BRANCH_NAME_LOWER}"
                         } else {
                            error("Build failed because of failed helm install")
                         }
                     }
                 }
             }
        }
    }
}
