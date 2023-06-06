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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.jenkinsci.plugins.additionalmetrics.PipelineDefinitions.failingDefinition;
import static org.jenkinsci.plugins.additionalmetrics.PipelineDefinitions.sleepDefinition;
import static org.jenkinsci.plugins.additionalmetrics.PipelineDefinitions.unstableDefinition;
import static org.jenkinsci.plugins.additionalmetrics.UIHelpers.createAndAddListView;
import static org.jenkinsci.plugins.additionalmetrics.UIHelpers.dataOf;
import static org.jenkinsci.plugins.additionalmetrics.UIHelpers.getListViewCell;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import hudson.model.ListView;
import org.htmlunit.html.DomNode;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class StdevSuccessDurationTest {

    @ClassRule
    public static final JenkinsRule jenkinsRule = new JenkinsRule();

    private StdevSuccessDurationColumn stdevSuccessDurationColumn;

    @Before
    public void before() {
        stdevSuccessDurationColumn = new StdevSuccessDurationColumn();
    }

    @Test
    public void two_successful_runs_should_return_their_sd_duration() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithTwoSuccessfulBuilds");
        project.setDefinition(sleepDefinition(1));
        WorkflowRun run1 = project.scheduleBuild2(0).get();
        project.setDefinition(sleepDefinition(6));
        WorkflowRun run2 = project.scheduleBuild2(0).get();

        Duration stdevDuration = stdevSuccessDurationColumn.getStdevSuccessDuration(project);
        assertEquals(
                (long) MathCommons.standardDeviation(ImmutableList.of(run1.getDuration(), run2.getDuration()))
                        .getAsDouble(),
                stdevDuration.getAsLong());
    }

    @Test
    public void failed_runs_should_be_excluded() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithOneFailedBuild");
        project.setDefinition(failingDefinition());
        project.scheduleBuild2(0).get();

        Duration stdevDuration = stdevSuccessDurationColumn.getStdevSuccessDuration(project);
        assertNull(stdevDuration);
    }

    @Test
    public void unstable_runs_should_be_excluded() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithOneUnstableBuild");
        project.setDefinition(unstableDefinition());
        project.scheduleBuild2(0).get();

        Duration stdevDuration = stdevSuccessDurationColumn.getStdevSuccessDuration(project);
        assertNull(stdevDuration);
    }

    @Test
    public void no_runs_should_display_as_NA_in_UI() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithZeroBuildsForUI");

        ListView listView =
                createAndAddListView(jenkinsRule.getInstance(), "MyListNoRuns", stdevSuccessDurationColumn, project);

        DomNode columnNode;
        try (JenkinsRule.WebClient webClient = jenkinsRule.createWebClient()) {
            columnNode = getListViewCell(
                    webClient.getPage(listView),
                    listView,
                    project.getName(),
                    stdevSuccessDurationColumn.getColumnCaption());
        }

        assertEquals("N/A", columnNode.asNormalizedText());
        assertEquals("0", columnNode.getAttributes().getNamedItem("data").getNodeValue());
    }

    @Test
    public void one_run_should_display_as_0_in_UI() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithOneBuildForUI");
        project.setDefinition(sleepDefinition(1));
        project.scheduleBuild2(0).get();

        ListView listView =
                createAndAddListView(jenkinsRule.getInstance(), "MyListOneRun", stdevSuccessDurationColumn, project);

        DomNode columnNode;
        try (JenkinsRule.WebClient webClient = jenkinsRule.createWebClient()) {
            columnNode = getListViewCell(
                    webClient.getPage(listView),
                    listView,
                    project.getName(),
                    stdevSuccessDurationColumn.getColumnCaption());
        }

        assertEquals("0 ms", columnNode.asNormalizedText());
        assertEquals("0", columnNode.getAttributes().getNamedItem("data").getNodeValue());
    }

    @Test
    public void two_runs_should_display_sd_duration_in_UI() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithTwoBuildsForUI");
        project.setDefinition(sleepDefinition(1));
        project.scheduleBuild2(0).get();
        project.setDefinition(sleepDefinition(3));
        project.scheduleBuild2(0).get();

        ListView listView =
                createAndAddListView(jenkinsRule.getInstance(), "MyListTwoRuns", stdevSuccessDurationColumn, project);

        DomNode columnNode;
        try (JenkinsRule.WebClient webClient = jenkinsRule.createWebClient()) {
            columnNode = getListViewCell(
                    webClient.getPage(listView),
                    listView,
                    project.getName(),
                    stdevSuccessDurationColumn.getColumnCaption());
        }

        // sample output: 1.1 sec
        String text = columnNode.asNormalizedText();
        assertTrue(text.contains("sec"));
        assertThat(Long.parseLong(dataOf(columnNode)), greaterThan(0L));
    }
}
