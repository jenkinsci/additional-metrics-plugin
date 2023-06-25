package org.jenkinsci.plugins.additionalmetrics;

import hudson.model.ListView;
import hudson.model.TopLevelItem;
import hudson.views.ListViewColumn;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import jenkins.model.Jenkins;
import org.htmlunit.html.DomElement;
import org.htmlunit.html.DomNode;
import org.htmlunit.html.HtmlPage;

class UIHelpers {

    private UIHelpers() {
        // utility class
    }

    static DomNode getListViewCell(HtmlPage page, ListView view, String jobName, String fieldName) {
        int i = 0;
        Map<String, Integer> textToIndex = new HashMap<>();
        for (ListViewColumn column : view.getColumns()) {
            textToIndex.put(column.getColumnCaption(), i++);
        }

        DomElement tr = page.getElementById("job_" + jobName);
        DomNode td = tr.getChildNodes().get(textToIndex.get(fieldName));

        return td;
    }

    static ListView createAndAddListView(Jenkins instance, String listName, ListViewColumn column, TopLevelItem job)
            throws IOException {
        ListView listView = new ListView(listName, instance);
        listView.getColumns().add(column);
        listView.add(job);

        instance.addView(listView);

        return listView;
    }

    static String dataOf(DomNode columnNode) {
        return columnNode.getAttributes().getNamedItem("data").getNodeValue();
    }
}
