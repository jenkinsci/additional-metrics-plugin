/*
 * MIT License
 *
 * Copyright (c) 2019 Chadi El Masri
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.jenkinsci.plugins.additionalmetrics;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;

class PipelineDefinitions {

    private static final String CHECKOUT = "checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[url: 'https://github.com/jenkinsci/additional-metrics-plugin.git']]])";
    private static final String FAILURE = "ech";
    private static final String UNSTABLE = "currentBuild.result = 'UNSTABLE'";

    private PipelineDefinitions() {
        // test utility class
    }

    static CpsFlowDefinition failingDefinition() {
        return new CpsFlowDefinition("node { " + FAILURE + " }", true);
    }

    static CpsFlowDefinition successDefinition() {
        return new CpsFlowDefinition("node { echo 'Hello, World!' }", true);
    }

    static CpsFlowDefinition unstableDefinition() {
        return new CpsFlowDefinition("node { " + UNSTABLE + " }", true);
    }

    static CpsFlowDefinition sleepDefinition(int seconds) {
        return new CpsFlowDefinition("node { sleep " + seconds + " }", true);
    }

    static CpsFlowDefinition sleepThenFailDefinition(int seconds) {
        return new CpsFlowDefinition("node { sleep " + seconds + "; " + FAILURE + " }", true);
    }

    static CpsFlowDefinition checkoutDefinition() {
        return new CpsFlowDefinition("node { " + CHECKOUT + " }", true);
    }

    static CpsFlowDefinition checkoutThenFailDefinition() {
        return new CpsFlowDefinition("node { " + CHECKOUT + "; " + FAILURE + " }", true);
    }

    static CpsFlowDefinition checkoutThenUnstableDefinition() {
        return new CpsFlowDefinition("node { " + CHECKOUT + "; " + UNSTABLE + " }", true);
    }

    static CpsFlowDefinition slowDefinition() {
        return sleepDefinition(60);
    }

}
