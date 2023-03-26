@Library('mylib') _

def tools = new org.devops.tools()
env.branchName = branch_name.split("/")[1]

currentBuild.displayName = branchName
currentBuild.description = "build maven"

gitlabCredentialId = "898c942e-e0be-40ec-8493-046efe874e0a"
sonarCredentialId = "62a8afd1-b1bf-4d25-af53-89bd5cb7cc85"
nexusCredentialId = "9a80e424-9b0f-462f-b472-ee29fcc79377"


pipeline {
    agent {
        node { label 'build' }
    }
    stages {
        stage('checkout') {
            steps {
                script {

                    try {
                        tools.Checkout(env.gitUrl, branchName, gitlabCredentialId)
                        println("下载代码完成")
                    } catch(Exception e) {
                            println(e)
                            error "下载代码失败"
                    }
                }
            }
        }
        stage('build') {
            steps {
                script {
                    tools.BuildMethod(env.buildType)
                }
            }
        }

        stage('sonar') {
            steps {
                script {
                    tools.SonarScanner(sonarCredentialId)
                }
            }
        }

        stage('upload nexus') {
            steps {
                script {
                    tools.Nexus(nexusCredentialId)
                }
            }
        }
    }
}
