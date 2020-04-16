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
        cpu: 20m 
        memory: 512Mi 
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
        stage('Bootstrap') {
            echo sh(script: 'env|sort', returnStdout: true)
        }
        stage('Build Palisade Services') {
            git url: 'https://github.com/gchq/Palisade-services.git'
            sh "git fetch origin develop"
            sh "git checkout ${env.CHANGE_BRANCH} || git checkout ${env.BRANCH_NAME} || git checkout develop"
            container('docker-cmds') {
                configFileProvider([configFile(fileId: "${env.CONFIG_FILE}", variable: 'MAVEN_SETTINGS')]) {
                    sh 'mvn -s $MAVEN_SETTINGS install'
                }
            }
        }
        stage('Install a Maven project') {
            git url: 'https://github.com/gchq/Palisade-integration-tests.git'
            sh "git fetch origin develop"
            sh "git checkout ${env.CHANGE_BRANCH} || git checkout ${env.BRANCH_NAME} || git checkout develop"
            container('docker-cmds') {
                configFileProvider([configFile(fileId: "${env.CONFIG_FILE}", variable: 'MAVEN_SETTINGS')]) {
                    sh 'mvn -s $MAVEN_SETTINGS install'
                }
            }
        }
        stage('Hadolinting') {
            container('hadolint') {
                sh 'hadolint */Dockerfile'
            }
        }
        stage('Do a Palisade') {
            x = env.BRANCH_NAME
            if (x.substring(0, 2) == "PR") {
                dir ('Palisade-common') {
                git url: 'https://github.com/gchq/Palisade-common.git'
                sh "git fetch origin develop"
                sh "git checkout ${env.CHANGE_BRANCH} || git checkout ${env.BRANCH_NAME} || git checkout develop"
                    container('docker-cmds') {
                        configFileProvider([configFile(fileId: "${env.CONFIG_FILE}", variable: 'MAVEN_SETTINGS')]) {
                            sh 'mvn -s $MAVEN_SETTINGS install'
                        }
                    }
                }
                dir ('Palisade-readers') {
                git url: 'https://github.com/gchq/Palisade-readers.git'
                sh "git fetch origin develop"
                sh "git checkout ${env.CHANGE_BRANCH} || git checkout ${env.BRANCH_NAME} || git checkout develop"
                    container('docker-cmds') {
                        configFileProvider([configFile(fileId: "${env.CONFIG_FILE}", variable: 'MAVEN_SETTINGS')]) {
                            sh 'mvn -s $MAVEN_SETTINGS install'
                        }
                    }
                }
                dir ('Palisade-clients') {
                git url: 'https://github.com/gchq/Palisade-clients.git'
                sh "git fetch origin develop"
                sh "git checkout ${env.CHANGE_BRANCH} || git checkout ${env.BRANCH_NAME} || git checkout develop"
                    container('docker-cmds') {
                        configFileProvider([configFile(fileId: "${env.CONFIG_FILE}", variable: 'MAVEN_SETTINGS')]) {
                            sh 'mvn -s $MAVEN_SETTINGS install'
                        }
                    }
                }
                dir ('Palisade-services') {
                git url: 'https://github.com/gchq/Palisade-services.git'
                sh "git fetch origin develop"
                sh "git checkout ${env.CHANGE_BRANCH} || git checkout ${env.BRANCH_NAME} || git checkout develop"
                    container('docker-cmds') {
                        configFileProvider([configFile(fileId: "${env.CONFIG_FILE}", variable: 'MAVEN_SETTINGS')]) {
                            sh 'mvn -s $MAVEN_SETTINGS package'
                        }
                    }
                }
                dir ('Palisade-examples') {
                    git url: 'https://github.com/gchq/Palisade-Examples.git'
                    sh "git fetch origin develop"
                    sh "git checkout ${env.CHANGE_BRANCH} ||  git checkout ${env.BRANCH_NAME} || git checkout develop"
                    container('docker-cmds') {
                        configFileProvider([configFile(fileId: "${env.CONFIG_FILE}", variable: 'MAVEN_SETTINGS')]) {
                            sh '''
                                mvn -s $MAVEN_SETTINGS install -Dmaven.test.skip=true
                                cd ../Palisade-services
                                java -jar -Dspring.profiles.active=discovery,debug services-manager/target/services-manager-*-exec.jar --manager.mode=run && java -jar -Dspring.profiles.active=example,debug services-manager/target/services-manager-*-exec.jar --manager.mode=run
                                cd ../Palisade-examples
                                ./deployment/local-jvm/bash-scripts/configureExamples.sh
                                ./deployment/local-jvm/bash-scripts/runFormattedLocalJVMExample.sh > deployment/local-jvm/bash-scripts/exampleOutput.txt
                                chmod +x deployment/local-jvm/bash-scripts/verify.sh
                            '''
                            sh './deployment/local-jvm/bash-scripts/verify.sh | tail -1 > numOfLines.txt'
                            String numOfLines = readFile 'numOfLines.txt'
                            if (numOfLines.trim().equals("780")){
                                currentBuild.result = 'SUCCESS'
                            } else {
                                error("Number of lines was not 780, but was: ${numOfLines.trim()}")
                            }
                        }
                    }
                }
            }
        }
    }
}
