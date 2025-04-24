package org.jenkinsci.plugins.additionalmetrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jenkinsci.plugins.additionalmetrics.PipelineDefinitions.verySlow;
import static org.jenkinsci.plugins.additionalmetrics.Utilities.getColumns;
import static org.jenkinsci.plugins.additionalmetrics.Utilities.getMetricMethod;
import static org.junit.jupiter.api.Assertions.assertNull;

import hudson.views.ListViewColumn;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class BuildingRunsTest {

    static List<? extends Class<?>> data() throws IOException {
        List<? extends Class<?>> columns = getColumns();
        assertThat(columns).isNotEmpty();
        return columns;
    }

    private static WorkflowJob project;
    private static WorkflowRun workflowRun;

    @BeforeAll
    static void setUp(JenkinsRule rule) throws Exception {
        project = rule.createProject(WorkflowJob.class, "ProjectWithOneBuildingBuild");
        project.setDefinition(verySlow());

        workflowRun = project.scheduleBuild2(0).waitForStart();
    }

    @AfterAll
    static void stopRun() {
        Utilities.terminateWorkflowRun(workflowRun);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void building_runs_should_be_excluded(Class<? extends ListViewColumn> clazz) throws Exception {
        Object instance = clazz.getDeclaredConstructor().newInstance();
        Method method = getMetricMethod(clazz);

        Object res = method.invoke(instance, project);

        assertNull(res);
    }
}
