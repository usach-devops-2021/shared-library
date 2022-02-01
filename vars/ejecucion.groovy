/*

    forma de invocación de método call:

    def ejecucion = load 'script.groovy'
    ejecucion.call()

*/

def call(){
  
  pipeline {
      agent any
      environment {
          NEXUS_USER         = credentials('nexus_username')
          NEXUS_PASSWORD     = credentials('nexus_password')
          SLACK_TOKEN        = credentials('slack_token')
      }
      parameters {
          choice  name: 'compileTool', choices: ['Gradle', 'Maven'], description: 'Seleccione el empaquetador maven/gradle'
      }
      stages {
          stage("Pipeline"){
              steps {
                  script{
                      // params.compileTool
                      sh "env"
                      switch(params.compileTool)
                      {
                          case 'Maven':
                              echo "Maven"
                            //   def ejecucion = load 'maven.groovy'
                            //   ejecucion.call()
                            maven.call()
                          break;
                          case 'Gradle':
                            //   def ejecucion = load 'gradle.groovy'
                            //   ejecucion.call()
                            gradle.call()
                          break;
                      }
                  }
              }
            //   post {
            //       always {
            //           sh "echo 'fase always executed post'"
            //       }

            //       success {
            //           sh "echo 'fase success'"
            //       }

            //       failure {
            //           sh "echo 'fase failure'"
            //       }
            //   }
             post{
                success{
                    slackSend color: 'good', message: "[rootchile] [${JOB_NAME}] [${BUILD_TAG}] Ejecucion Exitosa", teamDomain: 'dipdevopsusac-tr94431', tokenCredentialId: "${SLACK_TOKEN}"
                }
                failure{
                    slackSend color: 'danger', message: "[rootchile] [${env.JOB_NAME}] [${BUILD_TAG}] Ejecucion fallida en stage", teamDomain: 'dipdevopsusac-tr94431', tokenCredentialId: "${SLACK_TOKEN}"
                }
            }
          }
      }
  }

}

return this;
