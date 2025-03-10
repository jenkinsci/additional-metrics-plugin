package org.jenkinsci.plugins.additionalmetrics;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.jenkinsci.plugins.additionalmetrics.Utilities.getColumns;
import static org.jenkinsci.plugins.additionalmetrics.Utilities.getMetricMethod;
import static org.junit.jupiter.api.Assertions.assertNull;

import hudson.views.ListViewColumn;
import java.lang.reflect.Method;
import java.util.Collection;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class NoRunsTest {

    static Iterable<Class<? extends ListViewColumn>> data() {
        Collection<Class<? extends ListViewColumn>> columns = getColumns();
        assertThat(columns, not(empty()));
        return columns;
    }

    private static WorkflowJob project;

    private static JenkinsRule jenkinsRule;

    @BeforeAll
    static void setUp(JenkinsRule rule) throws Exception {
        jenkinsRule = rule;

        project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithZeroBuilds");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void no_runs_should_return_no_data(Class<? extends ListViewColumn> clazz) throws Exception {
        Object instance = clazz.getDeclaredConstructor().newInstance();
        Method method = getMetricMethod(clazz);

        Object res = method.invoke(instance, project);

        assertNull(res);
    }
}
