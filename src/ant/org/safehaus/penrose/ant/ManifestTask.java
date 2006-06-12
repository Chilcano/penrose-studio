/**
 * Copyright (c) 2000-2005, Identyx Corporation.
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
package org.safehaus.penrose.ant;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class ManifestTask extends Task {

    private String file;
    private String dir;
    private String newDir;

    public void execute() throws BuildException {
        try {
            executeImpl();
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }

    public void executeImpl() throws Exception {

        PrintWriter out = new PrintWriter(new FileWriter(file, true));

        File libDir = new File(dir);
        File files[] = libDir.listFiles();

        Collection list = new ArrayList();
        for (int i=0; i<files.length; i++) {
            File f = files[i];

            if (!f.isFile()) continue;
            if (!f.getName().endsWith(".jar")) continue;

            list.add(newDir+"/"+f.getName());
        }

        boolean first = true;
        for (Iterator i=list.iterator(); i.hasNext();) {
            String line = (String)i.next();

            if (i.hasNext()) {
                line = line+",";
            }

            //log(line);
            if (first) {
                out.print("Bundle-ClassPath:");
                first = false;
            }

            out.println(" "+line);
        }

        out.close();
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getNewDir() {
        return newDir;
    }

    public void setNewDir(String newDir) {
        this.newDir = newDir;
    }
}
