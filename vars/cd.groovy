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

def sGitDiff(){
    stage("gitDiff"){
        env.STAGE = env.STAGE_NAME
    }
}

def sNexusDownload(){
    stage("nexusDownload"){
        env.STAGE = env.STAGE_NAME
    }
}

def sRun(){
    stage("run"){
        env.STAGE = env.STAGE_NAME
    }
}

def sTest(){
    stage("test"){
        env.STAGE = env.STAGE_NAME
    }
}

def gitMergeMaster(){
    stage("sGitMergeMaster"){
        env.STAGE = env.STAGE_NAME
    }
}

def gitMergeDevelop(){
    stage("sGitMergeDevelop"){
        env.STAGE = env.STAGE_NAME
    }
}

def gitTagMaster(){
    stage("sGitTagMaster"){
        env.STAGE = env.STAGE_NAME
    }
}