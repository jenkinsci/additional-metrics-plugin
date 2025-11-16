package org.jenkinsci.plugins.additionalmetrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jenkinsci.plugins.additionalmetrics.JobRunner.WorkflowBuilder.StepDefinitions.*;
import static org.jenkinsci.plugins.additionalmetrics.UIHelpers.*;
import static org.jenkinsci.plugins.additionalmetrics.Utilities.TIME_UNITS;
import static org.junit.jupiter.api.Assertions.assertEquals;

import hudson.model.ListView;
import org.htmlunit.html.DomNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class AvgDurationColumnTest {

    private AvgDurationColumn avgDurationColumn;

    private static JenkinsRule jenkinsRule;

    @BeforeAll
    static void setUp(JenkinsRule rule) {
        jenkinsRule = rule;
    }

    @BeforeEach
    void before() {
        avgDurationColumn = new AvgDurationColumn();
    }

    @Test
    void two_successful_runs_should_return_their_average_duration() throws Exception {
        var runner = JobRunner.createWorkflowJob(jenkinsRule)
                .configurePipelineDefinition(SUCCESS)
                .schedule()
                .configurePipelineDefinition(SLOW_1S)
                .schedule();

        Duration avgDuration = avgDurationColumn.getAverageDuration(runner.getJob());

        assertEquals(
                (runner.getRuns()[0].getDuration() + runner.getRuns()[1].getDuration()) / 2, avgDuration.getAsLong());
    }

    @Test
    void two_runs_including_one_failure_should_return_their_average_duration() throws Exception {
        var runner = JobRunner.createWorkflowJob(jenkinsRule)
                .configurePipelineDefinition(SUCCESS)
                .schedule()
                .configurePipelineDefinition(SLOW_1S, FAILURE)
                .schedule();

        Duration avgDuration = avgDurationColumn.getAverageDuration(runner.getJob());

        assertEquals(
                (runner.getRuns()[0].getDuration() + runner.getRuns()[1].getDuration()) / 2, avgDuration.getAsLong());
    }

    @Test
    void failed_runs_are_not_excluded() throws Exception {
        var runner = JobRunner.createWorkflowJob(jenkinsRule)
                .configurePipelineDefinition(FAILURE)
                .schedule();

        Duration avgDuration = avgDurationColumn.getAverageDuration(runner.getJob());

        assertEquals(runner.getRuns()[0].getDuration(), avgDuration.getAsLong());
    }

    @Test
    void unstable_runs_are_not_excluded() throws Exception {
        var runner = JobRunner.createWorkflowJob(jenkinsRule)
                .configurePipelineDefinition(UNSTABLE)
                .schedule();

        Duration avgDuration = avgDurationColumn.getAverageDuration(runner.getJob());

        assertEquals(runner.getRuns()[0].getDuration(), avgDuration.getAsLong());
    }

    @Test
    void no_runs_should_display_as_NA_in_UI() throws Exception {
        var runner = JobRunner.createWorkflowJob(jenkinsRule);

        ListView listView =
                createAndAddListView(jenkinsRule.getInstance(), "MyListNoRuns", avgDurationColumn, runner.getJob());

        DomNode columnNode;
        try (JenkinsRule.WebClient webClient = jenkinsRule.createWebClient()) {
            columnNode = getListViewCell(
                    webClient.getPage(listView),
                    listView,
                    runner.getJob().getName(),
                    avgDurationColumn.getColumnCaption());
        }

        assertEquals("N/A", columnNode.asNormalizedText());
        assertEquals("0", columnNode.getAttributes().getNamedItem("data").getNodeValue());
    }

    @Test
    void one_run_should_display_avg_duration_in_UI() throws Exception {
        var runner = JobRunner.createWorkflowJob(jenkinsRule)
                .configurePipelineDefinition(SUCCESS)
                .schedule();

        ListView listView =
                createAndAddListView(jenkinsRule.getInstance(), "MyListOneRun", avgDurationColumn, runner.getJob());

        DomNode columnNode;
        try (JenkinsRule.WebClient webClient = jenkinsRule.createWebClient()) {
            columnNode = getListViewCell(
                    webClient.getPage(listView),
                    listView,
                    runner.getJob().getName(),
                    avgDurationColumn.getColumnCaption());
        }

        // sample output: 1.1 sec
        String text = columnNode.asNormalizedText();

        assertThat(text).containsAnyOf(TIME_UNITS);
        assertThat(Long.parseLong(dataOf(columnNode))).isGreaterThan(0L);
    }
}
