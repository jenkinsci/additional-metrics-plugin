package org.jenkinsci.plugins.additionalmetrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jenkinsci.plugins.additionalmetrics.Utilities.getColumns;
import static org.jenkinsci.plugins.additionalmetrics.Utilities.getMetricMethod;
import static org.junit.jupiter.api.Assertions.assertNull;

import hudson.views.ListViewColumn;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class NoRunsTest {

    static List<? extends Class<?>> data() throws IOException {
        List<? extends Class<?>> columns = getColumns();
        assertThat(columns).isNotEmpty();
        return columns;
    }

    private static JobRunner.WorkflowBuilder runner;

    @BeforeAll
    static void setUp(JenkinsRule rule) throws Exception {
        runner = JobRunner.createWorkflowJob(rule);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void no_runs_should_return_no_data(Class<? extends ListViewColumn> clazz) throws Exception {
        Object instance = clazz.getDeclaredConstructor().newInstance();
        Method method = getMetricMethod(clazz);

        Object res = method.invoke(instance, runner.getJob());

        assertNull(res);
    }
}
