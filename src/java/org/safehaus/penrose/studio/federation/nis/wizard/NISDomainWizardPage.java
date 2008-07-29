package org.safehaus.penrose.studio.federation.nis.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Endi Sukma Dewata
 */
public class NISDomainWizardPage extends WizardPage implements ModifyListener {

    public Logger log = LoggerFactory.getLogger(getClass());

    public final static String NAME = "NIS Domain";

    Text domainText;
    Text serverText;

    String domain;
    String server;

    public NISDomainWizardPage() {
        super(NAME);

        setDescription("Enter NIS domain name and NIS server name.");
    }

    public void createControl(final Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        GridLayout sectionLayout = new GridLayout();
        sectionLayout.numColumns = 2;
        composite.setLayout(sectionLayout);

        Label domainLabel = new Label(composite, SWT.NONE);
        domainLabel.setText("NIS Domain:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        domainLabel.setLayoutData(gd);

        domainText = new Text(composite, SWT.BORDER);
        domainText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        domainText.addModifyListener(this);

        Label serverLabel = new Label(composite, SWT.NONE);
        serverLabel.setText("NIS Server:");
        serverLabel.setLayoutData(new GridData());

        serverText = new Text(composite, SWT.BORDER);
        serverText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        serverText.addModifyListener(this);

        refresh();
    }

    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) {
            refresh();
        }
    }

    public void refresh() {
        domainText.setText(domain == null ? "" : domain);
        serverText.setText(server == null ? "" : server);

        setPageComplete(validatePage());
    }

    public boolean validatePage() {
        if (getDomain() == null) return false;
        if (getServer() == null) return false;
        return true;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getDomain() {
        String s = domainText.getText();
        return s.equals("") ? null : s;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getServer() {
        String s = serverText.getText();
        return s.equals("") ? null : s;
    }

    public void modifyText(ModifyEvent event) {
        setPageComplete(validatePage());
    }
}
