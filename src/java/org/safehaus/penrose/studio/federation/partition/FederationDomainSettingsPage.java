package org.safehaus.penrose.studio.federation.partition;

import org.apache.log4j.Logger;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.safehaus.penrose.federation.FederationClient;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.federation.partition.FederationDomainEditorWizard;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.connection.ConnectionClient;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.connection.ConnectionManagerClient;
import org.safehaus.penrose.source.SourceClient;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.source.SourceManagerClient;

import javax.naming.Context;

/**
 * @author Endi S. Dewata
 */
public class FederationDomainSettingsPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    FederationDomainEditor editor;

    Label urlText;
    Label suffixText;
    Label bindDnText;
    Label bindPasswordText;

    FederationClient federationClient;
    Server server;

    ConnectionClient connectionClient;
    SourceClient sourceClient;

    public FederationDomainSettingsPage(FederationDomainEditor editor) throws Exception {
        super(editor, "SETTINGS", "  Settings  ");

        this.editor = editor;
        this.federationClient = editor.federationClient;
        this.server = editor.server;

        PartitionClient partitionClient = federationClient.getPartitionClient();
        ConnectionManagerClient connectionManagerClient = partitionClient.getConnectionManagerClient();
        SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();

        connectionClient = connectionManagerClient.getConnectionClient("LDAP");
        sourceClient = sourceManagerClient.getSourceClient("Global");
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText(federationClient.getFederationDomain());

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section settingsSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        settingsSection.setText("Settings");
        settingsSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control settingsControl = createSettingsControl(settingsSection);
        settingsSection.setClient(settingsControl);

        refresh();
    }

    public Composite createSettingsControl(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Composite leftPanel = createSettingsLeftPanel(composite);
        leftPanel.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite rightPanel = createSettingsRightPanel(composite);
        rightPanel.setLayoutData(new GridData(GridData.FILL_VERTICAL));

        return composite;
    }

    public Composite createSettingsLeftPanel(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Label urlLabel = toolkit.createLabel(composite, "Servers:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        urlLabel.setLayoutData(gd);

        urlText = toolkit.createLabel(composite, "");
        urlText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label suffixLabel = toolkit.createLabel(composite, "Suffix:");
        suffixLabel.setLayoutData(new GridData());

        suffixText = toolkit.createLabel(composite, "");
        suffixText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label bindDnLabel = toolkit.createLabel(composite, "Bind DN:");
        bindDnLabel.setLayoutData(new GridData());

        bindDnText = toolkit.createLabel(composite, "");
        bindDnText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label bindPasswordLabel = toolkit.createLabel(composite, "Password:");
        bindPasswordLabel.setLayoutData(new GridData());

        bindPasswordText = toolkit.createLabel(composite, "");
        bindPasswordText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return composite;
    }

    public Composite createSettingsRightPanel(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout());

        Button editButton = new Button(composite, SWT.PUSH);
		editButton.setText("Edit");

        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.widthHint = 100;
        editButton.setLayoutData(gd);

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    FederationDomainEditorWizard wizard = new FederationDomainEditorWizard(federationClient);
                    wizard.setServer(server);

                    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
                    dialog.setPageSize(600, 300);
                    int rc = dialog.open();

                    if (rc == Window.CANCEL) return;

                    refresh();
                    
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        return composite;
    }

    public void refresh() {
        try {
            ConnectionConfig connectionConfig = connectionClient.getConnectionConfig();
            SourceConfig sourceConfig = sourceClient.getSourceConfig();

            String url = connectionConfig.getParameter(Context.PROVIDER_URL);
            String suffix = sourceConfig.getParameter("baseDn");
            String bindDn = connectionConfig.getParameter(Context.SECURITY_PRINCIPAL);
            String bindPassword = connectionConfig.getParameter(Context.SECURITY_CREDENTIALS);

            urlText.setText(url == null ? "" : url);
            suffixText.setText(suffix == null ? "" : suffix);
            bindDnText.setText(bindDn == null ? "" : bindDn);
            bindPasswordText.setText(bindPassword == null ? "" : "*****");

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

}
