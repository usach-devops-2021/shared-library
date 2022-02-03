def call(){
    pipeline {
        agent any
        environment {
          NEXUS_USER         = credentials('nexus_username')
          NEXUS_PASSWORD     = credentials('nexus_password')
          SLACK_TOKEN        = credentials('slack_token')
        }

        triggers {
            GenericTrigger(
                genericVariables: [
                [key: 'ref', value: '$.ref']
                ],
                genericRequestVariables: [
                    [key: 'stages', regexpFilter: '']
                ],
                    causeString: 'Iniciado en $env.GIT_BRANCH',
                token: 'laboratorio-mod3',
                tokenCredentialId: '',
                printContributedVariables: true,
                printPostContent: true,
                silentResponse: false,
                regexpFilterText: '$ref',
                regexpFilterExpression: 'refs/heads/' + BRANCH_NAME
            )
        }

        stages {
            stage("Pipeline"){
                steps {
                    script{
                    env.STAGE  = env.STAGE_NAME
                    //print 'Compile Tool: ' + params.compileTool;
                        if (fileExists('build.gradle')) {
                            sh "echo 'App Gradle'"
                            //gradle.call(env.stages, env.compileTool)
                        } else if(fileExists('pom.xml'))  {
                            sh "echo 'App Maven'"
                            //maven.call(env.stages, env.compileTool)
                        } else {
                            sh "echo 'App sin identificar'"
                            exit 0
                        }

                        def branch = env.GIT_BRANCH;

                        if (branch.startsWith('feature-') || branch == 'develop') {
                            ci.call(env.stages, env.compileTool)
                        } else if (branch.startWith('release-v')) {
                            cd.call(env.stages, env.compileTool)
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
