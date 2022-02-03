import utilities.*

def call(stages){
    def listStagesOrder = [
        'compile': 'sCompile',
        'unitTest': 'sUnitTest',
        'jar': 'sJar',
        'sonar': 'sSonar',
        'nexusUpload': 'sNexusUpload'
        //'gitCreateRelease': 'sGitCreateRelease'
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
    sUnitTest()
    sJar()
    sSonar()
    sNexusUpload()
    //sGitCreateRelease()
}

def sCompile(){
    stage("Paso 1: Compliar"){
      env.TAREA = env.STAGE_NAME
      sh "mvn clean compile -e"
  }

}

def sUnitTest() {
stage("Paso 2: Testear"){
      env.TAREA = env.STAGE_NAME
      sh "mvn clean test -e"
  }

}

def sJar() {
stage("Paso 3: Build .Jar"){
      env.TAREA = env.STAGE_NAME
      sh "mvn clean package -e"
  }

}

def sSonar() {
stage("Paso 4: Sonar - Análisis Estático"){
      env.TAREA = env.STAGE_NAME
      sh "echo 'Análisis Estático!'"
      withSonarQubeEnv('sonarqube') {
          sh 'mvn clean verify sonar:sonar -Dsonar.projectKey=ejemplo-gradle -Dsonar.java.binaries=build'
      }
  }

}

def sNexusUpload() {
stage("Paso 6: Subir Nexus"){
      env.TAREA = env.STAGE_NAME
      nexusPublisher nexusInstanceId: 'nexus',
      nexusRepositoryId: 'devops-usach-nexus',
      packages: [
          [$class: 'MavenPackage',
              mavenAssetList: [
                  [classifier: '',
                  extension: 'jar',
                  filePath: 'build/DevOpsUsach2020-0.0.1.jar'
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

def sGitCreateRelease() {
stage("Paso 6: Release"){
      env.TAREA = env.STAGE_NAME
      sh "git checkout develop && git pull origin develop"
      /*def ret = sh(script: 'git branch | grep -q "release-v2-0-0"; echo $?')
      sh "echo '$ret'"
      if (ret == 0) {
        sh "git branch -D release-v2-0-0"
      }*/
      sh "git checkout -b release-v2-0-0"
      sh "git push origin release-v2-0-0"
  }
}

return this;