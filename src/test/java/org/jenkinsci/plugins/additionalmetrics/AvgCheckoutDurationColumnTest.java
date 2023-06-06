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
import static org.jenkinsci.plugins.additionalmetrics.PipelineDefinitions.*;
import static org.jenkinsci.plugins.additionalmetrics.UIHelpers.*;
import static org.junit.Assert.*;

import hudson.model.FreeStyleProject;
import hudson.model.ListView;
import hudson.plugins.git.GitSCM;
import org.htmlunit.html.DomNode;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.SleepBuilder;

public class AvgCheckoutDurationColumnTest {
    @ClassRule
    public static final JenkinsRule jenkinsRule = new JenkinsRule();

    private AvgCheckoutDurationColumn avgCheckoutDurationColumn;

    @Before
    public void before() {
        avgCheckoutDurationColumn = new AvgCheckoutDurationColumn();
    }

    @Test
    public void two_successful_runs_should_return_a_positive_average_checkout_duration() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithTwoSuccessfulBuilds");
        project.setDefinition(checkoutDefinition());
        project.scheduleBuild2(0).get();
        project.setDefinition(checkoutDefinition());
        project.scheduleBuild2(0).get();

        Duration avgDuration = avgCheckoutDurationColumn.getAverageCheckoutDuration(project);

        assertThat(avgDuration.getAsLong(), greaterThan(0L));
    }

    @Test
    public void failed_runs_are_not_excluded() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithOneFailedBuild");
        project.setDefinition(checkoutThenFailDefinition());
        project.scheduleBuild2(0).get();

        Duration avgDuration = avgCheckoutDurationColumn.getAverageCheckoutDuration(project);

        assertThat(avgDuration.getAsLong(), greaterThan(0L));
    }

    @Test
    public void unstable_runs_are_not_excluded() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithOneUnstableBuild");
        project.setDefinition(checkoutThenUnstableDefinition());
        project.scheduleBuild2(0).get();

        Duration avgDuration = avgCheckoutDurationColumn.getAverageCheckoutDuration(project);

        assertThat(avgDuration.getAsLong(), greaterThan(0L));
    }

    @Test
    public void freestyle_jobs_are_not_counted() throws Exception {
        FreeStyleProject project = jenkinsRule.createFreeStyleProject("FreestyleProjectWithOneBuild");
        project.setScm(new GitSCM("https://github.com/jenkinsci/additional-metrics-plugin.git"));
        project.getBuildersList().add(new SleepBuilder(200));
        project.scheduleBuild2(0).waitForStart();

        Duration avgDuration = avgCheckoutDurationColumn.getAverageCheckoutDuration(project);

        assertNull(avgDuration);
    }

    @Test
    public void no_runs_should_display_as_NA_in_UI() throws Exception {
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
    public void one_run_should_display_avg_duration_in_UI() throws Exception {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithOneBuildForUI");
        project.setDefinition(checkoutDefinition());
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
        assertTrue(text.contains("sec"));

        assertThat(Long.parseLong(dataOf(columnNode)), greaterThan(0L));
    }
}
