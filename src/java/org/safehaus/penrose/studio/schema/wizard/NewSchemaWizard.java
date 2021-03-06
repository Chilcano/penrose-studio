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
package org.safehaus.penrose.studio.schema.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.schema.SchemaConfig;
import org.safehaus.penrose.schema.Schema;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.schema.SchemaManagerClient;
import org.apache.log4j.Logger;

import java.io.*;

/**
 * @author Endi S. Dewata
 */
public class NewSchemaWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Server server;

    public SchemaNameWizardPage namePage = new SchemaNameWizardPage();

    public NewSchemaWizard(Server server) {
        setWindowTitle("New Schema");

        this.server = server;
    }

    public void addPages() {
        addPage(namePage);
    }

    public boolean canFinish() {

        if (!namePage.isPageComplete()) return false;

        return true;
    }

    public void copy(String filename1, String filename2) throws Exception {
        File file1 = new File(filename1);
        File file2 = new File(filename2);
        file2.getParentFile().mkdirs();

        BufferedReader in = new BufferedReader(new FileReader(file1));
        PrintWriter out = new PrintWriter(new FileWriter(file2));

        String line;
        while ((line = in.readLine()) != null) {
            out.println(line);
        }

        out.close();
        in.close();
    }

    public boolean performFinish() {
        try {
            String schemaExtDir = "schema/ext";
            
            String name = namePage.getSchemaName();
            String path = schemaExtDir+"/"+name+".schema";

            SchemaConfig schemaConfig = new SchemaConfig();
            schemaConfig.setName(name);
            schemaConfig.setPath(path);

            Schema schema = new Schema(name);

            PenroseClient client = server.getClient();
            SchemaManagerClient schemaManagerClient = client.getSchemaManagerClient();
            schemaManagerClient.createSchema(schema);

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
            return false;
        }
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }
}
