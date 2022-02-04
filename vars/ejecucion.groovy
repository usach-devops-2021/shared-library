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
                    env.tipoPipeline = "";
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
                            env.tipoPipeline = "CI";
                            ci.call(env.stages, env.compileTool)
                        } else if (branch.startWith('release-v')) {
                            env.tipoPipeline = "CD";
                            cd.call(env.stages, env.compileTool)
                        }
                    }
                }
            }
        }
        post {
            success{
                slackSend color: 'good', teamDomain: 'dipdevopsusac-tr94431', channel: "#lab-pipeline-mod3-seccion3-status", message: "[Grupo3][${env.tipoPipeline}][Rama: ${env.GIT_BRANCH}][Stage: ${env.STAGE}][Resultado: OK]"
            }
            failure{
                slackSend color: 'danger', teamDomain: 'dipdevopsusac-tr94431', channel: "#lab-pipeline-mod3-seccion3-status", message: "[Grupo3][${env.tipoPipeline}][Rama: ${env.GIT_BRANCH}][Stage: ${env.STAGE}][Resultado: No OK]"
            }
        }
    }
}

return this;
