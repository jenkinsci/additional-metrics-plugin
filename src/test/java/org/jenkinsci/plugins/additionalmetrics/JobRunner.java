package org.jenkinsci.plugins.additionalmetrics;

import hudson.model.*;
import hudson.plugins.git.GitSCM;
import hudson.scm.SCM;
import hudson.tasks.Builder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.SleepBuilder;

class JobRunner {

    private static final String SCM_URL = "https://github.com/jenkinsci/additional-metrics-plugin.git";

    static FreestyleBuilder createFreestyleJob(JenkinsRule jenkinsRule) throws IOException {
        return new FreestyleBuilder(jenkinsRule);
    }

    static WorkflowBuilder createWorkflowJob(JenkinsRule jenkinsRule) throws IOException {
        return new WorkflowBuilder(jenkinsRule);
    }

    private static String randomProjectName() {
        Random random = new Random();

        String generatedString = random.ints(97, 123)
                .limit(10)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return "Project-" + generatedString;
    }

    static class WorkflowBuilder {
        private final WorkflowJob project;
        private final List<WorkflowRun> runs;

        WorkflowBuilder(JenkinsRule jenkinsRule) throws IOException {
            this.project = jenkinsRule.createProject(WorkflowJob.class, randomProjectName());
            this.runs = new ArrayList<>();
        }

        WorkflowBuilder configurePipelineDefinition(StepDefinitions... steps) throws Descriptor.FormException {
            project.setDefinition(new CpsFlowDefinition(
                    "node { "
                            + Arrays.stream(steps)
                                    .map(StepDefinitions::pipelineCode)
                                    .collect(Collectors.joining("; "))
                            + " }",
                    true));
            return this;
        }

        WorkflowBuilder schedule() throws ExecutionException, InterruptedException {
            runs.add(project.scheduleBuild2(0).get());
            return this;
        }

        WorkflowBuilder scheduleNoWait() throws ExecutionException, InterruptedException {
            runs.add(project.scheduleBuild2(0).waitForStart());
            return this;
        }

        WorkflowJob getJob() {
            return project;
        }

        WorkflowRun[] getRuns() {
            return runs.toArray(WorkflowRun[]::new);
        }

        enum StepDefinitions {
            SUCCESS("echo 'Hello, World!'"),
            FAILURE("ech"),
            UNSTABLE("currentBuild.result = 'UNSTABLE'"),
            SLOW_1S("sleep 1"),
            CHECKOUT(
                    "checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[url: '"
                            + SCM_URL + "']]])"),
            VERY_SLOW_60S("sleep 60");

            private final String pipelineCode;

            StepDefinitions(String pipelineCode) {
                this.pipelineCode = pipelineCode;
            }

            private String pipelineCode() {
                return pipelineCode;
            }
        }
    }

    static class FreestyleBuilder {
        private final FreeStyleProject project;
        private final List<FreeStyleBuild> runs;

        FreestyleBuilder(JenkinsRule jenkinsRule) throws IOException {
            this.project = jenkinsRule.createFreeStyleProject(randomProjectName());
            this.runs = new ArrayList<>();
        }

        FreestyleBuilder configureCheckout() throws IOException {
            project.setScm(Definitions.checkout());
            return this;
        }

        FreestyleBuilder addSuccessExecution() {
            project.getBuildersList().add(Definitions.successExecution());
            return this;
        }

        FreestyleBuilder schedule() throws ExecutionException, InterruptedException {
            runs.add(project.scheduleBuild2(0).get());
            return this;
        }

        FreeStyleProject getJob() {
            return project;
        }

        static class Definitions {
            static SCM checkout() {
                return new GitSCM(JobRunner.SCM_URL);
            }

            static Builder successExecution() {
                return new SleepBuilder(200);
            }
        }
    }
}
