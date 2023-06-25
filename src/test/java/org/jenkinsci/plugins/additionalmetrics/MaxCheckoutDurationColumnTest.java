package org.jenkinsci.plugins.additionalmetrics;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.jenkinsci.plugins.additionalmetrics.PipelineDefinitions.*;
import static org.jenkinsci.plugins.additionalmetrics.UIHelpers.*;
import static org.junit.Assert.*;

import hudson.model.FreeStyleProject;
import hudson.model.ListView;
import hudson.plugins.git.GitSCM;
import org.htmlunit.html.DomNode;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.JenkinsRule.WebClient;
import org.jvnet.hudson.test.SleepBuilder;

public class MaxCheckoutDurationColumnTest {
    @ClassRule
    public static final JenkinsRule jenkinsRule = new JenkinsRule();

    private MaxCheckoutDurationColumn maxCheckoutDurationColumn;

    @Before
    public void before() {
        maxCheckoutDurationColumn = new MaxCheckoutDurationColumn();
    }

    @Test
    public void one_run_with_checkout_should_return_checkout() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithCheckout");
        project.setDefinition(checkoutDefinition());
        project.scheduleBuild2(0).get();

        RunWithDuration longestCheckoutRun = maxCheckoutDurationColumn.getLongestCheckoutRun(project);

        assertThat(longestCheckoutRun.getDuration().getAsLong(), greaterThan(0L));
    }

    @Test
    public void failed_runs_are_included_in_the_checkout_time_calculation() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithFailureAfterCheckout");
        project.setDefinition(checkoutThenFailDefinition());
        WorkflowRun run = project.scheduleBuild2(0).get();

        RunWithDuration longestCheckoutRun = maxCheckoutDurationColumn.getLongestCheckoutRun(project);

        assertSame(run, longestCheckoutRun.getRun());
    }

    @Test
    public void unstable_runs_are_included_in_the_checkout_time_calculation() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithUnstableAfterCheckout");
        project.setDefinition(checkoutThenUnstableDefinition());
        WorkflowRun run = project.scheduleBuild2(0).get();

        RunWithDuration longestCheckoutRun = maxCheckoutDurationColumn.getLongestCheckoutRun(project);

        assertSame(run, longestCheckoutRun.getRun());
    }

    @Test
    public void freestyle_jobs_are_not_counted() throws Exception {
        FreeStyleProject project = jenkinsRule.createFreeStyleProject("FreestyleProjectWithOneBuild");
        project.setScm(new GitSCM("https://github.com/jenkinsci/additional-metrics-plugin.git"));
        project.getBuildersList().add(new SleepBuilder(200));
        project.scheduleBuild2(0).waitForStart();

        RunWithDuration longestCheckoutRun = maxCheckoutDurationColumn.getLongestCheckoutRun(project);

        assertNull(longestCheckoutRun);
    }

    @Test
    public void no_runs_should_display_as_NA_in_UI() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithZeroBuildsForUI");

        ListView listView =
                createAndAddListView(jenkinsRule.getInstance(), "MyListNoRuns", maxCheckoutDurationColumn, project);

        DomNode columnNode;
        try (WebClient webClient = jenkinsRule.createWebClient()) {
            columnNode = getListViewCell(
                    webClient.getPage(listView),
                    listView,
                    project.getName(),
                    maxCheckoutDurationColumn.getColumnCaption());
        }

        assertEquals("N/A", columnNode.asNormalizedText());
        assertEquals("0", columnNode.getAttributes().getNamedItem("data").getNodeValue());
    }

    @Test
    public void one_run_should_display_time_and_build_in_UI() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithOneBuildForUI");
        project.setDefinition(checkoutDefinition());
        WorkflowRun run = project.scheduleBuild2(0).get();

        ListView listView =
                createAndAddListView(jenkinsRule.getInstance(), "MyListOneRun", maxCheckoutDurationColumn, project);

        DomNode columnNode;
        try (WebClient webClient = jenkinsRule.createWebClient()) {
            columnNode = getListViewCell(
                    webClient.getPage(listView),
                    listView,
                    project.getName(),
                    maxCheckoutDurationColumn.getColumnCaption());
        }

        // sample output: 1.1 sec - #1
        String text = columnNode.asNormalizedText();
        assertTrue(text.contains("sec"));
        assertTrue(text.contains("#" + run.getId()));

        assertThat(Long.parseLong(dataOf(columnNode)), greaterThan(0L));
    }
}
