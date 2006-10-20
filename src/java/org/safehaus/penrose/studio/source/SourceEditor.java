package org.safehaus.penrose.studio.source;

import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.IEditorInput;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.partition.SourceConfig;
import org.safehaus.penrose.partition.Partition;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public abstract class SourceEditor extends FormEditor {

    public Logger log = Logger.getLogger(getClass());

    private Server server;
    private Partition partition;

    private SourceConfig originalSourceConfig;
    private SourceConfig sourceConfig;

    private boolean dirty;

    public void setInput(IEditorInput input) {
        super.setInput(input);

        SourceEditorInput ei = (SourceEditorInput)input;
        server = ei.getProject();
        partition = ei.getPartition();
        originalSourceConfig = ei.getSourceConfig();
        sourceConfig = (SourceConfig)getOriginalSourceConfig().clone();
    }

    public Server getProject() {
        return server;
    }

    public void setProject(Server server) {
        this.server = server;
    }

    public SourceConfig getOriginalSourceConfig() {
        return originalSourceConfig;
    }

    public void setOriginalSourceConfig(SourceConfig originalSourceConfig) {
        this.originalSourceConfig = originalSourceConfig;
    }

    public Partition getPartition() {
        return partition;
    }

    public void setPartition(Partition partition) {
        this.partition = partition;
    }

    public SourceConfig getSourceConfig() {
        return sourceConfig;
    }

    public void setSourceConfig(SourceConfig sourceConfig) {
        this.sourceConfig = sourceConfig;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void checkDirty() {
        try {
            dirty = false;

            if (!originalSourceConfig.equals(sourceConfig)) {
                dirty = true;
                return;
            }

        } catch (Exception e) {
            log.debug(e.getMessage(), e);

        } finally {
            firePropertyChange(PROP_DIRTY);
        }
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
}