package org.jenkinsci.plugins.additionalmetrics;

import com.google.common.collect.Iterables;
import com.google.common.reflect.ClassPath;
import hudson.views.ListViewColumn;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

class Utilities {
    static final CharSequence[] TIME_UNITS = {" sec", " ms"};

    static void terminateWorkflowRun(WorkflowRun workflowRun) {
        workflowRun.doTerm();
        workflowRun.doKill();
    }

    static List<? extends Class<?>> getColumns() throws IOException {
        Package p = Utilities.class.getPackage();

        return ClassPath.from(ClassLoader.getSystemClassLoader()).getAllClasses().stream()
                .filter(cl -> cl.getPackageName().equals(p.getName()))
                .map(ClassPath.ClassInfo::load)
                .filter(ListViewColumn.class::isAssignableFrom)
                .toList();
    }

    static Method getMetricMethod(Class<? extends ListViewColumn> clazz) {
        List<Method> methods = Arrays.stream(clazz.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(Metric.class))
                .toList();

        if (methods.isEmpty()) {
            throw new RuntimeException("Expected at least one method annotated with @Metric");
        }

        return Iterables.getOnlyElement(methods);
    }
}
