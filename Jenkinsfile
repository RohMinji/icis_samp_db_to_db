// 개별 환경에 맞는 변수를 선언한다.    
def PROJECT_NAME = "ICIS-SAMP-BATCH-DB-TO-DB-DAEMON"
def GIT_OPS_NAME = "ICIS-SAMP-BATCH-DB-TO-DB-DAEMON-GITOPS"
def gitOpsUrl = "gitlab.dspace.kt.co.kr/icis-tr/sa/icis-tlc-template/${GIT_OPS_NAME}.git"
def gitLabOrigin = "gitlab.dspace.kt.co.kr/icis-tr/sa/icis-tlc-template/${PROJECT_NAME}.git"
def gitLabUrl = "https://${gitLabOrigin}"
def NEXUS_URL = 'https://nexus.dspace.kt.co.kr'
def gitLabAccessToken = "1d_as_85bNmPMk1RojLg"
def TAG = getTag()
def ENV = getENV()
def BRANCH = "develop"
 
 
/////////////////////////////
pipeline {
         
    agent {
        docker {
            image 'nexus.dspace.kt.co.kr/argocd/icistr-sa-build-tools:v1.0.4'
            args '-u root:root -v /var/run/docker.sock:/var/run/docker.sock'
            registryUrl 'https://nexus.dspace.kt.co.kr'
            registryCredentialsId 'icistr-sa-nexus'
            reuseNode true
        }
    }
 
    stages {
        stage('Build') {
            steps {
                script{
                    // git branch: opsBranch, credentialsId: "SA-CICD-TEST", url: gitLabUrl
                    docker.withRegistry(NEXUS_URL, "icistr-sa-nexus") {
                        configFileProvider([configFile(fileId: 'icis-tr-maven_setting', variable: 'maven_settings', targetLocation: '/home/jenkins/.m2/settings.xml')]) {
                            sh  """
                                pwd
                                chmod 777 ./mvnw
                                echo 'TAG => ' ${TAG}
                                echo 'ENV => ' ${ENV}
                                skaffold build -p ${ENV} -t ${TAG}
                            """
                        }
                    }
                }
            }
        }
 
        stage('GitOps update') {
            steps{
                print "======kustomization.yaml tag update====="
                script{
                    sh """  
                        cd ~
                        rm -rf ./${GIT_OPS_NAME}
                        git clone https://gitlab-ci-token:${gitLabAccessToken}@${gitOpsUrl}
                        cd ./${GIT_OPS_NAME}
                        git checkout ${BRANCH}
                        kustomize edit set image nexus.dspace.kt.co.kr/argocd/${PROJECT_NAME}:${TAG}
                        git config --global user.email "sa-admin@icistr.com"
                        git config --global user.name "sa-admin"
                        git add .
                        git commit -am 'update image tag ${TAG}'
                        git push origin ${BRANCH}
                    """
                }
                print "git push finished !!!"
            }
        }
    }
}
 
def getTag(){
     
    def TAG
    def opsBranch
    DATETIME_TAG = new Date().format('yyyyMMddHHmmss') 
       
    opsBranch = "${params.Branch ? params.Branch.replaceFirst(/^.*\//,'') : 'dev'}"
    TAG = "${opsBranch}-${DATETIME_TAG}"
 
    return TAG
}
 
def getENV(){
     
    def deployENV
    def opsBranch
     
    opsBranch = "${params.Branch ? params.Branch.replaceFirst(/^.*\//,'') : 'dev' }"
    deployENV = "${opsBranch != 'dev' ? opsBranch.replaceAll(/\-.*/,'') : 'dev' }"
 
    return deployENV
}
