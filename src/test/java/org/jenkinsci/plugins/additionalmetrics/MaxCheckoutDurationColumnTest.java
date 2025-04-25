package org.jenkinsci.plugins.additionalmetrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jenkinsci.plugins.additionalmetrics.JobRunner.WorkflowBuilder.StepDefinitions.*;
import static org.jenkinsci.plugins.additionalmetrics.UIHelpers.*;
import static org.jenkinsci.plugins.additionalmetrics.Utilities.TIME_UNITS;
import static org.junit.jupiter.api.Assertions.*;

import hudson.model.ListView;
import org.htmlunit.html.DomNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.JenkinsRule.WebClient;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class MaxCheckoutDurationColumnTest {

    private MaxCheckoutDurationColumn maxCheckoutDurationColumn;

    private static JenkinsRule jenkinsRule;

    @BeforeAll
    static void setUp(JenkinsRule rule) {
        jenkinsRule = rule;
    }

    @BeforeEach
    void before() {
        maxCheckoutDurationColumn = new MaxCheckoutDurationColumn();
    }

    @Test
    void one_run_with_checkout_should_return_checkout() throws Exception {
        var runner = JobRunner.createWorkflowJob(jenkinsRule)
                .configurePipelineDefinition(CHECKOUT)
                .schedule();

        RunWithDuration longestCheckoutRun = maxCheckoutDurationColumn.getLongestCheckoutRun(runner.getJob());

        assertThat(longestCheckoutRun.duration().getAsLong()).isGreaterThan(0L);
    }

    @Test
    void failed_runs_are_included_in_the_checkout_time_calculation() throws Exception {
        var runner = JobRunner.createWorkflowJob(jenkinsRule)
                .configurePipelineDefinition(CHECKOUT, FAILURE)
                .schedule();

        RunWithDuration longestCheckoutRun = maxCheckoutDurationColumn.getLongestCheckoutRun(runner.getJob());

        assertSame(runner.getRuns()[0], longestCheckoutRun.run());
    }

    @Test
    void unstable_runs_are_included_in_the_checkout_time_calculation() throws Exception {
        var runner = JobRunner.createWorkflowJob(jenkinsRule)
                .configurePipelineDefinition(CHECKOUT, UNSTABLE)
                .schedule();

        RunWithDuration longestCheckoutRun = maxCheckoutDurationColumn.getLongestCheckoutRun(runner.getJob());

        assertSame(runner.getRuns()[0], longestCheckoutRun.run());
    }

    @Test
    void freestyle_jobs_are_not_counted() throws Exception {
        var runner = JobRunner.createFreestyleJob(jenkinsRule)
                .configureCheckout()
                .addSuccessExecution()
                .schedule();

        RunWithDuration longestCheckoutRun = maxCheckoutDurationColumn.getLongestCheckoutRun(runner.getJob());

        assertNull(longestCheckoutRun);
    }

    @Test
    void no_runs_should_display_as_NA_in_UI() throws Exception {
        var runner = JobRunner.createWorkflowJob(jenkinsRule);

        ListView listView = createAndAddListView(
                jenkinsRule.getInstance(), "MyListNoRuns", maxCheckoutDurationColumn, runner.getJob());

        DomNode columnNode;
        try (WebClient webClient = jenkinsRule.createWebClient()) {
            columnNode = getListViewCell(
                    webClient.getPage(listView),
                    listView,
                    runner.getJob().getName(),
                    maxCheckoutDurationColumn.getColumnCaption());
        }

        assertEquals("N/A", columnNode.asNormalizedText());
        assertEquals("0", columnNode.getAttributes().getNamedItem("data").getNodeValue());
    }

    @Test
    void one_run_should_display_time_and_build_in_UI() throws Exception {
        var runner = JobRunner.createWorkflowJob(jenkinsRule)
                .configurePipelineDefinition(CHECKOUT)
                .schedule();

        ListView listView = createAndAddListView(
                jenkinsRule.getInstance(), "MyListOneRun", maxCheckoutDurationColumn, runner.getJob());

        DomNode columnNode;
        try (WebClient webClient = jenkinsRule.createWebClient()) {
            columnNode = getListViewCell(
                    webClient.getPage(listView),
                    listView,
                    runner.getJob().getName(),
                    maxCheckoutDurationColumn.getColumnCaption());
        }

        // sample output: 1.1 sec - #1
        String text = columnNode.asNormalizedText();

        assertThat(text).containsAnyOf(TIME_UNITS);
        assertThat(text).contains("#" + runner.getRuns()[0].getId());
        assertThat(Long.parseLong(dataOf(columnNode))).isGreaterThan(0L);
    }
}
