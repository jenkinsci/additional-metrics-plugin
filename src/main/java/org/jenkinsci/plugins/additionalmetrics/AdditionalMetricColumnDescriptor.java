package org.jenkinsci.plugins.additionalmetrics;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.views.ListViewColumnDescriptor;

abstract class AdditionalMetricColumnDescriptor extends ListViewColumnDescriptor {

    private final String displayName;

    AdditionalMetricColumnDescriptor(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public boolean shownByDefault() {
        return false;
    }

    @NonNull
    @Override
    public String getDisplayName() {
        return displayName;
    }
}
