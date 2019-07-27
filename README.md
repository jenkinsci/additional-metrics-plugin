Additional Metrics Plugin
=========================

[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins/additional-metrics-plugin/master)](https://ci.jenkins.io/job/Plugins/job/additional-metrics-plugin/job/master/)

Provides additional metrics via columns in Jenkins' List View.

### Provided Metrics
- Minimum, Maximum, and Average build times for all, or only successful builds.
- Minimum, Maximum, and Average checkout times for Pipeline builds.
- Success and Failure rates.
- Success and Failure time rates (ie Uptime and Downtime).

### REST API
All provided metrics are also exposed in the Job's REST API as a job Action. They will show up at depth=3. For example, to access `Project` metrics, you can use the following request:
```
<JENKINS_URL>/api/xml?depth=3&xpath=/hudson/job[name='Project']/action/jobMetrics
```

### Requirements
Jenkins 1.651.3 or later.

### Building
```
mvn package
```

Snapshot builds are available [here](https://ci.jenkins.io/blue/organizations/jenkins/Plugins%2Fadditional-metrics-plugin/branches/).