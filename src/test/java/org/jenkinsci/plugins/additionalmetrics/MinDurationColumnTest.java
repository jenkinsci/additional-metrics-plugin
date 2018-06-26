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

import com.gargoylesoftware.htmlunit.html.DomNode;
import hudson.model.ListView;
import hudson.model.Run;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.JenkinsRule.WebClient;

import static org.jenkinsci.plugins.additionalmetrics.PipelineDefinitions.*;
import static org.jenkinsci.plugins.additionalmetrics.UIHelpers.createAndAddListView;
import static org.jenkinsci.plugins.additionalmetrics.UIHelpers.getListViewCell;
import static org.junit.Assert.*;

public class MinDurationColumnTest {
    @ClassRule
    public static final JenkinsRule jenkinsRule = new JenkinsRule();

    private MinDurationColumn minDurationColumn;

    @Before
    public void before() {
        minDurationColumn = new MinDurationColumn();
    }

    @Test
    public void no_runs_should_return_no_data() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithZeroBuilds");

        Run<?, ?> shortestRun = minDurationColumn.getShortestRun(project);

        assertNull(shortestRun);
    }

    @Test
    public void two_successful_runs_should_return_the_shortest() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithTwoSuccessfulBuilds");
        project.setDefinition(sleepDefinition(1));
        WorkflowRun run1 = project.scheduleBuild2(0).get();
        project.setDefinition(sleepDefinition(3));
        project.scheduleBuild2(0).get();

        Run<?, ?> shortestRun = minDurationColumn.getShortestRun(project);

        assertSame(run1, shortestRun);
    }

    @Test
    public void two_runs_including_one_failure_should_return_the_shortest() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithTwoBuildsOneFailure");
        project.setDefinition(sleepThenFailDefinition(1));
        WorkflowRun run1 = project.scheduleBuild2(0).get();
        project.setDefinition(sleepDefinition(3));
        project.scheduleBuild2(0).get();

        Run<?, ?> shortestRun = minDurationColumn.getShortestRun(project);

        assertSame(run1, shortestRun);
    }

    @Test
    public void failed_runs_are_not_excluded() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithOneFailedBuild");
        project.setDefinition(failingDefinition());
        WorkflowRun run = project.scheduleBuild2(0).get();

        Run<?, ?> shortestRun = minDurationColumn.getShortestRun(project);

        assertSame(run, shortestRun);
    }

    @Test
    public void unstable_runs_are_not_excluded() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithOneUnstableBuild");
        project.setDefinition(unstableDefinition());
        WorkflowRun run = project.scheduleBuild2(0).get();

        Run<?, ?> shortestRun = minDurationColumn.getShortestRun(project);

        assertSame(run, shortestRun);
    }

    @Test
    public void building_runs_should_be_excluded() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithOneBuildingBuild");
        project.setDefinition(slowDefinition());
        project.scheduleBuild2(0).waitForStart();

        Run<?, ?> shortestRun = minDurationColumn.getShortestRun(project);

        assertNull(shortestRun);
    }

    @Test
    public void no_runs_should_display_as_NA_in_UI() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithZeroBuildsForUI");

        ListView listView = createAndAddListView(jenkinsRule.getInstance(), "MyListNoRuns", minDurationColumn, project);

        DomNode columnNode;
        try (WebClient webClient = jenkinsRule.createWebClient()) {
            columnNode = getListViewCell(webClient.getPage(listView), listView, project.getName(), minDurationColumn.getColumnCaption());
        }

        assertEquals("N/A", columnNode.asText());
        assertEquals("0", columnNode.getAttributes().getNamedItem("data").getNodeValue());
    }

    @Test
    public void one_run_should_display_time_and_build_in_UI() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithOneBuildForUI");
        project.setDefinition(sleepDefinition(1));
        WorkflowRun run = project.scheduleBuild2(0).get();

        ListView listView = createAndAddListView(jenkinsRule.getInstance(), "MyListOneRun", minDurationColumn, project);

        DomNode columnNode;
        try (WebClient webClient = jenkinsRule.createWebClient()) {
            columnNode = getListViewCell(webClient.getPage(listView), listView, project.getName(), minDurationColumn.getColumnCaption());
        }

        // sample output: 1.1 sec - #1
        String text = columnNode.asText();
        assertTrue(text.contains("sec"));
        assertTrue(text.contains("#" + run.getId()));

        assertTrue(Long.parseLong(columnNode.getAttributes().getNamedItem("data").getNodeValue()) > 0);
    }

}