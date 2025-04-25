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
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class AvgCheckoutDurationColumnTest {

    private AvgCheckoutDurationColumn avgCheckoutDurationColumn;

    private static JenkinsRule jenkinsRule;

    @BeforeAll
    static void setUp(JenkinsRule rule) {
        jenkinsRule = rule;
    }

    @BeforeEach
    void before() {
        avgCheckoutDurationColumn = new AvgCheckoutDurationColumn();
    }

    @Test
    void two_successful_runs_should_return_a_positive_average_checkout_duration() throws Exception {
        var runner = JobRunner.createWorkflowJob(jenkinsRule)
                .configurePipelineDefinition(CHECKOUT)
                .schedule()
                .schedule();

        Duration avgDuration = avgCheckoutDurationColumn.getAverageCheckoutDuration(runner.getJob());

        assertThat(avgDuration.getAsLong()).isGreaterThan(0L);
    }

    @Test
    void failed_runs_are_not_excluded() throws Exception {
        var runner = JobRunner.createWorkflowJob(jenkinsRule)
                .configurePipelineDefinition(CHECKOUT, FAILURE)
                .schedule();

        Duration avgDuration = avgCheckoutDurationColumn.getAverageCheckoutDuration(runner.getJob());

        assertThat(avgDuration.getAsLong()).isGreaterThan(0L);
    }

    @Test
    void unstable_runs_are_not_excluded() throws Exception {
        var runner = JobRunner.createWorkflowJob(jenkinsRule)
                .configurePipelineDefinition(CHECKOUT, UNSTABLE)
                .schedule();

        Duration avgDuration = avgCheckoutDurationColumn.getAverageCheckoutDuration(runner.getJob());

        assertThat(avgDuration.getAsLong()).isGreaterThan(0L);
    }

    @Test
    void freestyle_jobs_are_not_counted() throws Exception {
        var runner = JobRunner.createFreestyleJob(jenkinsRule)
                .configureCheckout()
                .addSuccessExecution()
                .schedule();

        Duration avgDuration = avgCheckoutDurationColumn.getAverageCheckoutDuration(runner.getJob());

        assertNull(avgDuration);
    }

    @Test
    void no_runs_should_display_as_NA_in_UI() throws Exception {
        var runner = JobRunner.createWorkflowJob(jenkinsRule);

        ListView listView = createAndAddListView(
                jenkinsRule.getInstance(), "MyListNoRuns", avgCheckoutDurationColumn, runner.getJob());

        DomNode columnNode;
        try (JenkinsRule.WebClient webClient = jenkinsRule.createWebClient()) {
            columnNode = getListViewCell(
                    webClient.getPage(listView),
                    listView,
                    runner.getJob().getName(),
                    avgCheckoutDurationColumn.getColumnCaption());
        }

        assertEquals("N/A", columnNode.asNormalizedText());
        assertEquals("0", columnNode.getAttributes().getNamedItem("data").getNodeValue());
    }

    @Test
    void one_run_should_display_avg_duration_in_UI() throws Exception {
        var runner = JobRunner.createWorkflowJob(jenkinsRule)
                .configurePipelineDefinition(CHECKOUT)
                .schedule();

        ListView listView = createAndAddListView(
                jenkinsRule.getInstance(), "MyListOneRun", avgCheckoutDurationColumn, runner.getJob());

        DomNode columnNode;
        try (JenkinsRule.WebClient webClient = jenkinsRule.createWebClient()) {
            columnNode = getListViewCell(
                    webClient.getPage(listView),
                    listView,
                    runner.getJob().getName(),
                    avgCheckoutDurationColumn.getColumnCaption());
        }

        // sample output: 1.1 sec
        String text = columnNode.asNormalizedText();

        assertThat(text).containsAnyOf(TIME_UNITS);
        assertThat(Long.parseLong(dataOf(columnNode))).isGreaterThan(0L);
    }
}
