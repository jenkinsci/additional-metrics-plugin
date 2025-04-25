package org.jenkinsci.plugins.additionalmetrics;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.jenkinsci.plugins.additionalmetrics.JobRunner.WorkflowBuilder.StepDefinitions.CHECKOUT;
import static org.jenkinsci.plugins.additionalmetrics.JobRunner.WorkflowBuilder.StepDefinitions.SUCCESS;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.htmlunit.html.DomElement;
import org.htmlunit.html.DomNode;
import org.htmlunit.xml.XmlPage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.xml.sax.SAXException;

@WithJenkins
class MetricsActionFactoryTest {

    private static JenkinsRule jenkinsRule;

    @BeforeAll
    static void setUp(JenkinsRule rule) {
        jenkinsRule = rule;
    }

    @Test
    void no_runs_metrics_should_be_zeros() throws IOException, SAXException {
        var runner = JobRunner.createWorkflowJob(jenkinsRule);

        try (JenkinsRule.WebClient webClient = jenkinsRule.createWebClient()) {
            XmlPage xmlPage = webClient.goToXml(
                    "api/xml?depth=3&xpath=/hudson/job[name='" + runner.getJob().getName() + "']/action/jobMetrics");

            Map<String, String> metrics = childrenAsMap(xmlPage.getDocumentElement());

            assertThat(
                    metrics,
                    match(
                            "avgCheckoutDuration",
                            isEqualTo(0),
                            "avgDuration",
                            isEqualTo(0),
                            "avgSuccessDuration",
                            isEqualTo(0),
                            "failureRate",
                            isEqualTo(0.0),
                            "failureTimeRate",
                            isEqualTo(0.0),
                            "maxCheckoutDuration",
                            isEqualTo(0),
                            "maxDuration",
                            isEqualTo(0),
                            "maxSuccessDuration",
                            isEqualTo(0),
                            "minCheckoutDuration",
                            isEqualTo(0),
                            "minDuration",
                            isEqualTo(0),
                            "minSuccessDuration",
                            isEqualTo(0),
                            "successRate",
                            isEqualTo(0.0),
                            "successTimeRate",
                            isEqualTo(0.0),
                            "standardDeviationDuration",
                            isEqualTo(0),
                            "standardDeviationSuccessDuration",
                            isEqualTo(0),
                            "unstableRate",
                            isEqualTo(0.0)));
        }
    }

    @Test
    void one_run_should_have_appropriate_metrics() throws Exception {
        var runner = JobRunner.createWorkflowJob(jenkinsRule)
                .configurePipelineDefinition(SUCCESS)
                .schedule();

        try (JenkinsRule.WebClient webClient = jenkinsRule.createWebClient()) {
            XmlPage xmlPage = webClient.goToXml(
                    "api/xml?depth=3&xpath=/hudson/job[name='" + runner.getJob().getName() + "']/action/jobMetrics");

            Map<String, String> metrics = childrenAsMap(xmlPage.getDocumentElement());

            assertThat(
                    metrics,
                    match(
                            "avgDuration",
                            isGreaterThan(0),
                            "avgSuccessDuration",
                            isGreaterThan(0),
                            "failureRate",
                            isEqualTo(0.0),
                            "failureTimeRate",
                            isEqualTo(0.0),
                            "maxDuration",
                            isGreaterThan(0),
                            "maxSuccessDuration",
                            isGreaterThan(0),
                            "minDuration",
                            isGreaterThan(0),
                            "minSuccessDuration",
                            isGreaterThan(0),
                            "successRate",
                            isEqualTo(1.0),
                            "successTimeRate",
                            isEqualTo(1.0),
                            "standardDeviationDuration",
                            isEqualTo(0),
                            "standardDeviationSuccessDuration",
                            isEqualTo(0),
                            "unstableRate",
                            isEqualTo(0.0)));
        }
    }

    @Test
    void one_checkout_run_should_have_checkout_metrics() throws Exception {
        var runner = JobRunner.createWorkflowJob(jenkinsRule)
                .configurePipelineDefinition(CHECKOUT)
                .schedule();

        try (JenkinsRule.WebClient webClient = jenkinsRule.createWebClient()) {
            XmlPage xmlPage = webClient.goToXml(
                    "api/xml?depth=3&xpath=/hudson/job[name='" + runner.getJob().getName() + "']/action/jobMetrics");

            Map<String, String> metrics = childrenAsMap(xmlPage.getDocumentElement());

            assertThat(
                    metrics,
                    match(
                            "avgCheckoutDuration",
                            isGreaterThan(0),
                            "minCheckoutDuration",
                            isGreaterThan(0),
                            "maxCheckoutDuration",
                            isGreaterThan(0)));
        }
    }

    private Matcher<String> isGreaterThan(final Number value) {
        return new TypeSafeMatcher<>() {
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
        return new TypeSafeMatcher<>() {
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
        return new TypeSafeMatcher<>() {
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
            public void describeTo(Description description) {}
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
