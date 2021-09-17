#!groovy
build('bouncer-spring-boot-starter', 'docker-host') {
    checkoutRepo()
    loadBuildUtils()

    def javaLibPipeline
    runStage('load JavaLib pipeline') {
        javaLibPipeline = load("build_utils/jenkins_lib/pipeJavaLibInsideDocker.groovy")
    }

    javaLibPipeline()
}
