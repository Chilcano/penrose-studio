/**
 * Copyright 2009 Red Hat, Inc.
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
package org.safehaus.penrose.studio.service.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.server.tree.ServerNode;
import org.safehaus.penrose.studio.service.wizard.ServiceWizard;
import org.safehaus.penrose.studio.service.tree.ServicesNode;
import org.safehaus.penrose.service.ServiceConfig;
import org.safehaus.penrose.service.ServiceManagerClient;
import org.safehaus.penrose.client.PenroseClient;
import org.apache.log4j.Logger;

public class NewServiceAction extends Action {

    Logger log = Logger.getLogger(getClass());

	public NewServiceAction() {
        setText("New Service...");
        setId(getClass().getName());
	}
	
	public void run() {
        try {
            ServersView serversView = ServersView.getInstance();
            ServerNode serverNode = serversView.getSelectedServerNode();

            if (serverNode == null || !serverNode.isConnected()) {
                ErrorDialog.open("Not connected to server.");
                return;
            }

            Server server = serverNode.getServer();
            ServicesNode servicesNode = serverNode.getServicesNode();

            ServiceConfig serviceConfig = new ServiceConfig();

            ServiceWizard wizard = new ServiceWizard();
            wizard.setServer(server);
            wizard.setServiceConfig(serviceConfig);
            
            WizardDialog dialog = new WizardDialog(serversView.getSite().getShell(), wizard);
            dialog.setPageSize(600, 300);
            int rc = dialog.open();

            if (rc == Window.CANCEL) return;

            serversView.open(servicesNode);
            serversView.refresh(servicesNode);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
	}
	
}