node {
    try {
        stage('Prepare') {
            deleteDir()
            checkout scm
            buildImage = docker.build("build-changelog", "ci")
        }

        stage('Git changelog generation') {
            buildImage.inside() {
                if (env.BRANCH_NAME == 'master') {
                    sh "git config --global user.email 'no-email-jenkins@alpiq.com'; git config --global user.name 'Jenkins'"
                    sh "git remote -v"

                    // Get previous tag
                    def gitTagPrevious = sh(script: "git describe --abbrev=0 --tags `git rev-list --tags --skip=1 --max-count=1`", returnStdout: true).trim()
                    echo "Previous Tag ${previous_tag}"
                    sh "mkdir -p ~/.ssh"

                    // Generate changelog
                    def changelog = sh(script: "gitchangelog ${gitTagPrevious} >> CHANGELOG_TEST.md", returnStdout: true).trim()

                    // Commit changelog
                    sshagent (credentials: ['XXX_Credential_to_Bitbucket']) {
                        sh("git add CHANGELOG_TEST.md")
                        sh("git commit -m 'doc: new changelog from ${git_tag}'")
                        sh('git push origin BRANCH_MASTER')
                    }
                }
            }
        }
    } finally {
        step([$class: 'Mailer', notifyEveryUnstableBuild: true, recipients: emailextrecipients([[$class: 'CulpritsRecipientProvider'], [$class: 'RequesterRecipientProvider']])])
    }
}
