import utilities.*

def call(stages){
    def listStagesOrder = [
        'compile': 'sCompile',
        'test': 'sTest',
        'build': 'sBuild',
        'sonar': 'sSonar',
        'run': 'sRun'
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
    sCompile()
    sTest()
    sBuild()
    sSonar()
    sCurlSpring()
    sDownloadNexus()
    sRunArtifact()
    sTestArtifact()
}

def sCompile(){
    stage("Maven: Compile"){
        sh "mvn clean compile -e"
    }
}

def sTest(){
    stage("Maven: Test"){
        sh "mvn clean test -e"
    }
}

def sBuild(){
    stage("Maven: Build"){
        sh "mvn clean test -e"
    }
}

def sSonar(){
    stage("Maven: Sonar - Análisis Estático"){
        sh "echo 'Análisis Estático!'"
        withSonarQubeEnv('sonarqube') {
            sh 'mvn clean verify sonar:sonar -Dsonar.projectKey=ejemplo-gradle -Dsonar.java.binaries=build'
        }
    }
}

def sCurlSpring(){
    stage("Maven: Curl Springboot Maven sleep 60"){
        sh "mvn spring-boot:run &"
        sh "sleep 60 && curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
    }
}

def sUploadNexus(){
    stage("Maven: Subir Nexus"){
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
    stage("Maven: Descargar Nexus"){
        sh ' curl -X GET -u $NEXUS_USER:$NEXUS_PASSWORD "http://nexus:8081/repository/devops-usach-nexus/com/devopsusach2020/DevOpsUsach2020/0.0.1/DevOpsUsach2020-0.0.1.jar" -O'
    }
}

def sRunArtifact(){
    stage("Maven: Levantar Artefacto Jar"){
        sh 'nohup bash java -jar DevOpsUsach2020-0.0.1.jar & >/dev/null'
    }
}

def sTestArtifact(){
    stage("Maven: Testear Artefacto - Dormir(Esperar 60sg)"){
        sh "sleep 60 && curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
    }
}

return this;
