package org.safehaus.penrose.studio.nis;

import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.PenroseApplication;
import org.safehaus.penrose.studio.nis.action.NISAction;
import org.safehaus.penrose.studio.nis.action.NISActionRequest;
import org.safehaus.penrose.studio.nis.action.NISActionResponse;
import org.safehaus.penrose.naming.PenroseContext;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.source.SourceManager;
import org.safehaus.penrose.source.Source;
import org.safehaus.penrose.ldap.*;

/**
 * @author Endi S. Dewata
 */
public class NISGroupsPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Combo actionCombo;
    List domainsList;

    Label message;
    Table table;

    NISEditor editor;

    public NISGroupsPage(NISEditor editor) {
        super(editor, "GROUPS", "  Groups  ");

        this.editor = editor;
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("NIS Groups");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Action");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control sourcesSection = createActionSection(section);
        section.setClient(sourcesSection);

        section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Results");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control resultsSection = createResultsSection(section);
        section.setClient(resultsSection);

        init();
    }

    public void init() {
       try {
           actionCombo.removeAll();
           domainsList.removeAll();

           PenroseApplication penroseApplication = PenroseApplication.getInstance();
           PenroseContext penroseContext = penroseApplication.getPenroseContext();
           SourceManager sourceManager = penroseContext.getSourceManager();

           Source actionsSource = sourceManager.getSource("DEFAULT", "actions");

           SearchRequest request = new SearchRequest();
           request.setFilter("(type=groups)");

           SearchResponse<SearchResult> response = new SearchResponse<SearchResult>() {
               public void add(SearchResult result) throws Exception {
                   Attributes attributes = result.getAttributes();
                   String actionName = (String)attributes.getValue("name");
                   String className = (String)attributes.getValue("className");

                   actionCombo.add(actionName);
                   actionCombo.setData(actionName, className);
               }
           };

           actionsSource.search(request, response);

           actionCombo.select(0);

           Source domainsSource = sourceManager.getSource("DEFAULT", "domains");

           request = new SearchRequest();
           response = new SearchResponse<SearchResult>() {
               public void add(SearchResult result) throws Exception {
                   Attributes attributes = result.getAttributes();
                   String domainName = (String)attributes.getValue("name");
                   String partitionName = (String)attributes.getValue("partition");
                   domainsList.add(domainName);
                   domainsList.setData(domainName, partitionName);
               }
           };

           domainsSource.search(request, response);

       } catch (Exception e) {
           log.debug(e.getMessage(), e);
           String message = e.toString();
           if (message.length() > 500) {
               message = message.substring(0, 500) + "...";
           }
           MessageDialog.openError(editor.getSite().getShell(), "Init Failed", message);
       }
   }

    public Composite createActionSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(3, false));

        Label actionLabel = toolkit.createLabel(composite, "Action:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        actionLabel.setLayoutData(gd);

        actionCombo = new Combo(composite, SWT.READ_ONLY);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        actionCombo.setLayoutData(gd);

        Label domainLabel = toolkit.createLabel(composite, "Domain:");
        gd = new GridData();
        gd.verticalAlignment = GridData.BEGINNING;
        gd.widthHint = 100;
        domainLabel.setLayoutData(gd);

        domainsList = new List(composite, SWT.BORDER | SWT.MULTI);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        gd.heightHint = 100;
        domainsList.setLayoutData(gd);

        new Label(composite, SWT.NONE);

        Composite links = toolkit.createComposite(composite);
        links.setLayout(new RowLayout());
        links.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Hyperlink selectAllLink = toolkit.createHyperlink(links, "Select All", SWT.NONE);

        selectAllLink.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent event) {
                domainsList.selectAll();
            }
        });

        Hyperlink selectNoneLink = toolkit.createHyperlink(links, "Select None", SWT.NONE);

        selectNoneLink.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent event) {
                domainsList.deselectAll();
            }
        });

        Button runButton = new Button(composite, SWT.PUSH);
        runButton.setText("Run");
        gd = new GridData();
        gd.horizontalAlignment = GridData.END;
        gd.widthHint = 80;
        runButton.setLayoutData(gd);

        runButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    run();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                    String message = e.toString();
                    if (message.length() > 500) {
                        message = message.substring(0, 500) + "...";
                    }
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", message);
                }
            }
        });

        return composite;
    }

    public Composite createResultsSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite.setLayout(new GridLayout());

        message = toolkit.createLabel(composite, "");
        message.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        table = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));

        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableColumn tc = new TableColumn(table, SWT.NONE);
        tc.setText("Domain 1");
        tc.setWidth(100);

        tc = new TableColumn(table, SWT.NONE);
        tc.setText("Group");
        tc.setWidth(100);

        tc = new TableColumn(table, SWT.NONE);
        tc.setText("GID");
        tc.setWidth(100);

        tc = new TableColumn(table, SWT.NONE);
        tc.setText("Domain 2");
        tc.setWidth(100);

        tc = new TableColumn(table, SWT.NONE);
        tc.setText("Group");
        tc.setWidth(100);

        tc = new TableColumn(table, SWT.NONE);
        tc.setText("GID");
        tc.setWidth(100);

        Composite buttons = toolkit.createComposite(composite);
        buttons.setLayout(new RowLayout());
        buttons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Button edit1Button = new Button(buttons, SWT.PUSH);
        edit1Button.setText("Edit group from domain 1");

        edit1Button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (table.getSelectionCount() == 0) return;

                    TableItem item = table.getSelection()[0];
                    String domainName = (String)item.getData("domain1");
                    String partitionName = (String)item.getData("partition1");
                    Source source = (Source)item.getData("source1");
                    String cn = (String)item.getData("group1");
                    Object gidNumber = item.getData("gidNumber1");
                    edit(partitionName, source, cn);

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                    String message = e.toString();
                    if (message.length() > 500) {
                        message = message.substring(0, 500) + "...";
                    }
                    MessageDialog.openError(editor.getSite().getShell(), "Edit Failed", message);
                }
            }
        });

        Button edit2Button = new Button(buttons, SWT.PUSH);
        edit2Button.setText("Edit group from domain 2");

        edit2Button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (table.getSelectionCount() == 0) return;

                    TableItem item = table.getSelection()[0];
                    String domainName = (String)item.getData("domain2");
                    String partitionName = (String)item.getData("partition2");
                    Source source = (Source)item.getData("source2");
                    String cn = (String)item.getData("group2");
                    Object gidNumber = item.getData("gidNumber2");
                    edit(partitionName, source, cn);

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                    String message = e.toString();
                    if (message.length() > 500) {
                        message = message.substring(0, 500) + "...";
                    }
                    MessageDialog.openError(editor.getSite().getShell(), "Edit Failed", message);
                }
            }
        });

        return composite;
    }

    public void run() throws Exception {

        table.removeAll();

        String actionName = actionCombo.getText();
        String className = (String)actionCombo.getData(actionName);

        Class clazz = Class.forName(className);
        NISAction action = (NISAction)clazz.newInstance();

        NISActionRequest request = new NISActionRequest();

        String[] domains = domainsList.getSelection();
        for (int i=0; i<domains.length; i++) {
            request.addPartition(domains[i]);
        }

        NISActionResponse response = new NISActionResponse() {

            int counter = 0;

            public void add(Object object) {
                Attributes attributes = (Attributes)object;

                log.debug("Displaying result #"+counter);

                String domain1Name = (String)attributes.getValue("domain1");
                String partition1Name = (String)attributes.getValue("partition1");
                Source source1 = (Source)attributes.getValue("source1");
                Object group1 = attributes.getValue("group1");
                Object gidNumber1 = attributes.getValue("gidNumber1");

                String domain2Name = (String)attributes.getValue("domain2");
                String partition2Name = (String)attributes.getValue("partition2");
                Source source2 = (Source)attributes.getValue("source2");
                Object group2 = attributes.getValue("group2");
                Object gidNumber2 = attributes.getValue("gidNumber2");

                TableItem ti = new TableItem(table, SWT.NONE);
                ti.setText(0, domain1Name);
                ti.setText(1, ""+ group1);
                ti.setText(2, ""+ gidNumber1);
                ti.setText(3, domain2Name);
                ti.setText(4, ""+ group2);
                ti.setText(5, ""+ gidNumber2);

                ti.setData("domain1", domain1Name);
                ti.setData("partition1", partition1Name);
                ti.setData("source1", source1);
                ti.setData("group1", group1);
                ti.setData("gidNumber1", gidNumber1);

                ti.setData("domain2", domain2Name);
                ti.setData("partition2", partition2Name);
                ti.setData("source2", source2);
                ti.setData("group2", group2);
                ti.setData("gidNumber2", gidNumber2);

                counter++;
            }

            public void close() {
                message.setText("Found "+counter+" result(s).");
            }
        };

        action.execute(request, response);
    }

    public void edit(String partitionName, Source source, String cn) throws Exception {

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        PenroseContext penroseContext = penroseApplication.getPenroseContext();
        SourceManager sourceManager = penroseContext.getSourceManager();

        RDNBuilder rb = new RDNBuilder();
        rb.set("cn", cn);
        RDN rdn = rb.toRdn();
        DN dn = new DN(rdn);

        NISUserDialog dialog = new NISUserDialog(getSite().getShell(), SWT.NONE);

        Source sourceUidNumber = sourceManager.getSource(partitionName, "groups_gidNumber");
        dialog.setSourceConfig(sourceUidNumber.getSourceConfig());

        SearchRequest request = new SearchRequest();
        request.setFilter("(cn="+cn+")");
        SearchResponse<SearchResult> response = new SearchResponse<SearchResult>();

        sourceUidNumber.search(request, response);
        while (response.hasNext()) {
            SearchResult result = (SearchResult)response.next();
            Attributes attributes = result.getAttributes();
            Object uidNumber = attributes.getValue("gidNumber");
            dialog.addUidNumber(uidNumber);
        }

        dialog.open();

        if (dialog.getAction() == NISUserDialog.CANCEL) return;

        Attributes attributes = dialog.getAttributes();
        sourceUidNumber.add(dn, attributes);
    }
}
