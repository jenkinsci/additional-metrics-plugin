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

import hudson.views.ListViewColumn;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.jvnet.hudson.test.JenkinsRule;

import java.lang.reflect.Method;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.jenkinsci.plugins.additionalmetrics.Utilities.getColumns;
import static org.jenkinsci.plugins.additionalmetrics.Utilities.getMetricMethod;
import static org.junit.Assert.assertNull;

@RunWith(Parameterized.class)
public class NoRunsTest {
    @Parameters(name = "{0}")
    public static Iterable<Class<? extends ListViewColumn>> data() {
        Collection<Class<? extends ListViewColumn>> columns = getColumns();
        assertThat(columns, not(empty()));
        return columns;
    }

    @Parameter
    public Class<? extends ListViewColumn> clazz;

    private static WorkflowJob project;

    @ClassRule
    public static final JenkinsRule jenkinsRule = new JenkinsRule();

    @BeforeClass
    public static void initProject() throws Exception {
        project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithZeroBuilds");
    }

    @Test
    public void no_runs_should_return_no_data() throws Exception {
        Object instance = clazz.getDeclaredConstructor().newInstance();
        Method method = getMetricMethod(clazz);

        Object res = method.invoke(instance, project);

        assertNull(res);
    }

}
