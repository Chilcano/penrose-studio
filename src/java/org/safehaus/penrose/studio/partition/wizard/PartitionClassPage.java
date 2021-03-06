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
package org.safehaus.penrose.studio.partition.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;

/**
 * @author Endi S. Dewata
 */
public class PartitionClassPage extends WizardPage implements ModifyListener {

    public final static String NAME = "Partition Class";

    Combo classNameCombo;

    public PartitionClassPage() {
        super(NAME);
        setDescription("Enter the class name of the partition (optional).");
    }

    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        GridLayout sectionLayout = new GridLayout();
        sectionLayout.numColumns = 2;
        composite.setLayout(sectionLayout);

        Label nameLabel = new Label(composite, SWT.NONE);
        nameLabel.setText("Class:");
        GridData gd = new GridData(GridData.FILL);
        gd.widthHint = 50;
        nameLabel.setLayoutData(gd);

        classNameCombo = new Combo(composite, SWT.BORDER);
        classNameCombo.add("");
        classNameCombo.add("org.safehaus.penrose.federation.partition.FederationPartition");

        classNameCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        classNameCombo.addModifyListener(this);

        setPageComplete(validatePage());
    }

    public String getPartitionClass() {
        return "".equals(classNameCombo.getText()) ? null : classNameCombo.getText();
    }

    public boolean validatePage() {
        return true;
    }

    public void modifyText(ModifyEvent event) {
        setPageComplete(validatePage());
    }
}