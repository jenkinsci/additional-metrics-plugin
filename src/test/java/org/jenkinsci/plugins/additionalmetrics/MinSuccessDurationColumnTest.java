package org.jenkinsci.plugins.additionalmetrics;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.jenkinsci.plugins.additionalmetrics.PipelineDefinitions.*;
import static org.jenkinsci.plugins.additionalmetrics.UIHelpers.*;
import static org.junit.jupiter.api.Assertions.*;

import hudson.model.ListView;
import org.htmlunit.html.DomNode;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
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
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithTwoSuccessfulBuilds");
        project.setDefinition(sleepDefinition(1));
        WorkflowRun run1 = project.scheduleBuild2(0).get();
        project.setDefinition(sleepDefinition(6));
        project.scheduleBuild2(0).get();

        RunWithDuration shortestRun = minSuccessDurationColumn.getShortestSuccessfulRun(project);

        assertSame(run1, shortestRun.getRun());
    }

    @Test
    void failed_runs_should_be_excluded() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithOneFailedBuild");
        project.setDefinition(failingDefinition());
        project.scheduleBuild2(0).get();

        RunWithDuration shortestRun = minSuccessDurationColumn.getShortestSuccessfulRun(project);

        assertNull(shortestRun);
    }

    @Test
    void unstable_runs_should_be_excluded() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithOneUnstableBuild");
        project.setDefinition(unstableDefinition());
        project.scheduleBuild2(0).get();

        RunWithDuration shortestRun = minSuccessDurationColumn.getShortestSuccessfulRun(project);

        assertNull(shortestRun);
    }

    @Test
    void no_runs_should_display_as_NA_in_UI() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithZeroBuildsForUI");

        ListView listView =
                createAndAddListView(jenkinsRule.getInstance(), "MyListNoRuns", minSuccessDurationColumn, project);

        DomNode columnNode;
        try (WebClient webClient = jenkinsRule.createWebClient()) {
            columnNode = getListViewCell(
                    webClient.getPage(listView),
                    listView,
                    project.getName(),
                    minSuccessDurationColumn.getColumnCaption());
        }

        assertEquals("N/A", columnNode.asNormalizedText());
        assertEquals("0", columnNode.getAttributes().getNamedItem("data").getNodeValue());
    }

    @Test
    void one_run_should_display_time_and_build_in_UI() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithOneBuildForUI");
        project.setDefinition(sleepDefinition(1));
        WorkflowRun run = project.scheduleBuild2(0).get();

        ListView listView =
                createAndAddListView(jenkinsRule.getInstance(), "MyListOneRun", minSuccessDurationColumn, project);

        DomNode columnNode;
        try (WebClient webClient = jenkinsRule.createWebClient()) {
            columnNode = getListViewCell(
                    webClient.getPage(listView),
                    listView,
                    project.getName(),
                    minSuccessDurationColumn.getColumnCaption());
        }

        // sample output: 1.1 sec - #1
        String text = columnNode.asNormalizedText();
        assertTrue(text.contains("sec"));
        assertTrue(text.contains("#" + run.getId()));

        assertThat(Long.parseLong(dataOf(columnNode)), greaterThan(0L));
    }
}
