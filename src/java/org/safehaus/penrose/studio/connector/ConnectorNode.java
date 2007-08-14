/**
 * Copyright (c) 2000-2006, Identyx Corporation.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.safehaus.penrose.studio.connector;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.swt.graphics.Image;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.connector.ConnectorConfig;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class ConnectorNode extends Node {

    Logger log = Logger.getLogger(getClass());

    ServersView view;
    ProjectNode projectNode;

    public ConnectorNode(ServersView view, String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);
        projectNode = (ProjectNode)parent;
        this.view = projectNode.getView();
    }

    public void showMenu(IMenuManager manager) {

        manager.add(new Action("Open") {
            public void run() {
                try {
                    open();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
/*
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new Action("Copy") {
            public void run() {
                try {
                    view.copy(connectorConfig);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        manager.add(new Action("Paste") {
            public void run() {
                try {
                    view.paste(connectorConfig);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        manager.add(new Action("Delete", PenrosePlugin.getImageDescriptor(PenroseImage.DELETE)) {
            public void run() {
                try {
                    remove();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
*/
    }

    public void open() throws Exception {

        Project project = projectNode.getProject();
        ConnectorConfig connectorConfig = project.getPenroseConfig().getConnectorConfig();
        ConnectorEditorInput ei = new ConnectorEditorInput(connectorConfig);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, ConnectorEditor.class.getName());
    }
}