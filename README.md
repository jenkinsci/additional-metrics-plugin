Additional Metrics Plugin
=========================

[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins/additional-metrics-plugin/master)](https://ci.jenkins.io/job/Plugins/job/additional-metrics-plugin/job/master/)

Provides additional metrics via columns in Jenkins' List View.

### Provided Metrics
- Minimum, Maximum and Average build times for all, or only successful builds.
- Success and Failure rates.
- Success and Failure time rates (ie Uptime and Downtime).

### Requirements
Jenkins 1.651.3 or later.

### Building
```
mvn package
```

Snapshot builds are available [here](https://ci.jenkins.io/blue/organizations/jenkins/Plugins%2Fadditional-metrics-plugin/branches/).