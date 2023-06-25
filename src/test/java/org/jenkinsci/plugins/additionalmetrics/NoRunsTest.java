package org.jenkinsci.plugins.additionalmetrics;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.jenkinsci.plugins.additionalmetrics.Utilities.getColumns;
import static org.jenkinsci.plugins.additionalmetrics.Utilities.getMetricMethod;
import static org.junit.Assert.assertNull;

import hudson.views.ListViewColumn;
import java.lang.reflect.Method;
import java.util.Collection;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.jvnet.hudson.test.JenkinsRule;

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
