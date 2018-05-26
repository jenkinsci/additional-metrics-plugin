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

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.ListView;
import hudson.model.Run;
import hudson.tasks.Shell;
import hudson.views.ListViewColumn;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class MaxSuccessDurationColumnTest {
    @ClassRule
    public static JenkinsRule jenkinsRule = new JenkinsRule();

    @Test
    public void no_runs_should_return_no_data() throws Exception {
        FreeStyleProject project = jenkinsRule.createFreeStyleProject("ProjectWithZeroBuilds");
        MaxSuccessDurationColumn maxSuccessDurationColumn = new MaxSuccessDurationColumn();

        Run<?, ?> longestRun = maxSuccessDurationColumn.getLongestSuccessfulRun(project);

        assertNull(longestRun);
    }

    @Test
    public void two_successful_runs_should_return_the_longest() throws Exception {
        FreeStyleProject project = jenkinsRule.createFreeStyleProject("ProjectWithTwoSuccessfulBuilds");
        project.getBuildersList().add(new Shell("sleep 1"));
        FreeStyleBuild run1 = project.scheduleBuild2(0).get();
        project.getBuildersList().replace(new Shell("sleep 3"));
        FreeStyleBuild run2 = project.scheduleBuild2(0).get();

        MaxSuccessDurationColumn maxSuccessDurationColumn = new MaxSuccessDurationColumn();
        Run<?, ?> longestRun = maxSuccessDurationColumn.getLongestSuccessfulRun(project);

        assertSame(run2, longestRun);
    }

    @Test
    public void failed_runs_should_be_excluded() throws Exception {
        FreeStyleProject project = jenkinsRule.createFreeStyleProject("ProjectWithOneFailedBuild");
        project.getBuildersList().add(new Shell("ech syntax error"));
        project.scheduleBuild2(0).get();
        MaxSuccessDurationColumn maxSuccessDurationColumn = new MaxSuccessDurationColumn();

        Run<?, ?> longestRun = maxSuccessDurationColumn.getLongestSuccessfulRun(project);

        assertNull(longestRun);
    }

    @Test
    public void building_runs_should_be_excluded() throws Exception {
        FreeStyleProject project = jenkinsRule.createFreeStyleProject("ProjectWithOneBuildingBuild");
        project.getBuildersList().add(new Shell("sleep 60"));
        project.scheduleBuild2(0).waitForStart();
        MaxSuccessDurationColumn maxSuccessDurationColumn = new MaxSuccessDurationColumn();

        Run<?, ?> longestRun = maxSuccessDurationColumn.getLongestSuccessfulRun(project);

        assertNull(longestRun);
    }

    @Test
    public void no_runs_should_display_as_NA_in_UI() throws Exception {
        FreeStyleProject project = jenkinsRule.createFreeStyleProject("ProjectWithZeroBuildsForUI");
        MaxSuccessDurationColumn maxSuccessDurationColumn = new MaxSuccessDurationColumn();

        ListView myList = new ListView("MyListNoRuns", jenkinsRule.getInstance());
        myList.getColumns().add(maxSuccessDurationColumn);
        myList.add(project);

        jenkinsRule.getInstance().addView(myList);

        String textOnUi = getCellValue(myList, project.getName(), maxSuccessDurationColumn.getColumnCaption());

        assertEquals("N/A", textOnUi);
    }

    @Test
    public void one_run_should_display_time_and_build_in_UI() throws Exception {
        FreeStyleProject project = jenkinsRule.createFreeStyleProject("ProjectWithOneBuildForUI");
        project.getBuildersList().add(new Shell("sleep 1"));
        FreeStyleBuild run = project.scheduleBuild2(0).get();
        MaxSuccessDurationColumn maxSuccessDurationColumn = new MaxSuccessDurationColumn();

        ListView myList = new ListView("MyListOneRun", jenkinsRule.getInstance());
        myList.getColumns().add(maxSuccessDurationColumn);
        myList.add(project);

        jenkinsRule.getInstance().addView(myList);

        String textOnUi = getCellValue(myList, project.getName(), maxSuccessDurationColumn.getColumnCaption());

        // sample output: 1.1 sec - #1
        assertTrue(textOnUi.contains("sec"));
        assertTrue(textOnUi.contains("#" + run.getId()));
    }

    private String getCellValue(ListView view, String jobName, String fieldName) throws IOException, SAXException {
        int i = 0;
        Map<String, Integer> textToIndex = new HashMap<>();
        for (ListViewColumn column : view.getColumns()) {
            textToIndex.put(column.getColumnCaption(), i++);
        }

        try (JenkinsRule.WebClient webClient = jenkinsRule.createWebClient()) {
            HtmlPage page = webClient.getPage(view);

            DomElement tr = page.getElementById("job_" + jobName);
            DomNode td = tr.getChildNodes().get(textToIndex.get(fieldName));

            return td.asText();
        }
    }

}