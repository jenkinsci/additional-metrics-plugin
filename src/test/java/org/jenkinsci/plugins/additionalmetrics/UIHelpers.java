/*
 * MIT License
 *
 * Copyright (c) 2018 Chadi El Masri
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
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.model.ListView;
import hudson.model.TopLevelItem;
import hudson.views.ListViewColumn;
import jenkins.model.Jenkins;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class UIHelpers {
    static String getListViewCellValue(HtmlPage page, ListView view, String jobName, String fieldName) {
        int i = 0;
        Map<String, Integer> textToIndex = new HashMap<>();
        for (ListViewColumn column : view.getColumns()) {
            textToIndex.put(column.getColumnCaption(), i++);
        }

        DomElement tr = page.getElementById("job_" + jobName);
        DomNode td = tr.getChildNodes().get(textToIndex.get(fieldName));

        return td.asText();
    }

    static ListView createAndAddListView(Jenkins instance, String listName, ListViewColumn column, TopLevelItem job) throws IOException {
        ListView listView = new ListView(listName, instance);
        listView.getColumns().add(column);
        listView.add(job);

        instance.addView(listView);

        return listView;
    }
}
