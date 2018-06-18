/*
 * MIT License
 *
 * Copyright (c) 2018 Chadi El Masri
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

import hudson.model.ListView;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.jenkinsci.plugins.additionalmetrics.PipelineDefinitions.*;
import static org.jenkinsci.plugins.additionalmetrics.UIHelpers.createAndAddListView;
import static org.jenkinsci.plugins.additionalmetrics.UIHelpers.getListViewCellValue;
import static org.junit.Assert.*;

public class SuccessRateColumnTest {
    @ClassRule
    public static final JenkinsRule jenkinsRule = new JenkinsRule();

    private SuccessRateColumn successRateColumn;

    @Before
    public void before() {
        successRateColumn = new SuccessRateColumn();
    }

    @Test
    public void no_runs_should_return_no_data() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithZeroBuilds");

        Rate successRate = successRateColumn.getSuccessRate(project);

        assertNull(successRate);
    }

    @Test
    public void one_failed_job_over_two_success_rate_should_be_50_percent() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithOneOverTwoSuccess");
        project.setDefinition(failingDefinition());
        project.scheduleBuild2(0).get();
        project.setDefinition(successDefinition());
        project.scheduleBuild2(0).get();

        Rate successRate = successRateColumn.getSuccessRate(project);

        assertEquals(0.5, successRate.get(), 0);
    }

    @Test
    public void building_runs_should_be_excluded() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithOneBuildingBuild");
        project.setDefinition(slowDefinition());
        project.scheduleBuild2(0).waitForStart();

        Rate successRate = successRateColumn.getSuccessRate(project);

        assertNull(successRate);
    }

    @Test
    public void unstable_run_are_considered_failures() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithOneUnstableBuild");
        project.setDefinition(unstableDefinition());
        project.scheduleBuild2(0).get();

        Rate successRate = successRateColumn.getSuccessRate(project);

        assertEquals(0, successRate.get(), 0);
    }

    @Test
    public void no_runs_should_display_as_NA_in_UI() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithZeroBuildsForUI");

        ListView listView = createAndAddListView(jenkinsRule.getInstance(), "MyListNoRuns", successRateColumn, project);

        String textOnUi;
        try (JenkinsRule.WebClient webClient = jenkinsRule.createWebClient()) {
            textOnUi = getListViewCellValue(webClient.getPage(listView), listView, project.getName(), successRateColumn.getColumnCaption());
        }

        assertEquals("N/A", textOnUi);
    }

    @Test
    public void one_run_should_display_time_and_build_in_UI() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithOneBuildForUI");
        project.setDefinition(sleepDefinition(1));
        project.scheduleBuild2(0).get();

        ListView listView = createAndAddListView(jenkinsRule.getInstance(), "MyListOneRun", successRateColumn, project);

        String textOnUi;
        try (JenkinsRule.WebClient webClient = jenkinsRule.createWebClient()) {
            textOnUi = getListViewCellValue(webClient.getPage(listView), listView, project.getName(), successRateColumn.getColumnCaption());
        }

        // sample output: 0.00%
        assertTrue(textOnUi.contains("%"));
    }

}