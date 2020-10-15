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
        image: 779921734503.dkr.ecr.eu-west-1.amazonaws.com/jnlp-dood-new-infra:200710
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
            def GIT_BRANCH_NAME_LOWER
            def SERVICES_REVISION
            def COMMON_REVISION
            def READERS_REVISION
            def CLIENTS_REVISION
            def EXAMPLES_REVISION
            def INTEGRATION_REVISION
            def IS_PR
            def FEATURE_BRANCH
            def DEPLOY_EXAMPLES_IMAGES
            def DEPLOY_SERVICES_IMAGES

            stage('Bootstrap') {
                if (env.CHANGE_BRANCH) {
                    GIT_BRANCH_NAME=env.CHANGE_BRANCH
                    IS_PR = "true"
                } else {
                    GIT_BRANCH_NAME=env.BRANCH_NAME
                    IS_PR = "false"
                }
                // set default values for the variables
                DEPLOY_EXAMPLES_IMAGES = "false"
                DEPLOY_SERVICES_IMAGES = "false"
                FEATURE_BRANCH = "true"
                COMMON_REVISION = "SNAPSHOT"
                READERS_REVISION = "SNAPSHOT"
                CLIENTS_REVISION = "SNAPSHOT"
                EXAMPLES_REVISION = "SNAPSHOT"
                SERVICES_REVISION = "SNAPSHOT"
                GIT_BRANCH_NAME_LOWER = GIT_BRANCH_NAME.toLowerCase().take(7)
                HELM_DEPLOY_NAMESPACE = GIT_BRANCH_NAME_LOWER + "-example"
                INTEGRATION_REVISION = "BRANCH-${GIT_BRANCH_NAME_LOWER}-SNAPSHOT"
                // update values for the variables if this is the develop branch build
                if ("${env.BRANCH_NAME}" == "develop") {
                    INTEGRATION_REVISION = "SNAPSHOT"
                    HELM_DEPLOY_NAMESPACE = "dev-example"
                    FEATURE_BRANCH = "false"
                }
                // update values for the variables if this is the main branch build
                if ("${env.BRANCH_NAME}" == "main") {
                    SERVICES_REVISION = "RELEASE"
                    COMMON_REVISION = "RELEASE"
                    READERS_REVISION = "RELEASE"
                    CLIENTS_REVISION = "RELEASE"
                    EXAMPLES_REVISION = "RELEASE"
                    INTEGRATION_REVISION = "RELEASE"
                    HELM_DEPLOY_NAMESPACE = "release-example"
                    FEATURE_BRANCH = "false"
                }
                echo sh(script: 'env | sort', returnStdout: true)
            }

            stage('Prerequisites') {
                container('docker-cmds') {
                    configFileProvider([configFile(fileId: "${env.CONFIG_FILE}", variable: 'MAVEN_SETTINGS')]) {
                        if (FEATURE_BRANCH == "true") {
                            dir('Palisade-common') {
                                git branch: 'develop', url: 'https://github.com/gchq/Palisade-common.git'
                                if (sh(script: "git checkout ${GIT_BRANCH_NAME}", returnStatus: true) == 0) {
                                    COMMON_REVISION = "BRANCH-${GIT_BRANCH_NAME_LOWER}-SNAPSHOT"
                                }
                            }

                            dir('Palisade-readers') {
                                git branch: 'develop', url: 'https://github.com/gchq/Palisade-readers.git'
                                if (sh(script: "git checkout ${GIT_BRANCH_NAME}", returnStatus: true) == 0) {
                                    READERS_REVISION = "BRANCH-${GIT_BRANCH_NAME_LOWER}-SNAPSHOT"
                                } else {
                                     if (COMMON_REVISION == "BRANCH-${GIT_BRANCH_NAME_LOWER}-SNAPSHOT") {
                                         READERS_REVISION = "BRANCH-${GIT_BRANCH_NAME_LOWER}-SNAPSHOT"
                                         sh "mvn -s ${MAVEN_SETTINGS} -P quick -D revision=${READERS_REVISION} -D common.revision=${COMMON_REVISION} deploy"
                                     }
                                }
                            }

                            dir('Palisade-clients') {
                                git branch: 'develop', url: 'https://github.com/gchq/Palisade-clients.git'
                                if (sh(script: "git checkout ${GIT_BRANCH_NAME}", returnStatus: true) == 0) {
                                    CLIENTS_REVISION = "BRANCH-${GIT_BRANCH_NAME_LOWER}-SNAPSHOT"
                                } else {
                                     if (READERS_REVISION == "BRANCH-${GIT_BRANCH_NAME_LOWER}-SNAPSHOT") {
                                         CLIENTS_REVISION = "BRANCH-${GIT_BRANCH_NAME_LOWER}-SNAPSHOT"
                                         sh "mvn -s ${MAVEN_SETTINGS} -P quick -D revision=${CLIENTS_REVISION} -D common.revision=${COMMON_REVISION} -D readers.revision=${READERS_REVISION} deploy"
                                     }
                                }
                            }
                        }

                        dir('Palisade-examples') {
                            git branch: 'develop', url: 'https://github.com/gchq/Palisade-examples.git'
                            if (sh(script: "git checkout ${GIT_BRANCH_NAME}", returnStatus: true) == 0 && FEATURE_BRANCH == "true") {
                                EXAMPLES_REVISION = "BRANCH-${GIT_BRANCH_NAME_LOWER}-SNAPSHOT"
                                // do an install now ready for the JVM end to end test if we are not doing the full deploy
                                sh "mvn -s ${MAVEN_SETTINGS} -P pi -D maven.test.skip=true -D dockerfile.skip=true -D revision=${EXAMPLES_REVISION} -D common.revision=${COMMON_REVISION} -D readers.revision=${READERS_REVISION} -D clients.revision=${CLIENTS_REVISION} install"
                            } else {
                                if (CLIENTS_REVISION == "BRANCH-${GIT_BRANCH_NAME_LOWER}-SNAPSHOT") {
                                    EXAMPLES_REVISION = "BRANCH-${GIT_BRANCH_NAME_LOWER}-SNAPSHOT"
                                    DEPLOY_EXAMPLES_IMAGES = "true"
                                } else {
                                    // do an install now ready for the JVM end to end test if we are not doing the full deploy
                                    sh "mvn -s ${MAVEN_SETTINGS} -P pi -D maven.test.skip=true -D dockerfile.skip=true -D revision=${EXAMPLES_REVISION} -D common.revision=${COMMON_REVISION} -D readers.revision=${READERS_REVISION} -D clients.revision=${CLIENTS_REVISION} install"
                                }
                            }
                        }

                        dir('Palisade-services') {
                            git branch: 'develop', url: 'https://github.com/gchq/Palisade-services.git'
                            if (sh(script: "git checkout ${GIT_BRANCH_NAME}", returnStatus: true) == 0 && FEATURE_BRANCH == "true") {
                                SERVICES_REVISION = "BRANCH-${GIT_BRANCH_NAME_LOWER}-SNAPSHOT"
                                // do an install now ready for the JVM end to end test if we are not doing the full deploy
                                sh "mvn -s ${MAVEN_SETTINGS} -P pi -D maven.test.skip=true -D dockerfile.skip=true -D revision=${SERVICES_REVISION} -D common.revision=${COMMON_REVISION} -D readers.revision=${READERS_REVISION} -D examples.revision=${EXAMPLES_REVISION} install"
                            } else {
                                if (READERS_REVISION == "BRANCH-${GIT_BRANCH_NAME_LOWER}-SNAPSHOT") {
                                    SERVICES_REVISION = "BRANCH-${GIT_BRANCH_NAME_LOWER}-SNAPSHOT"
                                    DEPLOY_SERVICES_IMAGES = "true"
                                } else {
                                    // do an install now ready for the JVM end to end test if we are not doing the full deploy
                                    sh "mvn -s ${MAVEN_SETTINGS} -P pi -D maven.test.skip=true -D dockerfile.skip=true -D revision=${SERVICES_REVISION} -D common.revision=${COMMON_REVISION} -D readers.revision=${READERS_REVISION} -D examples.revision=${EXAMPLES_REVISION} install"
                                }
                            }
                        }
                    }
                }
            }

            parallel DeployExample: {
                stage('Deploy Example') {
                    container('maven') {
                        configFileProvider([configFile(fileId: "${env.CONFIG_FILE}", variable: 'MAVEN_SETTINGS')]) {
                            if (DEPLOY_EXAMPLES_IMAGES == "true" && (IS_PR == "true" || FEATURE_BRANCH == "false")) {
                                sh 'palisade-login'
                                dir("Palisade-examples") {
                                    sh "mvn -s ${MAVEN_SETTINGS} -P pi -D maven.test.skip=true -D revision=${EXAMPLES_REVISION} -D common.revision=${COMMON_REVISION} -D readers.revision=${READERS_REVISION} -D clients.revision=${CLIENTS_REVISION} deploy"
                                }
                            }
                        }
                    }
                }
            },

            DeployServices: {
                stage('Deploy Services') {
                    container('maven') {
                        configFileProvider([configFile(fileId: "${env.CONFIG_FILE}", variable: 'MAVEN_SETTINGS')]) {
                            if (DEPLOY_SERVICES_IMAGES == "true" && (IS_PR == "true" || FEATURE_BRANCH == "false")) {
                                sh 'palisade-login'
                                dir("Palisade-services") {
                                    sh "mvn -s ${MAVEN_SETTINGS} -P pi -D maven.test.skip=true -D revision=${EXAMPLES_REVISION} -D common.revision=${COMMON_REVISION} -D readers.revision=${READERS_REVISION} -D clients.revision=${CLIENTS_REVISION} deploy"
                                }
                            }
                        }
                    }
                }
            }

            parallel RunJvm: {
                stage('Run the JVM Example') {
                    container('docker-cmds') {
                        configFileProvider([configFile(fileId: "${env.CONFIG_FILE}", variable: 'MAVEN_SETTINGS')]) {
                            // Always run some sort of smoke test if this is a Pull Request or from develop or main
                            if (IS_PR == "true" || FEATURE_BRANCH == "false") {
                                // If this branch name exists in examples, use that
                                // Otherwise, default to examples/develop
                                dir ('Palisade-examples') {
                                    sh 'bash deployment/local-jvm/example-model/startServices.sh'
                                    sh 'bash deployment/local-jvm/example-model/runFormattedLocalJVMExample.sh | tee deployment/local-jvm/example-model/exampleOutput.txt'
                                    sh 'bash deployment/local-jvm/example-model/stopServices.sh'
                                    sh 'bash deployment/local-jvm/example-model/verify.sh'
                                }
                            }
                        }
                    }
                }
            },

            RunK8s: {
                stage('Run the K8s Example') {
                    container('maven') {
                        if (IS_PR == "true" || FEATURE_BRANCH == "false") {
                            dir('Palisade-examples') {
                                sh "palisade-login"
                                sh 'extract-addresses'
                                if (sh(script: "kubectl get ns ${HELM_DEPLOY_NAMESPACE}", returnStatus: true) == 0) {
                                    sh "kubectl delete ns ${HELM_DEPLOY_NAMESPACE}"
                                }
                                if (sh(script: "namespace-create ${HELM_DEPLOY_NAMESPACE}", returnStatus: true) == 0) {
                                    if (sh(script: "bash deployment/aws-k8s/example-model/deployServicesToK8s.sh -n ${HELM_DEPLOY_NAMESPACE} -r ${ECR_REGISTRY} -h ${EGRESS_ELB} -d ${VOLUME_HANDLE_DATA_STORE} -c ${VOLUME_HANDLE_CLASSPATH_JARS}", returnStatus:
                                    true) == 0) {
                                        echo("successfully deployed")
                                        sleep(time: 90, unit: 'SECONDS')
                                        sh "bash deployment/aws-k8s/example-model/runFormattedK8sExample.sh ${HELM_DEPLOY_NAMESPACE}"
                                        sh "bash deployment/aws-k8s/example-model/verify.sh ${HELM_DEPLOY_NAMESPACE}"
                                        if (FEATURE_BRANCH == "true") {
                                            sh "kubectl delete ns ${HELM_DEPLOY_NAMESPACE}"
                                        }
                                    } else {
                                        echo("failed to deploy")
                                        sleep(time: 1, unit: 'MINUTES')
                                        sh "kubectl get pvc -n ${HELM_DEPLOY_NAMESPACE}"
                                        sh "kubectl get all -n ${HELM_DEPLOY_NAMESPACE}"
                                        sh "kubectl describe all -n ${HELM_DEPLOY_NAMESPACE}"
                                        error("Build failed because of failed helm deploy")
                                    }
                                } else {
                                    error("Failed to create namespace")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
