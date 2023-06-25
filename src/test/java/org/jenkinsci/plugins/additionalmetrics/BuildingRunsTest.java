package org.jenkinsci.plugins.additionalmetrics;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.jenkinsci.plugins.additionalmetrics.PipelineDefinitions.slowDefinition;
import static org.jenkinsci.plugins.additionalmetrics.Utilities.getColumns;
import static org.jenkinsci.plugins.additionalmetrics.Utilities.getMetricMethod;
import static org.junit.Assert.assertNull;

import hudson.views.ListViewColumn;
import java.lang.reflect.Method;
import java.util.Collection;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.jvnet.hudson.test.JenkinsRule;

@RunWith(Parameterized.class)
public class BuildingRunsTest {

    @Parameters(name = "{0}")
    public static Iterable<Class<? extends ListViewColumn>> data() {
        Collection<Class<? extends ListViewColumn>> columns = getColumns();
        assertThat(columns, not(empty()));
        return columns;
    }

    @Parameter
    public Class<? extends ListViewColumn> clazz;

    private static WorkflowJob project;
    private static WorkflowRun workflowRun;

    @ClassRule
    public static final JenkinsRule jenkinsRule = new JenkinsRule();

    @BeforeClass
    public static void initProject() throws Exception {
        project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithOneBuildingBuild");
        project.setDefinition(slowDefinition());

        workflowRun = project.scheduleBuild2(0).waitForStart();
    }

    @AfterClass
    public static void stopRun() {
        Utilities.terminateWorkflowRun(workflowRun);
    }

    @Test
    public void building_runs_should_be_excluded() throws Exception {
        Object instance = clazz.getDeclaredConstructor().newInstance();
        Method method = getMetricMethod(clazz);

        Object res = method.invoke(instance, project);

        assertNull(res);
    }
}
