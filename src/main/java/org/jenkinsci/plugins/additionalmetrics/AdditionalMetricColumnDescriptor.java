package org.jenkinsci.plugins.additionalmetrics;

import hudson.views.ListViewColumnDescriptor;
import javax.annotation.Nonnull;

abstract class AdditionalMetricColumnDescriptor extends ListViewColumnDescriptor {

    private final String displayName;

    AdditionalMetricColumnDescriptor(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public boolean shownByDefault() {
        return false;
    }

    @Nonnull
    @Override
    public String getDisplayName() {
        return displayName;
    }
}
