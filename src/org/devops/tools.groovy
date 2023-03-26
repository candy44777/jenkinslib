package org.devops

def Checkout(url, branch, credentialId) {
    checkout([$class: 'GitSCM',
    branches: [[name: branch ]],
    extensions: [],
    userRemoteConfigs:
    [
        [credentialsId: credentialId,
        url: url]
    ]])
}

def QyWechat(user, phone, url) {
    qyWechatNotification failNotify: true,
    mentionedId: user,
    mentionedMobile: phone,
    moreInfo: "gitlab地址: $url",
    onlyFailSendQyWechatNotify: true,
    webhookUrl: 'https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=a74c8a83-392c-4432-9241-2aca96f17da9'
}

def BuildMethod(buildType) {
     switch(buildType){
            case "maven":
                sh """ /usr/local/maven/bin/mvn clean package -DskipTests """
                break;
            case "gradle":
                sh """ gradle build -x test """
                break;
            case "golang":
                sh """ go build main.go """
                break;
            case "npm":

                break;

            case "yarn":

                break;
            default:
                error "buildType choice error!"
                break;
    }
}

def SonarScanner(credentialId) {
    gitAddr = env.gitUrl - ".git"
    withSonarQubeEnv(credentialsId: credentialId) {
        sh """
            /usr/local/bin/sonar-scanner \
            -Dsonar.projectKey="${JOB_NAME}" \
            -Dsonar.login=${SONAR_AUTH_TOKEN} \
            -Dsonar.projectVersion=${env.branchName} \
            -Dsonar.host.url=http://172.17.15.105:9000 \
            -Dsonar.ws.timeout=30 \
            -Dsonar.projectDescription="my first project!" \
            -Dsonar.links.homepage="${gitAddr}" \
            -Dsonar.links.ci="${JOB_URL}" \
            -Dsonar.sources=src \
            -Dsonar.sourceEncoding=UTF-8 \
            -Dsonar.java.binaries=target/classes \
            -Dsonar.java.test.binaries=target/test-classes \
            -Dsonar.java.surefire.report=target/surefire-reports
        """
    }
}

def Nexus(credentialId) {
    commitId = sh(returnStdout: true, script: 'git rev-parse --short HEAD').trim()
    POM = readMavenPom file: 'pom.xml'
    pkgName = "${POM.artifactId}-${POM.version}.${POM.packaging}"
    appName = JOB_NAME.split("_")[0]
    appVersion = "${branchName}-${commitId}"
    newPkgName = "${appName}-${appVersion}.${POM.packaging}"
    sh "mv target/${pkgName} target/${newPkgName}"
    println(pkgName)
    if ( pkgName.contains("SNAPSHOT") ) {
        repository = "snapshot"
    }else {
        repository = "release"
    }

    nexusArtifactUploader artifacts: [
        [
            artifactId: POM.artifactId,
            classifier: '',
            file: "target/$newPkgName",
            type: POM.packaging
        ]
    ],
    credentialsId: credentialId,
    groupId: POM.groupId,
    nexusUrl: '172.17.15.105:8081',
    nexusVersion: 'nexus3',
    protocol: 'http',
    repository: "devops-maven-${repository}",
    version: POM.version
}