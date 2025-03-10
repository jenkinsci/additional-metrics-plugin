package org.jenkinsci.plugins.additionalmetrics;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.jenkinsci.plugins.additionalmetrics.PipelineDefinitions.failingDefinition;
import static org.jenkinsci.plugins.additionalmetrics.PipelineDefinitions.successDefinition;
import static org.jenkinsci.plugins.additionalmetrics.UIHelpers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import hudson.model.ListView;
import org.htmlunit.html.DomNode;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class FailureTimeRateColumnTest {

    private FailureTimeRateColumn failureTimeRateColumn;

    private static JenkinsRule jenkinsRule;

    @BeforeAll
    static void setUp(JenkinsRule rule) {
        jenkinsRule = rule;
    }

    @BeforeEach
    void before() {
        failureTimeRateColumn = new FailureTimeRateColumn();
    }

    @Test
    void one_success_run_failure_time_rate_should_be_0_percent() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithOneSuccess");
        project.setDefinition(successDefinition());
        project.scheduleBuild2(0).get();

        Rate failureTimeRate = failureTimeRateColumn.getFailureTimeRate(project);

        assertEquals(0, failureTimeRate.getAsDouble(), 0);
    }

    @Test
    void two_success_runs_failure_time_rate_should_be_0_percent() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithTwoSuccess");
        project.setDefinition(successDefinition());
        project.scheduleBuild2(0).get();
        project.scheduleBuild2(0).get();

        Rate failureTimeRate = failureTimeRateColumn.getFailureTimeRate(project);

        assertEquals(0, failureTimeRate.getAsDouble(), 0);
    }

    @Test
    void one_success_run_followed_by_one_failure_run() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithSuccessThenFailure");
        project.setDefinition(successDefinition());
        project.scheduleBuild2(0).get();
        project.setDefinition(failingDefinition());
        project.scheduleBuild2(0).get();

        Rate failureTimeRate = failureTimeRateColumn.getFailureTimeRate(project);

        assertThat(failureTimeRate.getAsDouble(), greaterThan(0.0));
        assertThat(failureTimeRate.getAsDouble(), lessThan(1.0));
    }

    @Test
    void no_runs_should_display_as_NA_in_UI() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithZeroBuildsForUI");

        ListView listView =
                createAndAddListView(jenkinsRule.getInstance(), "MyListNoRuns", failureTimeRateColumn, project);

        DomNode columnNode;
        try (JenkinsRule.WebClient webClient = jenkinsRule.createWebClient()) {
            columnNode = getListViewCell(
                    webClient.getPage(listView), listView, project.getName(), failureTimeRateColumn.getColumnCaption());
        }

        assertEquals("N/A", columnNode.asNormalizedText());
        assertEquals("0.0", columnNode.getAttributes().getNamedItem("data").getNodeValue());
    }

    @Test
    void one_run_should_display_percentage_in_UI() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithOneBuildForUI");
        project.setDefinition(failingDefinition());
        project.scheduleBuild2(0).get();

        ListView listView =
                createAndAddListView(jenkinsRule.getInstance(), "MyListOneRun", failureTimeRateColumn, project);

        DomNode columnNode;
        try (JenkinsRule.WebClient webClient = jenkinsRule.createWebClient()) {
            columnNode = getListViewCell(
                    webClient.getPage(listView), listView, project.getName(), failureTimeRateColumn.getColumnCaption());
        }

        assertEquals("100.00%", columnNode.asNormalizedText());
        assertEquals("1.0", dataOf(columnNode));
    }
}
