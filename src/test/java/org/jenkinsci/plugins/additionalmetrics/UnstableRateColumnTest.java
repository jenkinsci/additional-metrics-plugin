package org.jenkinsci.plugins.additionalmetrics;

import static org.jenkinsci.plugins.additionalmetrics.JobRunner.WorkflowBuilder.StepDefinitions.*;
import static org.jenkinsci.plugins.additionalmetrics.UIHelpers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import hudson.model.ListView;
import org.htmlunit.html.DomNode;
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
        var runner = JobRunner.createWorkflowJob(jenkinsRule)
                .configurePipelineDefinition(FAILURE)
                .schedule()
                .configurePipelineDefinition(SUCCESS)
                .schedule();

        Rate unstableRate = unstableRateColumn.getUnstableRate(runner.getJob());

        assertEquals(0.0, unstableRate.getAsDouble(), 0);
    }

    @Test
    void one_unstable_job_over_two_failed_should_be_50_percent() throws Exception {
        var runner = JobRunner.createWorkflowJob(jenkinsRule)
                .configurePipelineDefinition(UNSTABLE)
                .schedule()
                .configurePipelineDefinition(FAILURE)
                .schedule();

        Rate unstableRate = unstableRateColumn.getUnstableRate(runner.getJob());

        assertEquals(0.5, unstableRate.getAsDouble(), 0);
    }

    @Test
    void no_runs_should_display_as_NA_in_UI() throws Exception {
        var runner = JobRunner.createWorkflowJob(jenkinsRule);

        ListView listView =
                createAndAddListView(jenkinsRule.getInstance(), "MyListNoRuns", unstableRateColumn, runner.getJob());

        DomNode columnNode;
        try (JenkinsRule.WebClient webClient = jenkinsRule.createWebClient()) {
            columnNode = getListViewCell(
                    webClient.getPage(listView),
                    listView,
                    runner.getJob().getName(),
                    unstableRateColumn.getColumnCaption());
        }

        assertEquals("N/A", columnNode.asNormalizedText());
        assertEquals("0.0", columnNode.getAttributes().getNamedItem("data").getNodeValue());
    }

    @Test
    void one_unstable_over_one_failed_should_display_percentage_in_UI() throws Exception {
        var runner = JobRunner.createWorkflowJob(jenkinsRule)
                .configurePipelineDefinition(UNSTABLE)
                .schedule();

        ListView listView =
                createAndAddListView(jenkinsRule.getInstance(), "MyListOneRun", unstableRateColumn, runner.getJob());

        DomNode columnNode;
        try (JenkinsRule.WebClient webClient = jenkinsRule.createWebClient()) {
            columnNode = getListViewCell(
                    webClient.getPage(listView),
                    listView,
                    runner.getJob().getName(),
                    unstableRateColumn.getColumnCaption());
        }

        assertEquals("100.00%", columnNode.asNormalizedText());
        assertEquals("1.0", dataOf(columnNode));
    }

    @Test
    void one_unstable_job_over_four_jobs_should_be_25_percent() throws Exception {
        var runner = JobRunner.createWorkflowJob(jenkinsRule)
                .configurePipelineDefinition(UNSTABLE)
                .schedule()
                .configurePipelineDefinition(FAILURE)
                .schedule()
                .schedule()
                .configurePipelineDefinition(SUCCESS)
                .schedule();

        Rate unstableRate = unstableRateColumn.getUnstableRate(runner.getJob());

        assertEquals(0.25, unstableRate.getAsDouble(), 0);
    }
}
