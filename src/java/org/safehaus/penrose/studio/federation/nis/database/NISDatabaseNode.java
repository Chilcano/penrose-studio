package org.safehaus.penrose.studio.federation.nis.database;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.federation.nis.NISNode;
import org.safehaus.penrose.studio.federation.nis.NISFederation;
import org.safehaus.penrose.studio.federation.nis.domain.NISDomainNode;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.server.ServersView;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;

/**
 * @author Endi S. Dewata
 */
public class NISDatabaseNode extends Node {

    ProjectNode projectNode;
    NISNode nisNode;
    NISDomainNode domainNode;

    private NISFederation nisFederation;

    public NISDatabaseNode(String name, NISDomainNode domainNode) {
        super(
                name,
                ServersView.ENTRY,
                PenroseStudioPlugin.getImage(PenroseImage.FOLDER),
                null,
                domainNode
        );

        this.domainNode = domainNode;

        nisNode = domainNode.getNisNode();
        projectNode = nisNode.getProjectNode();

        nisFederation = nisNode.getNisFederation();
    }

    public void showMenu(IMenuManager manager) throws Exception {

        manager.add(new Action("Open") {
            public void run() {
                try {
                    open();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
    }

    public void open() throws Exception {

        NISDatabaseEditorInput ei = new NISDatabaseEditorInput();
        ei.setProject(projectNode.getProject());
        ei.setNisTool(nisFederation);
        ei.setDomain(domainNode.getDomain());

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, NISDatabaseEditor.class.getName());
    }


    public NISFederation getNisTool() {
        return nisFederation;
    }

    public void setNisTool(NISFederation nisFederation) {
        this.nisFederation = nisFederation;
    }
}