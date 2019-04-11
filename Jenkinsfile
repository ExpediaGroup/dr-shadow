#!groovy
//Documentation: https://confluence/display/ECT/Egencia+Jenkins+Blue+Ocean
MASTER_BRANCH_NAME = 'master'
SLACK_CHANNEL = "#eg-tech-drshadow"

// Set to true if you want a minor release on each commit to master
// Set to false if you want to perform a release manually from Jenkins UI
AUTO_RELEASE = false
SONAR_ANALYSIS = true
libraryName = JOB_NAME.replaceAll(/-pipeline.*/, '')

/** Library Pipeline --- **/

try {
    switch (env.BRANCH_NAME) {
        case MASTER_BRANCH_NAME:
            // not sending metrics when release pipeline is skipped
            slackSend channel: "${SLACK_CHANNEL}", color: 'good', message: "@here [CUMULUS] <${env.RUN_DISPLAY_URL}|${env.JOB_NAME}:${env.BUILD_NUMBER}> Starting master build..."
            mavenReleasePipeline autoRelease:AUTO_RELEASE, libraryName:libraryName, sonarAnalysis: SONAR_ANALYSIS
            break
        default:
            libraryPullRequestPipeline libraryName: libraryName, sonarAnalysis: SONAR_ANALYSIS
            sendPipelineMetrics("SUCCESS")
    }
} catch (Error e) {
    println e.message
    sendPipelineMetrics("FAILED")
    error e.message
}
