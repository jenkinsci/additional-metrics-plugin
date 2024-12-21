package org.jenkinsci.plugins.additionalmetrics;

import hudson.model.Descriptor;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;

class PipelineDefinitions {

    private static final String CHECKOUT =
            "checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[url: 'https://github.com/jenkinsci/additional-metrics-plugin.git']]])";
    private static final String FAILURE = "ech";
    private static final String UNSTABLE = "currentBuild.result = 'UNSTABLE'";

    private PipelineDefinitions() {
        // test utility class
    }

    static CpsFlowDefinition failingDefinition() throws Descriptor.FormException {
        return new CpsFlowDefinition("node { " + FAILURE + " }", true);
    }

    static CpsFlowDefinition successDefinition() throws Descriptor.FormException {
        return new CpsFlowDefinition("node { echo 'Hello, World!' }", true);
    }

    static CpsFlowDefinition unstableDefinition() throws Descriptor.FormException {
        return new CpsFlowDefinition("node { " + UNSTABLE + " }", true);
    }

    static CpsFlowDefinition sleepDefinition(int seconds) throws Descriptor.FormException {
        return new CpsFlowDefinition("node { sleep " + seconds + " }", true);
    }

    static CpsFlowDefinition sleepThenFailDefinition(int seconds) throws Descriptor.FormException {
        return new CpsFlowDefinition("node { sleep " + seconds + "; " + FAILURE + " }", true);
    }

    static CpsFlowDefinition checkoutDefinition() throws Descriptor.FormException {
        return new CpsFlowDefinition("node { " + CHECKOUT + " }", true);
    }

    static CpsFlowDefinition checkoutThenFailDefinition() throws Descriptor.FormException {
        return new CpsFlowDefinition("node { " + CHECKOUT + "; " + FAILURE + " }", true);
    }

    static CpsFlowDefinition checkoutThenUnstableDefinition() throws Descriptor.FormException {
        return new CpsFlowDefinition("node { " + CHECKOUT + "; " + UNSTABLE + " }", true);
    }

    static CpsFlowDefinition slowDefinition() throws Descriptor.FormException {
        return sleepDefinition(60);
    }
}
