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
package org.safehaus.penrose.studio.mapping.editor;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.safehaus.penrose.studio.mapping.wizard.MappingPropertiesWizard;
import org.safehaus.penrose.studio.dialog.ErrorDialog;

/**
 * @author Endi S. Dewata
 */
public class MappingPropertiesPage extends MappingEditorPage {

    Label nameText;
    Label classText;
    Button enabledCheckbox;
    Text descriptionText;

    public MappingPropertiesPage(MappingEditor editor) {
        super(editor, "PROPERTIES", "Properties");
    }

    public void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);

        ScrolledForm form = managedForm.getForm();

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section propertiesSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        propertiesSection.setText("Properties");
        propertiesSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control propertiesControl = createPropertiesControl(propertiesSection);
        propertiesSection.setClient(propertiesControl);
    }

    public Composite createPropertiesControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite leftControl = createPropertiesLeftControl(composite);
        leftControl.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite rightControl = createPropertiesRightControl(composite);
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 100;
        rightControl.setLayoutData(gd);

        return composite;
    }

    public Composite createPropertiesLeftControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);

        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        Label nameLabel = toolkit.createLabel(composite, "Name:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        nameLabel.setLayoutData(gd);

        nameText = toolkit.createLabel(composite, "", SWT.READ_ONLY);
        nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        toolkit.createLabel(composite, "Class:");

        classText = toolkit.createLabel(composite, "", SWT.READ_ONLY);
        classText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        toolkit.createLabel(composite, "Enabled:");

        enabledCheckbox = toolkit.createButton(composite, "", SWT.CHECK);
        enabledCheckbox.setEnabled(false);

        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);

        Label descriptionLabel = toolkit.createLabel(composite, "Description:");
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        descriptionLabel.setLayoutData(gd);

        descriptionText = toolkit.createText(composite, "", SWT.READ_ONLY | SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = 100;
        gd.horizontalSpan = 2;
        descriptionText.setLayoutData(gd);

        return composite;
    }

    public Composite createPropertiesRightControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);

        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        Button editButton = new Button(composite, SWT.PUSH);
		editButton.setText("Edit");
        editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    MappingPropertiesWizard wizard = new MappingPropertiesWizard();
                    wizard.setMappingConfig(mappingConfig);

                    WizardDialog dialog = new WizardDialog(editor.getSite().getShell(), wizard);
                    dialog.setPageSize(600, 300);

                    int rc = dialog.open();
                    if (rc == Window.CANCEL) return;

                    editor.store();
                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e.getMessage());
                }
            }
        });

        return composite;
    }

    public void refresh() {
        String name = mappingConfig.getName();
        nameText.setText(name == null ? "" : name);

        String className = mappingConfig.getMappingClass();
        classText.setText(className == null ? "" : className);

        enabledCheckbox.setSelection(mappingConfig.isEnabled());

        String description = mappingConfig.getDescription();
        descriptionText.setText(description == null ? "" : description);
    }
}