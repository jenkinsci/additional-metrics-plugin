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
class MinSuccessDurationColumnTest {

    private MinSuccessDurationColumn minSuccessDurationColumn;

    private static JenkinsRule jenkinsRule;

    @BeforeAll
    static void setUp(JenkinsRule rule) {
        jenkinsRule = rule;
    }

    @BeforeEach
    void before() {
        minSuccessDurationColumn = new MinSuccessDurationColumn();
    }

    @Test
    void two_successful_runs_should_return_the_shortest() throws Exception {
        var runner = JobRunner.createWorkflowJob(jenkinsRule)
                .configurePipelineDefinition(SUCCESS)
                .schedule()
                .configurePipelineDefinition(SLOW_1S)
                .schedule();

        RunWithDuration shortestRun = minSuccessDurationColumn.getShortestSuccessfulRun(runner.getJob());

        assertSame(runner.getRuns()[0], shortestRun.run());
    }

    @Test
    void failed_runs_should_be_excluded() throws Exception {
        var runner = JobRunner.createWorkflowJob(jenkinsRule)
                .configurePipelineDefinition(FAILURE)
                .schedule();

        RunWithDuration shortestRun = minSuccessDurationColumn.getShortestSuccessfulRun(runner.getJob());

        assertNull(shortestRun);
    }

    @Test
    void unstable_runs_should_be_excluded() throws Exception {
        var runner = JobRunner.createWorkflowJob(jenkinsRule)
                .configurePipelineDefinition(UNSTABLE)
                .schedule();

        RunWithDuration shortestRun = minSuccessDurationColumn.getShortestSuccessfulRun(runner.getJob());

        assertNull(shortestRun);
    }

    @Test
    void no_runs_should_display_as_NA_in_UI() throws Exception {
        var runner = JobRunner.createWorkflowJob(jenkinsRule);

        ListView listView = createAndAddListView(
                jenkinsRule.getInstance(), "MyListNoRuns", minSuccessDurationColumn, runner.getJob());

        DomNode columnNode;
        try (WebClient webClient = jenkinsRule.createWebClient()) {
            columnNode = getListViewCell(
                    webClient.getPage(listView),
                    listView,
                    runner.getJob().getName(),
                    minSuccessDurationColumn.getColumnCaption());
        }

        assertEquals("N/A", columnNode.asNormalizedText());
        assertEquals("0", columnNode.getAttributes().getNamedItem("data").getNodeValue());
    }

    @Test
    void one_run_should_display_time_and_build_in_UI() throws Exception {
        var runner = JobRunner.createWorkflowJob(jenkinsRule)
                .configurePipelineDefinition(SUCCESS)
                .schedule();

        ListView listView = createAndAddListView(
                jenkinsRule.getInstance(), "MyListOneRun", minSuccessDurationColumn, runner.getJob());

        DomNode columnNode;
        try (WebClient webClient = jenkinsRule.createWebClient()) {
            columnNode = getListViewCell(
                    webClient.getPage(listView),
                    listView,
                    runner.getJob().getName(),
                    minSuccessDurationColumn.getColumnCaption());
        }

        // sample output: 1.1 sec - #1
        String text = columnNode.asNormalizedText();

        assertThat(text).containsAnyOf(TIME_UNITS);
        assertThat(text).contains("#" + runner.getRuns()[0].getId());
        assertThat(Long.parseLong(dataOf(columnNode))).isGreaterThan(0L);
    }
}
