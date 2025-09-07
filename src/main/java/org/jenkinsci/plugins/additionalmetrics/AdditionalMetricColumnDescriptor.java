package org.jenkinsci.plugins.additionalmetrics;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.views.ListViewColumnDescriptor;

/**
 * Abstract base class for additional metric column descriptors.
 * Provides common functionality for metric columns including display name handling
 * and default visibility configuration.
 */
abstract class AdditionalMetricColumnDescriptor extends ListViewColumnDescriptor {

    private final String displayName;

    /**
     * Creates a new additional metric column descriptor with the specified display name.
     *
     * @param displayName the human-readable name for this column type
     */
    AdditionalMetricColumnDescriptor(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Indicates whether this column should be shown by default in list views.
     * Additional metric columns are not shown by default and must be explicitly added by users.
     *
     * @return false, indicating this column is not shown by default
     */
    @Override
    public boolean shownByDefault() {
        return false;
    }

    /**
     * Returns the display name for this column type.
     * This name appears in the Jenkins UI when users are configuring list view columns.
     *
     * @return the human-readable display name for this column type
     */
    @NonNull
    @Override
    public String getDisplayName() {
        return displayName;
    }
}
