package org.jenkinsci.plugins.additionalmetrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jenkinsci.plugins.additionalmetrics.PipelineDefinitions.*;
import static org.jenkinsci.plugins.additionalmetrics.UIHelpers.*;
import static org.jenkinsci.plugins.additionalmetrics.Utilities.TIME_UNITS;
import static org.junit.jupiter.api.Assertions.*;

import hudson.model.FreeStyleProject;
import hudson.model.ListView;
import hudson.plugins.git.GitSCM;
import org.htmlunit.html.DomNode;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.SleepBuilder;
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
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithTwoSuccessfulBuilds");
        project.setDefinition(checkout());
        project.scheduleBuild2(0).get();
        project.scheduleBuild2(0).get();

        Duration avgDuration = avgCheckoutDurationColumn.getAverageCheckoutDuration(project);

        assertThat(avgDuration.getAsLong()).isGreaterThan(0L);
    }

    @Test
    void failed_runs_are_not_excluded() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithOneFailedBuild");
        project.setDefinition(checkoutThenFail());
        project.scheduleBuild2(0).get();

        Duration avgDuration = avgCheckoutDurationColumn.getAverageCheckoutDuration(project);

        assertThat(avgDuration.getAsLong()).isGreaterThan(0L);
    }

    @Test
    void unstable_runs_are_not_excluded() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithOneUnstableBuild");
        project.setDefinition(checkoutThenUnstable());
        project.scheduleBuild2(0).get();

        Duration avgDuration = avgCheckoutDurationColumn.getAverageCheckoutDuration(project);

        assertThat(avgDuration.getAsLong()).isGreaterThan(0L);
    }

    @Test
    void freestyle_jobs_are_not_counted() throws Exception {
        FreeStyleProject project = jenkinsRule.createFreeStyleProject("FreestyleProjectWithOneBuild");
        project.setScm(new GitSCM("https://github.com/jenkinsci/additional-metrics-plugin.git"));
        project.getBuildersList().add(new SleepBuilder(200));
        project.scheduleBuild2(0).get();

        Duration avgDuration = avgCheckoutDurationColumn.getAverageCheckoutDuration(project);

        assertNull(avgDuration);
    }

    @Test
    void no_runs_should_display_as_NA_in_UI() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithZeroBuildsForUI");

        ListView listView =
                createAndAddListView(jenkinsRule.getInstance(), "MyListNoRuns", avgCheckoutDurationColumn, project);

        DomNode columnNode;
        try (JenkinsRule.WebClient webClient = jenkinsRule.createWebClient()) {
            columnNode = getListViewCell(
                    webClient.getPage(listView),
                    listView,
                    project.getName(),
                    avgCheckoutDurationColumn.getColumnCaption());
        }

        assertEquals("N/A", columnNode.asNormalizedText());
        assertEquals("0", columnNode.getAttributes().getNamedItem("data").getNodeValue());
    }

    @Test
    void one_run_should_display_avg_duration_in_UI() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithOneBuildForUI");
        project.setDefinition(checkout());
        project.scheduleBuild2(0).get();

        ListView listView =
                createAndAddListView(jenkinsRule.getInstance(), "MyListOneRun", avgCheckoutDurationColumn, project);

        DomNode columnNode;
        try (JenkinsRule.WebClient webClient = jenkinsRule.createWebClient()) {
            columnNode = getListViewCell(
                    webClient.getPage(listView),
                    listView,
                    project.getName(),
                    avgCheckoutDurationColumn.getColumnCaption());
        }

        // sample output: 1.1 sec
        String text = columnNode.asNormalizedText();

        assertThat(text).containsAnyOf(TIME_UNITS);
        assertThat(Long.parseLong(dataOf(columnNode))).isGreaterThan(0L);
    }
}
