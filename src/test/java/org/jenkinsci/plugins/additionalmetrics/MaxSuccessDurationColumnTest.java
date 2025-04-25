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
class MaxSuccessDurationColumnTest {

    private MaxSuccessDurationColumn maxSuccessDurationColumn;

    private static JenkinsRule jenkinsRule;

    @BeforeAll
    static void setUp(JenkinsRule rule) {
        jenkinsRule = rule;
    }

    @BeforeEach
    void before() {
        maxSuccessDurationColumn = new MaxSuccessDurationColumn();
    }

    @Test
    void two_successful_runs_should_return_the_longest() throws Exception {
        var runner = JobRunner.createWorkflowJob(jenkinsRule)
                .configurePipelineDefinition(SUCCESS)
                .schedule()
                .configurePipelineDefinition(SLOW_3S)
                .schedule();

        RunWithDuration longestRun = maxSuccessDurationColumn.getLongestSuccessfulRun(runner.getJob());

        assertSame(runner.getRuns()[1], longestRun.run());
    }

    @Test
    void failed_runs_should_be_excluded() throws Exception {
        var runner = JobRunner.createWorkflowJob(jenkinsRule)
                .configurePipelineDefinition(FAILURE)
                .schedule();

        RunWithDuration longestRun = maxSuccessDurationColumn.getLongestSuccessfulRun(runner.getJob());

        assertNull(longestRun);
    }

    @Test
    void unstable_runs_should_be_excluded() throws Exception {
        var runner = JobRunner.createWorkflowJob(jenkinsRule)
                .configurePipelineDefinition(UNSTABLE)
                .schedule();

        RunWithDuration longestRun = maxSuccessDurationColumn.getLongestSuccessfulRun(runner.getJob());

        assertNull(longestRun);
    }

    @Test
    void no_runs_should_display_as_NA_in_UI() throws Exception {
        var runner = JobRunner.createWorkflowJob(jenkinsRule);

        ListView listView = createAndAddListView(
                jenkinsRule.getInstance(), "MyListNoRuns", maxSuccessDurationColumn, runner.getJob());

        DomNode columnNode;
        try (WebClient webClient = jenkinsRule.createWebClient()) {
            columnNode = getListViewCell(
                    webClient.getPage(listView),
                    listView,
                    runner.getJob().getName(),
                    maxSuccessDurationColumn.getColumnCaption());
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
                jenkinsRule.getInstance(), "MyListOneRun", maxSuccessDurationColumn, runner.getJob());

        DomNode columnNode;
        try (WebClient webClient = jenkinsRule.createWebClient()) {
            columnNode = getListViewCell(
                    webClient.getPage(listView),
                    listView,
                    runner.getJob().getName(),
                    maxSuccessDurationColumn.getColumnCaption());
        }

        // sample output: 1.1 sec - #1
        String text = columnNode.asNormalizedText();

        assertThat(text).containsAnyOf(TIME_UNITS);
        assertThat(text).contains("#" + runner.getRuns()[0].getId());
        assertThat(Long.parseLong(dataOf(columnNode))).isGreaterThan(0L);
    }
}
