import utilities.*

def call(stages, compileTool){
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
    stage("Compliar"){
      env.STAGE = env.STAGE_NAME
      sh "mvn clean compile -e"
  }

}

def sUnitTest() {
stage("Testear"){
      env.STAGE = env.STAGE_NAME
      sh "mvn clean test -e"
  }

}

def sJar() {
stage("Build .Jar"){
      env.STAGE = env.STAGE_NAME
      sh "mvn clean package -e"
  }

}

def sSonar() {
stage("Sonar - Análisis Estático"){
      env.STAGE = env.STAGE_NAME
      GIT_REPO_NAME = env.GIT_URL.replaceFirst(/^.*\/([^\/]+?).git$/, '$1')
      sh "echo 'Análisis Estático!'"
      withSonarQubeEnv('sonarqube') {
          sh "mvn clean verify sonar:sonar -Dsonar.projectKey=${GIT_REPO_NAME}-${env.GIT_BRANCH}-${env.BUILD_NUMBER} -Dsonar.java.binaries=build"
      }
  }

}

def sNexusUpload() {
stage("Subir Nexus"){
      env.STAGE = env.STAGE_NAME
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
stage("Git Create Release"){
      env.STAGE = env.STAGE_NAME
      sh "git checkout develop && git pull origin develop"
      sh "git checkout -b release-v2-0-0"
      sh "git push origin release-v2-0-0"
  }
}

return this;
