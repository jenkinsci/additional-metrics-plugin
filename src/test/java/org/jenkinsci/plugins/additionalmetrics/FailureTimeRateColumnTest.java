/*
 * MIT License
 *
 * Copyright (c) 2022 Chadi El Masri
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.jenkinsci.plugins.additionalmetrics;

import com.gargoylesoftware.htmlunit.html.DomNode;
import hudson.model.ListView;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.jenkinsci.plugins.additionalmetrics.PipelineDefinitions.*;
import static org.jenkinsci.plugins.additionalmetrics.UIHelpers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class FailureTimeRateColumnTest {
    @ClassRule
    public static final JenkinsRule jenkinsRule = new JenkinsRule();

    private FailureTimeRateColumn failureTimeRateColumn;

    @Before
    public void before() {
        failureTimeRateColumn = new FailureTimeRateColumn();
    }

    @Test
    public void no_runs_should_return_no_data() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithZeroBuilds");

        Rate failureTimeRate = failureTimeRateColumn.getFailureTimeRate(project);

        assertNull(failureTimeRate);
    }

    @Test
    public void one_success_run_failure_time_rate_should_be_0_percent() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithOneSuccess");
        project.setDefinition(successDefinition());
        project.scheduleBuild2(0).get();

        Rate failureTimeRate = failureTimeRateColumn.getFailureTimeRate(project);

        assertEquals(0, failureTimeRate.getAsDouble(), 0);
    }

    @Test
    public void two_success_runs_failure_time_rate_should_be_0_percent() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithTwoSuccess");
        project.setDefinition(successDefinition());
        project.scheduleBuild2(0).get();
        project.scheduleBuild2(0).get();

        Rate failureTimeRate = failureTimeRateColumn.getFailureTimeRate(project);

        assertEquals(0, failureTimeRate.getAsDouble(), 0);
    }

    @Test
    public void one_success_run_followed_by_one_failure_run() throws Exception {
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
    public void building_runs_should_be_excluded() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithOneBuildingBuild");
        project.setDefinition(slowDefinition());
        WorkflowRun workflowRun = project.scheduleBuild2(0).waitForStart();

        Rate failureTimeRate = failureTimeRateColumn.getFailureTimeRate(project);

        assertNull(failureTimeRate);

        Utilities.terminateWorkflowRun(workflowRun);
    }

    @Test
    public void no_runs_should_display_as_NA_in_UI() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithZeroBuildsForUI");

        ListView listView = createAndAddListView(jenkinsRule.getInstance(), "MyListNoRuns", failureTimeRateColumn, project);

        DomNode columnNode;
        try (JenkinsRule.WebClient webClient = jenkinsRule.createWebClient()) {
            columnNode = getListViewCell(webClient.getPage(listView), listView, project.getName(), failureTimeRateColumn.getColumnCaption());
        }

        assertEquals("N/A", columnNode.asNormalizedText());
        assertEquals("0.0", columnNode.getAttributes().getNamedItem("data").getNodeValue());
    }

    @Test
    public void one_run_should_display_percentage_in_UI() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithOneBuildForUI");
        project.setDefinition(failingDefinition());
        project.scheduleBuild2(0).get();

        ListView listView = createAndAddListView(jenkinsRule.getInstance(), "MyListOneRun", failureTimeRateColumn, project);

        DomNode columnNode;
        try (JenkinsRule.WebClient webClient = jenkinsRule.createWebClient()) {
            columnNode = getListViewCell(webClient.getPage(listView), listView, project.getName(), failureTimeRateColumn.getColumnCaption());
        }

        assertEquals("100.00%", columnNode.asNormalizedText());
        assertEquals("1.0", dataOf(columnNode));
    }

}