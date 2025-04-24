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
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.JenkinsRule.WebClient;
import org.jvnet.hudson.test.SleepBuilder;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class MinCheckoutDurationColumnTest {

    private MinCheckoutDurationColumn minCheckoutDurationColumn;

    private static JenkinsRule jenkinsRule;

    @BeforeAll
    static void setUp(JenkinsRule rule) {
        jenkinsRule = rule;
    }

    @BeforeEach
    void before() {
        minCheckoutDurationColumn = new MinCheckoutDurationColumn();
    }

    @Test
    void one_run_with_checkout_should_return_checkout() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithCheckout");
        project.setDefinition(checkout());
        project.scheduleBuild2(0).get();

        RunWithDuration shortestCheckoutRun = minCheckoutDurationColumn.getShortestCheckoutRun(project);

        assertThat(shortestCheckoutRun.duration().getAsLong()).isGreaterThan(0L);
    }

    @Test
    void failed_runs_are_included_in_the_checkout_time_calculation() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithFailureAfterCheckout");
        project.setDefinition(checkoutThenFail());
        WorkflowRun run = project.scheduleBuild2(0).get();

        RunWithDuration shortestCheckoutRun = minCheckoutDurationColumn.getShortestCheckoutRun(project);

        assertSame(run, shortestCheckoutRun.run());
    }

    @Test
    void unstable_runs_are_included_in_the_checkout_time_calculation() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithUnstableAfterCheckout");
        project.setDefinition(checkoutThenUnstable());
        WorkflowRun run = project.scheduleBuild2(0).get();

        RunWithDuration shortestCheckoutRun = minCheckoutDurationColumn.getShortestCheckoutRun(project);

        assertSame(run, shortestCheckoutRun.run());
    }

    @Test
    void freestyle_jobs_are_not_counted() throws Exception {
        FreeStyleProject project = jenkinsRule.createFreeStyleProject("FreestyleProjectWithOneBuild");
        project.setScm(new GitSCM("https://github.com/jenkinsci/additional-metrics-plugin.git"));
        project.getBuildersList().add(new SleepBuilder(200));
        project.scheduleBuild2(0).get();

        RunWithDuration shortestCheckoutRun = minCheckoutDurationColumn.getShortestCheckoutRun(project);

        assertNull(shortestCheckoutRun);
    }

    @Test
    void no_runs_should_display_as_NA_in_UI() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithZeroBuildsForUI");

        ListView listView =
                createAndAddListView(jenkinsRule.getInstance(), "MyListNoRuns", minCheckoutDurationColumn, project);

        DomNode columnNode;
        try (WebClient webClient = jenkinsRule.createWebClient()) {
            columnNode = getListViewCell(
                    webClient.getPage(listView),
                    listView,
                    project.getName(),
                    minCheckoutDurationColumn.getColumnCaption());
        }

        assertEquals("N/A", columnNode.asNormalizedText());
        assertEquals("0", columnNode.getAttributes().getNamedItem("data").getNodeValue());
    }

    @Test
    void one_run_should_display_time_and_build_in_UI() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithOneBuildForUI");
        project.setDefinition(checkout());
        WorkflowRun run = project.scheduleBuild2(0).get();

        ListView listView =
                createAndAddListView(jenkinsRule.getInstance(), "MyListOneRun", minCheckoutDurationColumn, project);

        DomNode columnNode;
        try (WebClient webClient = jenkinsRule.createWebClient()) {
            columnNode = getListViewCell(
                    webClient.getPage(listView),
                    listView,
                    project.getName(),
                    minCheckoutDurationColumn.getColumnCaption());
        }

        // sample output: 1.1 sec - #1
        String text = columnNode.asNormalizedText();

        assertThat(text).containsAnyOf(TIME_UNITS);
        assertThat(text).contains("#" + run.getId());
        assertThat(Long.parseLong(dataOf(columnNode))).isGreaterThan(0L);
    }
}
