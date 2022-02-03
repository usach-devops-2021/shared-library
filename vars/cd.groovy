import utilities.*

def call(stages){
    def listStagesOrder = [
        'gitDiff': 'sGitDiff',
        'nexusDownload': 'sNexusDownload',
        'run': 'sRun',
        'test': 'sTest',
        'gitMergeMaster': 'sGitMergeMaster',
        'gitMergeDevelop': 'sGitMergeDevelop',
        'gitTagMaster': 'sGitTagMaster'
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
    sGitDiff()
    sNexusDownload()
    sRun()
    sTest() 
    sGitMergeMaster()
    sGitMergeDevelop()
    sGitTagMaster()
}

def sGitDiff() {
stage("Paso 7: Git Diff"){
    def branch = env.GIT_BRANCH
      env.TAREA = env.STAGE_NAME
      sh "DIFERENCIAS ${branch} VS MAIN:"
      sh "git diff ${branch}..main"
  }

}

def sNexusDownload() {
stage("Paso 8: Descargar Nexus"){
      env.TAREA = env.STAGE_NAME
      sh ' curl -X GET -u $NEXUS_USER:$NEXUS_PASSWORD "http://nexus:8081/repository/devops-usach-nexus/com/devopsusach2020/DevOpsUsach2020/0.0.1/DevOpsUsach2020-0.0.1.jar" -O'
  }

}

def sRun() {

  stage("Paso 9: Levantar Artefacto Jar"){
      env.TAREA = env.STAGE_NAME
      sh 'nohup bash java -jar DevOpsUsach2020-0.0.1.jar & >/dev/null'
  }

}

def sTest() {

  stage("Paso 10: Testear Artefacto - Dormir(Esperar 20sg) "){
      env.TAREA = env.STAGE_NAME
      sh "sleep 20 && curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
  }

}

def sGitMergeMaster() {

stage("Paso 11: Git Merge Master"){
    branch = env.GIT_BRANCH
      env.TAREA = env.STAGE_NAME
      sh "git checkout main && git pull origin main"
      sh "git merge --no-ff ${branch}"
      sh "git push origin main"
  }
}

def sGitMergeDevelop() {
stage("Paso 12: Git Merge Develop"){
    branch = env.GIT_BRANCH
      env.TAREA = env.STAGE_NAME
      sh "git checkout main && git pull origin main"
      sh "git merge --no-ff ${branch}"
      sh "git push origin main"
  }

}

def sGitTagMaster() {

stage("Paso 13: Git Merge Develop"){
    env = env.GIT_BRANCH
      env.TAREA = env.STAGE_NAME
      sh "git checkout main && git pull origin main"
      sh "git tag ${branch.substring(9)}"
      sh "git push origin ${branch.substring(9)}"
  }
}


def sBuild() {
    stage("Paso 1: Build && Test"){
        env.TAREA = env.STAGE_NAME
        sh "gradle clean build"
    }

}
    
def sSonar() {
    stage("Paso 2: Sonar - Análisis Estático"){
        env.TAREA = env.STAGE_NAME
        sh "echo 'Análisis Estático!'"
        withSonarQubeEnv('sonarqube') {
            sh './gradlew sonarqube -Dsonar.projectKey=ejemplo-gradle -Dsonar.java.binaries=build'
        }
    }

}

def sCurl() {
    stage("Paso 3: Curl Springboot Gradle sleep 20"){
        env.TAREA = env.STAGE_NAME
        sh "gradle bootRun&"
        sh "sleep 20 && curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
    }

}
    
def sNexusUpload() {
    stage("Paso 4: Subir Nexus"){
        env.TAREA = env.STAGE_NAME
        nexusPublisher nexusInstanceId: 'nexus',
        nexusRepositoryId: 'devops-usach-nexus',
        packages: [
            [$class: 'MavenPackage',
                mavenAssetList: [
                    [classifier: '',
                    extension: 'jar',
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

def sNexusDownload() {
    stage("Paso 5: Descargar Nexus"){
        env.TAREA = env.STAGE_NAME
        sh ' curl -X GET -u $NEXUS_USER:$NEXUS_PASSWORD "http://nexus:8081/repository/devops-usach-nexus/com/devopsusach2020/DevOpsUsach2020/0.0.1/DevOpsUsach2020-0.0.1.jar" -O'
    }
}
    
def sJar() {
    stage("Paso 6: Levantar Artefacto Jar"){
        env.TAREA = env.STAGE_NAME
        sh 'nohup bash java -jar DevOpsUsach2020-0.0.1.jar & >/dev/null'
    }
}
    
def sTest() {
    stage("Paso 7: Testear Artefacto - Dormir(Esperar 20sg) "){
        env.TAREA = env.STAGE_NAME
        sh "sleep 20 && curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
    }
}

return this;