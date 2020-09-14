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

        stage('Bootstrap') {
            if (env.CHANGE_BRANCH) {
                GIT_BRANCH_NAME=env.CHANGE_BRANCH
            } else {
                GIT_BRANCH_NAME=env.BRANCH_NAME
            }
            echo sh(script: 'env | sort', returnStdout: true)
        }
        stage('Run the K8s Example') {
            dir('Palisade-examples') {
                 container('maven') {
                    configFileProvider([configFile(fileId: "${env.CONFIG_FILE}", variable: 'MAVEN_SETTINGS')]) {
                        git branch: '${GIT_BRANCH_NAME}', url: 'https://github.com/gchq/Palisade-examples.git'
                        sh "mvn -s ${MAVEN_SETTINGS} install -Dmaven.test.skip=true"
                    }
                }
            }

            dir('Palisade-services') {
                container('maven') {
                    configFileProvider([configFile(fileId: "${env.CONFIG_FILE}", variable: 'MAVEN_SETTINGS')]) {
                        git branch: '${GIT_BRANCH_NAME}', url: 'https://github.com/gchq/Palisade-services.git'
                        sh "mvn -s ${MAVEN_SETTINGS} install -Dmaven.test.skip=true"
                    }
                }
            }
            dir('Palisade-examples') {
                 container('maven') {
                        def GIT_BRANCH_NAME_LOWER = GIT_BRANCH_NAME.toLowerCase().take(24)

                        sh "palisade-login"
                        sh 'extract-addresses'
                        sh "kubectl delete ns ${GIT_BRANCH_NAME_LOWER} || true"
                        sh "kubectl delete pv palisade-classpath-jars-example-${GIT_BRANCH_NAME_LOWER} || true"
                        sh "kubectl delete pv palisade-data-store-${GIT_BRANCH_NAME_LOWER} || true"
                        if (sh(script: "namespace-create ${GIT_BRANCH_NAME_LOWER}", returnStatus: true) == 0) {
                           //sh 'bash deployment/local-k8s/example-model/deployServicesToK8s.sh'
                           sh 'helm list --all'
                           sh 'kubectl get pods --all-namespaces'
                           sh "helm version"
                           sh 'ls ..'
                           sh 'ls'
                           sh 'ls charts/'
                           sh "helm dep up --debug"
                           sh 'ls charts/'

                           if (sh(script: "helm upgrade --install palisade . " +
                                    "--set global.hosting=aws  " +
                                    "--set traefik.install=false,dashboard.install=false " +
                                    "--set global.repository=${ECR_REGISTRY} " +
                                    "--set global.hostname=${EGRESS_ELB} " +
                                    "--set global.deployment=example " +
                                    "--set global.persistence.dataStores.palisade-data-store.aws.volumeHandle=${VOLUME_HANDLE_DATA_STORE} " +
                                    "--set global.persistence.classpathJars.aws.volumeHandle=${VOLUME_HANDLE_CLASSPATH_JARS} " +
                                    "--set global.redisClusterEnabled=false " +
                                    "--set global.redis.install=false " +
                                    "--set global.redis-cluster.install=false " +
                                    "--set global.persistence.dataStores.palisade-data-store.local.hostPath=\$(pwd)/resources/data" +
                                    "--set global.persistence.classpathJars.local.hostPath=\$(pwd)/deployment/target" +
                                    "--namespace ${GIT_BRANCH_NAME_LOWER}", returnStatus: true) == 0) {
                                echo("successfully deployed")
                           } else {
                              error("Helm deploy failed")
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