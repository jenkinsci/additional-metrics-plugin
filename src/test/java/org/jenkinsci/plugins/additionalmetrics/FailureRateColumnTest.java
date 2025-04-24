package org.jenkinsci.plugins.additionalmetrics;

import static org.jenkinsci.plugins.additionalmetrics.PipelineDefinitions.*;
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
class FailureRateColumnTest {

    private FailureRateColumn failureRateColumn;

    private static JenkinsRule jenkinsRule;

    @BeforeAll
    static void setUp(JenkinsRule rule) {
        jenkinsRule = rule;
    }

    @BeforeEach
    void before() {
        failureRateColumn = new FailureRateColumn();
    }

    @Test
    void one_failed_job_over_two_failure_rate_should_be_50_percent() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithOneOverTwoSuccess");
        project.setDefinition(failure());
        project.scheduleBuild2(0).get();
        project.setDefinition(success());
        project.scheduleBuild2(0).get();

        Rate failureRate = failureRateColumn.getFailureRate(project);

        assertEquals(0.5, failureRate.getAsDouble(), 0);
    }

    @Test
    void unstable_run_are_considered_failures() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithOneUnstableBuild");
        project.setDefinition(unstable());
        project.scheduleBuild2(0).get();

        Rate failureRate = failureRateColumn.getFailureRate(project);

        assertEquals(1, failureRate.getAsDouble(), 0);
    }

    @Test
    void no_runs_should_display_as_NA_in_UI() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithZeroBuildsForUI");

        ListView listView = createAndAddListView(jenkinsRule.getInstance(), "MyListNoRuns", failureRateColumn, project);

        DomNode columnNode;
        try (JenkinsRule.WebClient webClient = jenkinsRule.createWebClient()) {
            columnNode = getListViewCell(
                    webClient.getPage(listView), listView, project.getName(), failureRateColumn.getColumnCaption());
        }

        assertEquals("N/A", columnNode.asNormalizedText());
        assertEquals("0.0", columnNode.getAttributes().getNamedItem("data").getNodeValue());
    }

    @Test
    void one_run_should_display_percentage_in_UI() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithOneBuildForUI");
        project.setDefinition(failure());
        project.scheduleBuild2(0).get();

        ListView listView = createAndAddListView(jenkinsRule.getInstance(), "MyListOneRun", failureRateColumn, project);

        DomNode columnNode;
        try (JenkinsRule.WebClient webClient = jenkinsRule.createWebClient()) {
            columnNode = getListViewCell(
                    webClient.getPage(listView), listView, project.getName(), failureRateColumn.getColumnCaption());
        }

        assertEquals("100.00%", columnNode.asNormalizedText());
        assertEquals("1.0", dataOf(columnNode));
    }
}
