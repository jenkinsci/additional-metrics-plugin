package org.jenkinsci.plugins.additionalmetrics;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.jenkinsci.plugins.additionalmetrics.PipelineDefinitions.*;
import static org.jenkinsci.plugins.additionalmetrics.UIHelpers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import hudson.model.ListView;
import org.htmlunit.html.DomNode;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class AvgDurationColumnTest {
    @ClassRule
    public static final JenkinsRule jenkinsRule = new JenkinsRule();

    private AvgDurationColumn avgDurationColumn;

    @Before
    public void before() {
        avgDurationColumn = new AvgDurationColumn();
    }

    @Test
    public void two_successful_runs_should_return_their_average_duration() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithTwoSuccessfulBuilds");
        project.setDefinition(sleepDefinition(1));
        WorkflowRun run1 = project.scheduleBuild2(0).get();
        project.setDefinition(sleepDefinition(6));
        WorkflowRun run2 = project.scheduleBuild2(0).get();

        Duration avgDuration = avgDurationColumn.getAverageDuration(project);

        assertEquals((run1.getDuration() + run2.getDuration()) / 2, avgDuration.getAsLong());
    }

    @Test
    public void two_runs_including_one_failure_should_return_their_average_duration() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithTwoBuildsOneFailure");
        project.setDefinition(sleepDefinition(1));
        WorkflowRun run1 = project.scheduleBuild2(0).get();
        project.setDefinition(sleepThenFailDefinition(6));
        WorkflowRun run2 = project.scheduleBuild2(0).get();

        Duration avgDuration = avgDurationColumn.getAverageDuration(project);

        assertEquals((run1.getDuration() + run2.getDuration()) / 2, avgDuration.getAsLong());
    }

    @Test
    public void failed_runs_are_not_excluded() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithOneFailedBuild");
        project.setDefinition(failingDefinition());
        WorkflowRun run = project.scheduleBuild2(0).get();

        Duration avgDuration = avgDurationColumn.getAverageDuration(project);

        assertEquals(run.getDuration(), avgDuration.getAsLong());
    }

    @Test
    public void unstable_runs_are_not_excluded() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithOneUnstableBuild");
        project.setDefinition(unstableDefinition());
        WorkflowRun run = project.scheduleBuild2(0).get();

        Duration avgDuration = avgDurationColumn.getAverageDuration(project);

        assertEquals(run.getDuration(), avgDuration.getAsLong());
    }

    @Test
    public void no_runs_should_display_as_NA_in_UI() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithZeroBuildsForUI");

        ListView listView = createAndAddListView(jenkinsRule.getInstance(), "MyListNoRuns", avgDurationColumn, project);

        DomNode columnNode;
        try (JenkinsRule.WebClient webClient = jenkinsRule.createWebClient()) {
            columnNode = getListViewCell(
                    webClient.getPage(listView), listView, project.getName(), avgDurationColumn.getColumnCaption());
        }

        assertEquals("N/A", columnNode.asNormalizedText());
        assertEquals("0", columnNode.getAttributes().getNamedItem("data").getNodeValue());
    }

    @Test
    public void one_run_should_display_avg_duration_in_UI() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithOneBuildForUI");
        project.setDefinition(sleepDefinition(1));
        project.scheduleBuild2(0).get();

        ListView listView = createAndAddListView(jenkinsRule.getInstance(), "MyListOneRun", avgDurationColumn, project);

        DomNode columnNode;
        try (JenkinsRule.WebClient webClient = jenkinsRule.createWebClient()) {
            columnNode = getListViewCell(
                    webClient.getPage(listView), listView, project.getName(), avgDurationColumn.getColumnCaption());
        }

        // sample output: 1.1 sec
        String text = columnNode.asNormalizedText();
        assertTrue(text.contains("sec"));

        assertThat(Long.parseLong(dataOf(columnNode)), greaterThan(0L));
    }
}
