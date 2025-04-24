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

    static CpsFlowDefinition failure() throws Descriptor.FormException {
        return new CpsFlowDefinition("node { " + FAILURE + " }", true);
    }

    static CpsFlowDefinition success() throws Descriptor.FormException {
        return new CpsFlowDefinition("node { echo 'Hello, World!' }", true);
    }

    static CpsFlowDefinition unstable() throws Descriptor.FormException {
        return new CpsFlowDefinition("node { " + UNSTABLE + " }", true);
    }

    static CpsFlowDefinition slow() throws Descriptor.FormException {
        return sleep(3);
    }

    static CpsFlowDefinition verySlow() throws Descriptor.FormException {
        return sleep(60);
    }

    static CpsFlowDefinition slowFailure() throws Descriptor.FormException {
        return sleepThenFail(3);
    }

    static CpsFlowDefinition checkout() throws Descriptor.FormException {
        return new CpsFlowDefinition("node { " + CHECKOUT + " }", true);
    }

    static CpsFlowDefinition checkoutThenFail() throws Descriptor.FormException {
        return new CpsFlowDefinition("node { " + CHECKOUT + "; " + FAILURE + " }", true);
    }

    static CpsFlowDefinition checkoutThenUnstable() throws Descriptor.FormException {
        return new CpsFlowDefinition("node { " + CHECKOUT + "; " + UNSTABLE + " }", true);
    }

    static CpsFlowDefinition sleep(int seconds) throws Descriptor.FormException {
        return new CpsFlowDefinition("node { sleep " + seconds + " }", true);
    }

    private static CpsFlowDefinition sleepThenFail(int seconds) throws Descriptor.FormException {
        return new CpsFlowDefinition("node { sleep " + seconds + "; " + FAILURE + " }", true);
    }
}
