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

podTemplate(yaml: '''
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: docker-cmds
    image: jnlp-did:jdk11
    imagePullPolicy: Never
    command:
    - sleep
    args:
    - 99d
    env:
      - name: DOCKER_HOST
        value: tcp://localhost:2375
  - name: hadolint
    image: hadolint/hadolint:latest-debian@sha256:15016b18964c5e623bd2677661a0be3c00ffa85ef3129b11acf814000872861e
    imagePullPolicy: Always
    command:
    - cat
    tty: true
  - name: docker-daemon
    image: docker:19.03.1-dind
    securityContext:
      privileged: true
    resources: 
      requests: 
        cpu: 500m
        memory: 2Gi
    volumeMounts: 
      - name: docker-graph-storage 
        mountPath: /var/lib/docker 
    env:
      - name: DOCKER_TLS_CERTDIR
        value: ""
        
  - name: maven
    image: jnlp-slave-palisade:jdk11
    imagePullPolicy: Never
    command: ['cat']
    tty: true
    env:
    - name: TILLER_NAMESPACE
      value: tiller
    - name: HELM_HOST
      value: :44134
    volumeMounts:
      - mountPath: /var/run
        name: docker-sock
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
                git url: 'https://github.com/gchq/Palisade-common.git'
                if (sh(script: "git checkout ${GIT_BRANCH_NAME}", returnStatus: true) == 0) {
                    container('docker-cmds') {
                        configFileProvider([configFile(fileId: "${env.CONFIG_FILE}", variable: 'MAVEN_SETTINGS')]) {
                        }
                    }
                }
            }
            dir('Palisade-readers') {
                git url: 'https://github.com/gchq/Palisade-readers.git'
                if (sh(script: "git checkout ${GIT_BRANCH_NAME}", returnStatus: true) == 0) {
                    container('docker-cmds') {
                        configFileProvider([configFile(fileId: "${env.CONFIG_FILE}", variable: 'MAVEN_SETTINGS')]) {
                        }
                    }
                }
            }
            dir('Palisade-services') {
                git url: 'https://github.com/gchq/Palisade-services.git'
                if (sh(script: "git checkout ${GIT_BRANCH_NAME}", returnStatus: true) == 0) {
                    container('docker-cmds') {
                        configFileProvider([configFile(fileId: "${env.CONFIG_FILE}", variable: 'MAVEN_SETTINGS')]) {
                        }
                    }
                }
            }
        }

        stage('Integration Tests, Checkstyle') {
            dir('Palisade-integration-tests') {
                git url: 'https://github.com/gchq/Palisade-integration-tests.git'
                sh "git checkout ${GIT_BRANCH_NAME}"
                container('docker-cmds') {
                    configFileProvider([configFile(fileId: "${env.CONFIG_FILE}", variable: 'MAVEN_SETTINGS')]) {
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
        stage('Do a Palisade') {
            x = env.BRANCH_NAME
            if (x.substring(0, 2) != "PR") {
                dir ('Palisade-common') {
                git url: 'https://github.com/gchq/Palisade-common.git'
                sh "git fetch origin develop"
                sh "git checkout ${GIT_BRANCH_NAME}"
                    container('docker-cmds') {
                        configFileProvider([configFile(fileId: "${env.CONFIG_FILE}", variable: 'MAVEN_SETTINGS')]) {
                            sh 'mvn -s $MAVEN_SETTINGS install -P quick'
                        }
                    }
                }
                dir ('Palisade-readers') {
                git url: 'https://github.com/gchq/Palisade-readers.git'
                sh "git fetch origin develop"
                sh "git checkout PAL-390-resource-service-persistence || git checkout ${GIT_BRANCH_NAME}"
                    container('docker-cmds') {
                        configFileProvider([configFile(fileId: "${env.CONFIG_FILE}", variable: 'MAVEN_SETTINGS')]) {
                            sh 'mvn -s $MAVEN_SETTINGS install -P quick'
                        }
                    }
                }
                dir ('Palisade-clients') {
                git url: 'https://github.com/gchq/Palisade-clients.git'
                sh "git fetch origin develop"
                sh "git checkout PAL-390-resource-service-persistence || git checkout ${GIT_BRANCH_NAME}"
                    container('docker-cmds') {
                        configFileProvider([configFile(fileId: "${env.CONFIG_FILE}", variable: 'MAVEN_SETTINGS')]) {
                            sh 'mvn -s $MAVEN_SETTINGS install -P quick'
                        }
                    }
                }
                dir ('Palisade-services') {
                git url: 'https://github.com/gchq/Palisade-services.git'
                sh "git fetch origin develop"
                sh "git checkout ${GIT_BRANCH_NAME}"
                    container('docker-cmds') {
                        configFileProvider([configFile(fileId: "${env.CONFIG_FILE}", variable: 'MAVEN_SETTINGS')]) {
                            sh 'mvn -s $MAVEN_SETTINGS install -P quick'
                        }
                    }
                }
                dir ('Palisade-examples') {
                    git url: 'https://github.com/gchq/Palisade-Examples.git'
                    sh "git fetch origin develop"
                    sh "git checkout ${GIT_BRANCH_NAME}"
                    container('docker-cmds') {
                        configFileProvider([configFile(fileId: "${env.CONFIG_FILE}", variable: 'MAVEN_SETTINGS')]) {
                            sh '''
                                mvn -s $MAVEN_SETTINGS install -P quick
                                cd ../Palisade-examples
                            '''
                            sh './deployment/local-jvm/bash-scripts/startServices.sh'
                            sh './deployment/local-jvm/bash-scripts/configureExamples.sh'
                            sh './deployment/local-jvm/bash-scripts/runLocalJVMExample.sh | tee deployment/local-jvm/bash-scripts/exampleOutput.txt'
                            sh './deployment/local-jvm/bash-scripts/verify.sh | tail -1 > numOfLines.txt'
                            String numOfLines = readFile 'numOfLines.txt'
                            if (numOfLines.trim().equals("183")){
                                currentBuild.result = 'SUCCESS'
                            } else {
                                error("Number of lines was not 183, but was: ${numOfLines.trim()}")
                            }
                        }
                    }
                }
            }
        }
    }
}
