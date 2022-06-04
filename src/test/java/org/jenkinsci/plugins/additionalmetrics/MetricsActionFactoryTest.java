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

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.xml.XmlPage;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.jenkinsci.plugins.additionalmetrics.PipelineDefinitions.checkoutDefinition;
import static org.jenkinsci.plugins.additionalmetrics.PipelineDefinitions.successDefinition;

public class MetricsActionFactoryTest {
    @ClassRule
    public static final JenkinsRule jenkinsRule = new JenkinsRule();

    @Test
    public void no_runs_metrics_should_be_zeros() throws IOException, SAXException {
        jenkinsRule.createProject(WorkflowJob.class, "ProjectWithZeroBuilds");

        try (JenkinsRule.WebClient webClient = jenkinsRule.createWebClient()) {
            XmlPage xmlPage = webClient.goToXml("api/xml?depth=3&xpath=/hudson/job[name='ProjectWithZeroBuilds']/action/jobMetrics");

            Map<String, String> metrics = childrenAsMap(xmlPage.getDocumentElement());

            assertThat(metrics,
                    match("avgCheckoutDuration", isEqualTo(0),
                            "avgDuration", isEqualTo(0),
                            "avgSuccessDuration", isEqualTo(0),
                            "failureRate", isEqualTo(0.0),
                            "failureTimeRate", isEqualTo(0.0),
                            "maxCheckoutDuration", isEqualTo(0),
                            "maxDuration", isEqualTo(0),
                            "maxSuccessDuration", isEqualTo(0),
                            "minCheckoutDuration", isEqualTo(0),
                            "minDuration", isEqualTo(0),
                            "minSuccessDuration", isEqualTo(0),
                            "successRate", isEqualTo(0.0),
                            "successTimeRate", isEqualTo(0.0),
                            "standardDeviationDuration", isEqualTo(0),
                            "standardDeviationSuccessDuration", isEqualTo(0)
                    )
            );
        }
    }

    @Test
    public void one_run_should_have_appropriate_metrics() throws IOException, SAXException, ExecutionException, InterruptedException {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithOneSuccessBuild");
        project.setDefinition(successDefinition());
        project.scheduleBuild2(0).get();


        try (JenkinsRule.WebClient webClient = jenkinsRule.createWebClient()) {
            XmlPage xmlPage = webClient.goToXml("api/xml?depth=3&xpath=/hudson/job[name='ProjectWithOneSuccessBuild']/action/jobMetrics");

            Map<String, String> metrics = childrenAsMap(xmlPage.getDocumentElement());

            assertThat(metrics,
                    match("avgDuration", isGreaterThan(0),
                            "avgSuccessDuration", isGreaterThan(0),
                            "failureRate", isEqualTo(0.0),
                            "failureTimeRate", isEqualTo(0.0),
                            "maxDuration", isGreaterThan(0),
                            "maxSuccessDuration", isGreaterThan(0),
                            "minDuration", isGreaterThan(0),
                            "minSuccessDuration", isGreaterThan(0),
                            "successRate", isEqualTo(1.0),
                            "successTimeRate", isEqualTo(1.0),
                            "standardDeviationDuration", isEqualTo(0),
                            "standardDeviationSuccessDuration", isEqualTo(0)
                    )
            );
        }
    }

    @Test
    public void one_checkout_run_should_have_checkout_metrics() throws IOException, SAXException, ExecutionException, InterruptedException {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "ProjectWithOneCheckoutBuild");
        project.setDefinition(checkoutDefinition());
        project.scheduleBuild2(0).get();

        try (JenkinsRule.WebClient webClient = jenkinsRule.createWebClient()) {
            XmlPage xmlPage = webClient.goToXml("api/xml?depth=3&xpath=/hudson/job[name='ProjectWithOneCheckoutBuild']/action/jobMetrics");

            Map<String, String> metrics = childrenAsMap(xmlPage.getDocumentElement());

            assertThat(metrics,
                    match("avgCheckoutDuration", isGreaterThan(0),
                            "minCheckoutDuration", isGreaterThan(0),
                            "maxCheckoutDuration", isGreaterThan(0)
                    )
            );
        }
    }

    private Matcher<String> isGreaterThan(final Number value) {
        return new TypeSafeMatcher<String>() {
            @Override
            protected boolean matchesSafely(String item) {
                return Double.parseDouble(item) > value.doubleValue();
            }

            @Override
            public void describeTo(Description description) {
                description.appendValue(value);
            }
        };
    }

    private Matcher<String> isEqualTo(final Number value) {
        return new TypeSafeMatcher<String>() {
            @Override
            protected boolean matchesSafely(String item) {
                return item.equals(value.toString());
            }

            @Override
            public void describeTo(Description description) {
                description.appendValue(value);
            }
        };
    }

    private Matcher<Map<String, String>> match(final Object... data) {
        return new TypeSafeMatcher<Map<String, String>>() {
            @Override
            protected boolean matchesSafely(Map<String, String> item) {
                for (int i = 0; i < data.length; i += 2) {
                    String key = data[i].toString();
                    Matcher<String> matcher = (Matcher<String>) data[i + 1];

                    if (!item.containsKey(key) || !matcher.matches(item.get(key))) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
            }
        };
    }

    private Map<String, String> childrenAsMap(DomElement parent) {
        Map<String, String> elements = new HashMap<>();

        for (DomNode domNode : parent.getChildNodes()) {
            elements.put(domNode.getNodeName(), domNode.getTextContent());
        }

        return elements;
    }

}
