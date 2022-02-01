import utilities.*

def call(stages){
    def listStagesOrder = [
        'compile': 'sCompile',
        'test': 'sTest',
        'build': 'sBuild',
        'sonar': 'sSonar',
        'curl_spring': 'sCurlSpring',
        'upload_nexus': 'sUploadNexus',
        'dowload_nexus': 'sDownloadNexus',
        'upload_artifact': 'sUploadArtifact',
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
    sUploadArtifact()
    sTestArtifact()
}

def sCompile(){
    stage("Paso 1: Compile"){
        env.STAGE = env.STAGE_NAME
        sh "mvn clean compile -e"
    }
}

def sTest(){
    stage("Paso 2: Test"){
        env.STAGE = env.STAGE_NAME
        sh "mvn clean test -e"
    }
}

def sBuild(){
    stage("Paso 3: Build"){
        env.STAGE = env.STAGE_NAME
        sh "mvn clean test -e"
    }
}

def sSonar(){
    stage("Paso 4: Sonar - Análisis Estático"){
        env.STAGE = env.STAGE_NAME
        sh "echo 'Análisis Estático!'"
        withSonarQubeEnv('sonarqube') {
            sh 'mvn clean verify sonar:sonar -Dsonar.projectKey=ejemplo-gradle -Dsonar.java.binaries=build'
        }
    }
}

def sCurlSpring(){
    stage("Paso 5: Curl Springboot Maven sleep 60"){
        env.STAGE = env.STAGE_NAME
        sh "mvn spring-boot:run &"
        sh "sleep 60 && curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
    }
}

def sUploadNexus(){
    stage("Paso 6: Subir Nexus"){
        env.STAGE = env.STAGE_NAME
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
    stage("Paso 7: Descargar Nexus"){
        env.STAGE = env.STAGE_NAME
        sh ' curl -X GET -u $NEXUS_USER:$NEXUS_PASSWORD "http://nexus:8081/repository/devops-usach-nexus/com/devopsusach2020/DevOpsUsach2020/0.0.1/DevOpsUsach2020-0.0.1.jar" -O'
    }
}

def sUploadArtifact(){
    stage("Paso 8: Levantar Artefacto Jar"){
        env.STAGE = env.STAGE_NAME
        sh 'nohup bash java -jar DevOpsUsach2020-0.0.1.jar & >/dev/null'
    }
}

def sTestArtifact(){
    stage("Paso 9: Testear Artefacto - Dormir(Esperar 60sg)"){
        env.STAGE = env.STAGE_NAME
        sh "sleep 60 && curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
    }
}

return this;