package org.jenkinsci.plugins.additionalmetrics;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.jenkinsci.plugins.additionalmetrics.PipelineDefinitions.failingDefinition;
import static org.jenkinsci.plugins.additionalmetrics.PipelineDefinitions.successDefinition;
import static org.jenkinsci.plugins.additionalmetrics.UIHelpers.*;
import static org.junit.Assert.assertEquals;

import hudson.model.ListView;
import org.htmlunit.html.DomNode;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class SuccessTimeRateColumnTest {
    @ClassRule
    public static final JenkinsRule jenkinsRule = new JenkinsRule();

    private SuccessTimeRateColumn successTimeRateColumn;

    @Before
    public void before() {
        successTimeRateColumn = new SuccessTimeRateColumn();
    }

    @Test
    public void one_failed_run_success_time_rate_should_be_0_percent() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithOneFailure");
        project.setDefinition(failingDefinition());
        project.scheduleBuild2(0).get();

        Rate successTimeRate = successTimeRateColumn.getSuccessTimeRate(project);

        assertEquals(0, successTimeRate.getAsDouble(), 0);
    }

    @Test
    public void two_failed_runs_success_time_rate_should_be_0_percent() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithTwoFailures");
        project.setDefinition(failingDefinition());
        project.scheduleBuild2(0).get();
        project.scheduleBuild2(0).get();

        Rate successTimeRate = successTimeRateColumn.getSuccessTimeRate(project);

        assertEquals(0, successTimeRate.getAsDouble(), 0);
    }

    @Test
    public void one_failed_run_followed_by_one_success_run() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithFailureThenSuccess");
        project.setDefinition(failingDefinition());
        project.scheduleBuild2(0).get();
        project.setDefinition(successDefinition());
        project.scheduleBuild2(0).get();

        Rate successTimeRate = successTimeRateColumn.getSuccessTimeRate(project);

        assertThat(successTimeRate.getAsDouble(), greaterThan(0.0));
        assertThat(successTimeRate.getAsDouble(), lessThan(1.0));
    }

    @Test
    public void no_runs_should_display_as_NA_in_UI() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithZeroBuildsForUI");

        ListView listView =
                createAndAddListView(jenkinsRule.getInstance(), "MyListNoRuns", successTimeRateColumn, project);

        DomNode columnNode;
        try (JenkinsRule.WebClient webClient = jenkinsRule.createWebClient()) {
            columnNode = getListViewCell(
                    webClient.getPage(listView), listView, project.getName(), successTimeRateColumn.getColumnCaption());
        }

        assertEquals("N/A", columnNode.asNormalizedText());
        assertEquals("0.0", columnNode.getAttributes().getNamedItem("data").getNodeValue());
    }

    @Test
    public void one_run_should_display_percentage_in_UI() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithOneBuildForUI");
        project.setDefinition(successDefinition());
        project.scheduleBuild2(0).get();

        ListView listView =
                createAndAddListView(jenkinsRule.getInstance(), "MyListOneRun", successTimeRateColumn, project);

        DomNode columnNode;
        try (JenkinsRule.WebClient webClient = jenkinsRule.createWebClient()) {
            columnNode = getListViewCell(
                    webClient.getPage(listView), listView, project.getName(), successTimeRateColumn.getColumnCaption());
        }

        assertEquals("100.00%", columnNode.asNormalizedText());
        assertEquals("1.0", dataOf(columnNode));
    }
}
