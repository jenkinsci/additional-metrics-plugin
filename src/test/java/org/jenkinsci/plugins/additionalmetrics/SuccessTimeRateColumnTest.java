package org.jenkinsci.plugins.additionalmetrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jenkinsci.plugins.additionalmetrics.JobRunner.WorkflowBuilder.StepDefinitions.FAILURE;
import static org.jenkinsci.plugins.additionalmetrics.JobRunner.WorkflowBuilder.StepDefinitions.SUCCESS;
import static org.jenkinsci.plugins.additionalmetrics.UIHelpers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import hudson.model.ListView;
import org.htmlunit.html.DomNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class SuccessTimeRateColumnTest {

    private SuccessTimeRateColumn successTimeRateColumn;

    private static JenkinsRule jenkinsRule;

    @BeforeAll
    static void setUp(JenkinsRule rule) {
        jenkinsRule = rule;
    }

    @BeforeEach
    void before() {
        successTimeRateColumn = new SuccessTimeRateColumn();
    }

    @Test
    void one_failed_run_success_time_rate_should_be_0_percent() throws Exception {
        var runner = JobRunner.createWorkflowJob(jenkinsRule)
                .configurePipelineDefinition(FAILURE)
                .schedule();

        Rate successTimeRate = successTimeRateColumn.getSuccessTimeRate(runner.getJob());

        assertEquals(0, successTimeRate.getAsDouble(), 0);
    }

    @Test
    void two_failed_runs_success_time_rate_should_be_0_percent() throws Exception {
        var runner = JobRunner.createWorkflowJob(jenkinsRule)
                .configurePipelineDefinition(FAILURE)
                .schedule()
                .schedule();

        Rate successTimeRate = successTimeRateColumn.getSuccessTimeRate(runner.getJob());

        assertEquals(0, successTimeRate.getAsDouble(), 0);
    }

    @Test
    void one_failed_run_followed_by_one_success_run() throws Exception {
        var runner = JobRunner.createWorkflowJob(jenkinsRule)
                .configurePipelineDefinition(FAILURE)
                .schedule()
                .configurePipelineDefinition(SUCCESS)
                .schedule();

        Rate successTimeRate = successTimeRateColumn.getSuccessTimeRate(runner.getJob());

        assertThat(successTimeRate.getAsDouble()).isGreaterThan(0.0);
        assertThat(successTimeRate.getAsDouble()).isLessThan(1.0);
    }

    @Test
    void no_runs_should_display_as_NA_in_UI() throws Exception {
        var runner = JobRunner.createWorkflowJob(jenkinsRule);

        ListView listView =
                createAndAddListView(jenkinsRule.getInstance(), "MyListNoRuns", successTimeRateColumn, runner.getJob());

        DomNode columnNode;
        try (JenkinsRule.WebClient webClient = jenkinsRule.createWebClient()) {
            columnNode = getListViewCell(
                    webClient.getPage(listView),
                    listView,
                    runner.getJob().getName(),
                    successTimeRateColumn.getColumnCaption());
        }

        assertEquals("N/A", columnNode.asNormalizedText());
        assertEquals("0.0", columnNode.getAttributes().getNamedItem("data").getNodeValue());
    }

    @Test
    void one_run_should_display_percentage_in_UI() throws Exception {
        var runner = JobRunner.createWorkflowJob(jenkinsRule)
                .configurePipelineDefinition(SUCCESS)
                .schedule();

        ListView listView =
                createAndAddListView(jenkinsRule.getInstance(), "MyListOneRun", successTimeRateColumn, runner.getJob());

        DomNode columnNode;
        try (JenkinsRule.WebClient webClient = jenkinsRule.createWebClient()) {
            columnNode = getListViewCell(
                    webClient.getPage(listView),
                    listView,
                    runner.getJob().getName(),
                    successTimeRateColumn.getColumnCaption());
        }

        assertEquals("100.00%", columnNode.asNormalizedText());
        assertEquals("1.0", dataOf(columnNode));
    }
}
