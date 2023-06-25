package org.jenkinsci.plugins.additionalmetrics;

import com.google.common.collect.Iterables;
import hudson.views.ListViewColumn;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

class Utilities {
    static void terminateWorkflowRun(WorkflowRun workflowRun) {
        workflowRun.doTerm();
        workflowRun.doKill();
    }

    static Collection<Class<? extends ListViewColumn>> getColumns() {
        String packagePath = BuildingRunsTest.class.getPackage().getName();

        Reflections reflections =
                new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.forPackage(packagePath)));

        return reflections.getSubTypesOf(ListViewColumn.class);
    }

    static Method getMetricMethod(Class<? extends ListViewColumn> clazz) {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setScanners(Scanners.MethodsAnnotated)
                .setUrls(ClasspathHelper.forClass(clazz))
                .filterInputsBy(s -> s.equals(clazz.getName() + ".class")));

        Set<Method> methods = reflections.getMethodsAnnotatedWith(Metric.class);

        if (methods.isEmpty()) {
            throw new RuntimeException("Expected at least one method annotated with @Metric");
        }

        return Iterables.getOnlyElement(methods);
    }
}
