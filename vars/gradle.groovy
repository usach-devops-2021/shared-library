import utilities.*

def call(stages){
    def listStagesOrder = [
        'build': 'sBuild',
        'sonar': 'sSonar',
        'curl_spring': 'sCurlSpring',
        'upload_nexus': 'sUploadNexus',
        'dowload_nexus': 'sDownloadNexus',
        'run_artifact': 'sRunArtifact',
        'test_artifact': 'sTestArtifact'
    ]
    def arrayUtils = new array.arrayExtentions();
    def stagesArray = []
        stagesArray = arrayUtils.searchKeyInArray(stages, ";", listStagesOrder)

    if (stagesArray.isEmpty()) {
        echo 'El pipeline se ejecutará completo'
        allStages()
    } else {
        echo 'Stages a ejecutar :' + stages
        stagesArray.each{ stageFunction ->//variable as param
            echo 'Ejecutando ' + stageFunction
            "${stageFunction}"()
        }
    }
}

def allStages(){
    sBuild()
    sSonar()
    sCurlSpring()
    sDownloadNexus()
    sRunArtifact()
    sTestArtifact()
}

def sBuild(){
    stage("Gradle: Build and Test"){
        sh "gradle clean build"
    }
}

def sSonar(){
    stage("Gradle: Sonar - Análisis Estático"){
        sh "echo 'Análisis Estático!'"
        withSonarQubeEnv('sonarqube') {
            sh 'chmod +x gradlew && ./gradlew sonarqube -Dsonar.projectKey=ejemplo-gradle -Dsonar.java.binaries=build'
        }
    }
}

def sCurlSpring(){
    stage("Gradle: Curl Springboot Gradle sleep 60"){
        sh "gradle bootRun&"
        sh "sleep 60 && curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
    }
}

def sUploadNexus(){
    stage("Gradle: Subir Nexus"){
        nexusPublisher nexusInstanceId: 'nexus',
        nexusRepositoryId: 'devops-usach-nexus',
        packages: [
            [$class: 'MavenPackage',
                mavenAssetList: [
                    [classifier: '',
                    extension: '.jar',
                    filePath: 'build/libs/DevOpsUsach2020-0.0.1.jar'
                ]
            ],
                mavenCoordinate: [
                    artifactId: 'DevOpsUsach2020',
                    groupId: 'com.devopsusach2020',
                    packaging: 'jar',
                    version: '0.0.1'
                ]
            ]
        ]
    }
}

def sDownloadNexus(){
    stage("Gradle: Descargar Nexus"){
        sh ' curl -X GET -u $NEXUS_USER:$NEXUS_PASSWORD "http://nexus:8081/repository/devops-usach-nexus/com/devopsusach2020/DevOpsUsach2020/0.0.1/DevOpsUsach2020-0.0.1.jar" -O'
    }
}

def sRunArtifact(){
    stage("Gradle: Levantar Artefacto Jar"){
        sh 'nohup bash java -jar DevOpsUsach2020-0.0.1.jar & >/dev/null'
    }
}

def sTestArtifact(){
    stage("Gradle: Testear Artefacto - Dormir(Esperar 60sg)"){
        sh "sleep 60 && curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
    }
}

return this;
