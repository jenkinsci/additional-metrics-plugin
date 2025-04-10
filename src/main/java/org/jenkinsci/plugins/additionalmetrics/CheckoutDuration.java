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
        if (instance != null && instance.getPlugin("workflow-job") != null && run instanceof WorkflowRun currentBuild) {
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
