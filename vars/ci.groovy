import utilities.*

def call(stages){
    def listStagesOrder = [
        'compile': 'sCompile',
        'unitTest': 'sUnitTest',
        'sonar': 'sSonar',
        'nexusUpload': 'sNexusUpload',
        'gitCreateRelease': 'sGitCreateRelease'
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
    sSonar();
    sNexusUpload()
    sGitCreateRelease()
}

def sCompile(){
    stage("compile"){
        env.STAGE = env.STAGE_NAME
    }
}

def sUnitTest(){
    stage("unitTest"){
        env.STAGE = env.STAGE_NAME
    }
}

def sSonar(){
    stage("sonar"){
        env.STAGE = env.STAGE_NAME
    }
}

def sNexusUpload(){
    stage("nexusUpload"){
        env.STAGE = env.STAGE_NAME
    }
}

def sGitCreateRelease(){
    stage("gitCreateRelease"){
        env.STAGE = env.STAGE_NAME
    }
}