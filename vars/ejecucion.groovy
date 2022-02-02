def call(){
    pipeline {
        agent any
        environment {
          NEXUS_USER         = credentials('nexus_username')
          NEXUS_PASSWORD     = credentials('nexus_password')
          SLACK_TOKEN        = credentials('slack_token')
        }
        parameters {
            choice(
                name:'compileTool',
                choices: ['Maven', 'Gradle'],
                description: 'Seleccione herramienta de compilacion'
            )
            string(
                name:'stages',
                description: 'Ingrese los stages para ejecutar',
                trim: true
            )
        }
        stages {
            stage("Pipeline"){
                steps {
                    script{
                    env.STAGE  = env.STAGE_NAME
                    print 'Compile Tool: ' + params.compileTool;
                    switch(params.compileTool)
                        {
                            case 'Maven':
                                figlet  "Maven"
                                maven.call(params.stages)
                            break;
                            case 'Gradle':
                                figlet  "Gradle"
                                gradle.call(params.stages)
                            break;
                        }
                    }
                }
            }
        }
        post {
            success{
                slackSend color: 'good', message: "[Grupo 3][${JOB_NAME}][${params.compileTool}] Ejecución Exitosa.", teamDomain: 'dipdevopsusac-tr94431', tokenCredentialId: 'slack_token'
            }
            failure{
                slackSend color: 'danger', message: "[Grupo 3][${JOB_NAME}][${params.compileTool}] Ejecucion fallida en stage [${env.STAGE}]", teamDomain: 'dipdevopsusac-tr94431', tokenCredentialId: 'slack_token'
            }
        }
    }
}

return this;
