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
package org.safehaus.penrose.studio.jdbc.connection;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.safehaus.penrose.source.TableConfig;
import org.safehaus.penrose.source.FieldConfig;
import org.safehaus.penrose.jdbc.JDBCClient;
import org.safehaus.penrose.jdbc.adapter.JDBCAdapter;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.connection.editor.ConnectionEditorPage;
import org.safehaus.penrose.connection.Connection;
import org.safehaus.penrose.partition.Partitions;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.partition.PartitionContext;
import org.safehaus.penrose.config.PenroseConfig;
import org.safehaus.penrose.naming.PenroseContext;

import java.util.Collection;
import java.io.StringWriter;
import java.io.PrintWriter;

/**
 * @author Endi S. Dewata
 */
public class JDBCConnectionTablesPage extends ConnectionEditorPage {

    Combo catalogCombo;
    Combo schemaCombo;
    Table tablesTable;
    Table fieldsTable;

    public JDBCConnectionTablesPage(JDBCConnectionEditor editor) {
        super(editor, "TABLES", "  Tables  ");
    }

    public void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);

        ScrolledForm form = managedForm.getForm();
        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Actions");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control actionsSection = createActionsSection(section);
        section.setClient(actionsSection);

        section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Catalogs and Schema");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control catalogsSection = createCatalogsSection(section);
        section.setClient(catalogsSection);

        section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Tables and Fields");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control tablesSection = createTablesSection(section);
        section.setClient(tablesSection);
    }

    public Composite createActionsSection(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new RowLayout());

        Hyperlink refresh = toolkit.createHyperlink(composite, "Refresh", SWT.NONE);

        refresh.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent event) {
                refresh();
                showFieldNames();
            }
        });

        return composite;
    }

    public Composite createCatalogsSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Label catalogLabel = toolkit.createLabel(composite, "Catalog:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        catalogLabel.setLayoutData(gd);

        catalogCombo = new Combo(composite, SWT.NONE);
        catalogCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        catalogCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                showTableNames();
                showFieldNames();
            }
        });

        Label schemaLabel = toolkit.createLabel(composite, "Schema:");
        gd = new GridData();
        gd.widthHint = 100;
        schemaLabel.setLayoutData(gd);

        schemaCombo = new Combo(composite, SWT.NONE);
        schemaCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        schemaCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                showTableNames();
                showFieldNames();
            }
        });

        String driver   = connectionConfig.getParameter(JDBCClient.DRIVER);
        String username = connectionConfig.getParameter(JDBCClient.USER);

        if ("oracle.jdbc.driver.OracleDriver".equals(driver)) {
            log.debug("Setting Oracle's default schema to "+username.toUpperCase());
            schemaCombo.setText(username.toUpperCase());
        }

        return composite;
    }

    public Composite createTablesSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        tablesTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 200;
        tablesTable.setLayoutData(gd);

        tablesTable.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                showFieldNames();
            }
        });

        Menu menu = new Menu(tablesTable);
        tablesTable.setMenu(menu);

        MenuItem mi = new MenuItem(menu, SWT.PUSH);
        mi.setText("Create source...");

        mi.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    TableItem ti = tablesTable.getSelection()[0];
                    TableConfig tableConfig = (TableConfig)ti.getData();

                    JDBCSourceWizard wizard = new JDBCSourceWizard(partitionConfig, connectionConfig, tableConfig);
                    wizard.setProject(editor.getProject());

                    WizardDialog dialog = new WizardDialog(getEditor().getSite().getShell(), wizard);
                    dialog.setPageSize(600, 300);
                    dialog.open();

                    PenroseStudio penroseStudio = PenroseStudio.getInstance();
                    penroseStudio.notifyChangeListeners();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        fieldsTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
        fieldsTable.setHeaderVisible(true);
        fieldsTable.setLinesVisible(true);
        fieldsTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        TableColumn tc = new TableColumn(fieldsTable, SWT.NONE);
        tc.setText("Field");
        tc.setWidth(200);

        tc = new TableColumn(fieldsTable, SWT.NONE);
        tc.setText("Type");
        tc.setWidth(100);

        return composite;
    }

    public void refresh() {
        try {
            String catalog = catalogCombo.getText();
            String schema = schemaCombo.getText();

            catalogCombo.removeAll();
            schemaCombo.removeAll();
            tablesTable.removeAll();

            PenroseConfig penroseConfig = project.getPenroseConfig();
            PenroseContext penroseContext = project.getPenroseContext();

            Partitions partitions = new Partitions();

            PartitionContext partitionContext = new PartitionContext();
            partitionContext.setPenroseConfig(penroseConfig);
            partitionContext.setPenroseContext(penroseContext);

            Partition partition = new Partition();
            partition.init(partitionConfig, partitionContext);
            partitions.addPartition(partition);

            Connection connection = partition.createConnection(connectionConfig);

            JDBCAdapter jdbcAdapter = (JDBCAdapter)connection.getAdapter();
            JDBCClient client = jdbcAdapter.getClient();

            //JDBCClient client = new JDBCClient(connectionConfig.getParameters());
            //client.connect();

            Collection<String> catalogs = client.getCatalogs();
            Collection<String> schemas = client.getSchemas();
            Collection<TableConfig> tables = client.getTables(getCatalog(), getSchema());

            //client.close();
            connection.stop();

            catalogCombo.add("");
            for (String catalogName : catalogs) {
                catalogCombo.add(catalogName);
            }

            if (catalogs.contains(catalog)) {
                catalogCombo.setText(catalog);
            } else {
                catalogCombo.select(0);
            }

            schemaCombo.add("");
            for (String schemaName : schemas) {
                schemaCombo.add(schemaName);
            }

            if (schemas.contains(schema)) {
                schemaCombo.setText(schema);
            } else {
                schemaCombo.select(0);
            }

            for (TableConfig tableConfig : tables) {
                TableItem item = new TableItem(tablesTable, SWT.NONE);
                item.setText(tableConfig.getName());
                item.setData(tableConfig);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String message = sw.toString();
            if (message.length() > 500) {
                message = message.substring(0, 500) + "...";
            }
            MessageDialog.openError(getEditorSite().getShell(), "Error", "Error: "+message);
        }
    }

    public String getCatalog() {
        return "".equals(catalogCombo.getText()) ? null : catalogCombo.getText();
    }

    public String getSchema() {
        return "".equals(schemaCombo.getText()) ? null : schemaCombo.getText();
    }

    public void showTableNames() {
        try {
            log.debug("Updating table names");

            tablesTable.removeAll();

            PenroseConfig penroseConfig = project.getPenroseConfig();
            PenroseContext penroseContext = project.getPenroseContext();

            Partitions partitions = new Partitions();

            PartitionContext partitionContext = new PartitionContext();
            partitionContext.setPenroseConfig(penroseConfig);
            partitionContext.setPenroseContext(penroseContext);

            Partition partition = new Partition();
            partition.init(partitionConfig, partitionContext);
            partitions.addPartition(partition);

            Connection connection = partition.createConnection(connectionConfig);

            JDBCAdapter jdbcAdapter = (JDBCAdapter)connection.getAdapter();
            JDBCClient client = jdbcAdapter.getClient();

            //JDBCClient client = new JDBCClient(connectionConfig.getParameters());
            //client.connect();

            Collection<TableConfig> tables = client.getTables(getCatalog(), getSchema());

            //client.close();
            connection.stop();

            for (TableConfig tableConfig : tables) {
                TableItem item = new TableItem(tablesTable, SWT.NONE);
                item.setText(tableConfig.getName());
                item.setData(tableConfig);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String message = sw.toString();
            if (message.length() > 500) {
                message = message.substring(0, 500) + "...";
            }
            MessageDialog.openError(getEditorSite().getShell(), "Error", "Error: "+message);
        }
    }

    public void showFieldNames() {
        try {
            log.debug("Updating field names");

            fieldsTable.removeAll();

            if (tablesTable.getSelectionCount() == 0) return;

            TableItem ti = tablesTable.getSelection()[0];
            TableConfig tableConfig = (TableConfig)ti.getData();

            JDBCClient client = new JDBCClient(connectionConfig.getParameters());

            Collection<FieldConfig> fields = client.getColumns(getCatalog(), getSchema(), tableConfig.getName());

            client.close();

            for (FieldConfig field : fields) {
                TableItem it = new TableItem(fieldsTable, SWT.NONE);
                it.setImage(PenrosePlugin.getImage(field.isPrimaryKey() ? PenroseImage.KEY : PenroseImage.NOKEY));
                it.setText(0, field.getName());
                it.setText(1, field.getType());
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String message = sw.toString();
            if (message.length() > 500) {
                message = message.substring(0, 500) + "...";
            }
            MessageDialog.openError(getEditorSite().getShell(), "Error", "Error: "+message);
        }
    }
}