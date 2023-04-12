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
