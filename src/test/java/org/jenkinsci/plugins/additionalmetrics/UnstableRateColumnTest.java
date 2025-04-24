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
class UnstableRateColumnTest {

    private UnstableRateColumn unstableRateColumn;

    private static JenkinsRule jenkinsRule;

    @BeforeAll
    static void setUp(JenkinsRule rule) {
        jenkinsRule = rule;
    }

    @BeforeEach
    void before() {
        unstableRateColumn = new UnstableRateColumn();
    }

    @Test
    void no_unstable_job_should_be_0_percent() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithNoUnstableBuilds");
        project.setDefinition(failure());
        project.scheduleBuild2(0).get();
        project.setDefinition(success());
        project.scheduleBuild2(0).get();
        Rate unstableRate = unstableRateColumn.getUnstableRate(project);
        assertEquals(0.0, unstableRate.getAsDouble(), 0);
    }

    @Test
    void one_unstable_job_over_two_failed_should_be_50_percent() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithOneUnstableJob");
        project.setDefinition(unstable());
        project.scheduleBuild2(0).get();
        project.setDefinition(failure());
        project.scheduleBuild2(0).get();
        Rate unstableRate = unstableRateColumn.getUnstableRate(project);
        assertEquals(0.5, unstableRate.getAsDouble(), 0);
    }

    @Test
    void no_runs_should_display_as_NA_in_UI() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithZeroBuildsForUI");

        ListView listView =
                createAndAddListView(jenkinsRule.getInstance(), "MyListNoRuns", unstableRateColumn, project);

        DomNode columnNode;
        try (JenkinsRule.WebClient webClient = jenkinsRule.createWebClient()) {
            columnNode = getListViewCell(
                    webClient.getPage(listView), listView, project.getName(), unstableRateColumn.getColumnCaption());
        }

        assertEquals("N/A", columnNode.asNormalizedText());
        assertEquals("0.0", columnNode.getAttributes().getNamedItem("data").getNodeValue());
    }

    @Test
    void one_unstable_over_one_failed_should_display_percentage_in_UI() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithOneBuildForUI");
        project.setDefinition(unstable());
        project.scheduleBuild2(0).get();

        ListView listView =
                createAndAddListView(jenkinsRule.getInstance(), "MyListOneRun", unstableRateColumn, project);

        DomNode columnNode;
        try (JenkinsRule.WebClient webClient = jenkinsRule.createWebClient()) {
            columnNode = getListViewCell(
                    webClient.getPage(listView), listView, project.getName(), unstableRateColumn.getColumnCaption());
        }

        assertEquals("100.00%", columnNode.asNormalizedText());
        assertEquals("1.0", dataOf(columnNode));
    }

    @Test
    void one_unstable_job_over_four_jobs_should_be_25_percent() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithFourJobs");
        project.setDefinition(unstable());
        project.scheduleBuild2(0).get();
        project.setDefinition(failure());
        project.scheduleBuild2(0).get();
        project.scheduleBuild2(0).get();
        project.setDefinition(success());
        project.scheduleBuild2(0).get();

        Rate unstableRate = unstableRateColumn.getUnstableRate(project);
        assertEquals(0.25, unstableRate.getAsDouble(), 0);
    }
}
