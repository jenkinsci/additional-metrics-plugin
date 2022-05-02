/*
 * MIT License
 *
 * Copyright (c) 2022 Chadi El Masri
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

import hudson.model.Run;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.actions.TimingAction;
import org.jenkinsci.plugins.workflow.cps.nodes.StepAtomNode;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.graph.FlowGraphWalker;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.scm.GenericSCMStep;

class CheckoutDuration {

    private CheckoutDuration() {
        // not instantiatable
    }

    static long checkoutDurationOf(Run run) {
        Jenkins instance = Jenkins.getInstanceOrNull();
        if (instance != null
                && instance.getPlugin("workflow-job") != null
                && run instanceof WorkflowRun) {
            WorkflowRun currentBuild = (WorkflowRun) run;
            FlowExecution execution = currentBuild.getExecution();
            if (execution != null) {
                return countCheckoutDuration(execution);
            }
        }

        return 0;
    }

    private static long countCheckoutDuration(FlowExecution execution) {
        long totalCheckoutTime = 0;

        FlowGraphWalker graphWalker = new FlowGraphWalker(execution);
        FlowNode nextNode = null;
        for (FlowNode node : graphWalker) {
            if (node instanceof StepAtomNode) {
                StepDescriptor descriptor = ((StepAtomNode) node).getDescriptor();
                if (descriptor != null && descriptor.clazz.equals(GenericSCMStep.class)) {
                    totalCheckoutTime += (TimingAction.getStartTime(nextNode) - TimingAction.getStartTime(node));
                }
            }
            nextNode = node;
        }

        return totalCheckoutTime;
    }

}
