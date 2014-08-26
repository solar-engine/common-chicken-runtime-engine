/*
 * Copyright 2014 Gregor Peach, Colby Skeggs (bugfixes)
 * 
 * This file is part of the CCRE, the Common Chicken Runtime Engine.
 * 
 * The CCRE is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * The CCRE is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the CCRE.  If not, see <http://www.gnu.org/licenses/>.
 */
package intelligence;

import ccre.log.Logger;
import ccre.util.CArrayList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * A folder displayed on the intelligence panel.
 *
 * @author peachg
 */
public final class Folder extends Remote {

    /**
     * Loads the list of folders from the configuration file.
     *
     * @return the current list of folders.
     */
    public static Folder[] setupFolders() {
        ArrayList<Folder> folderList = new ArrayList<Folder>(10);
        try {
            File folder = new File(".").getAbsoluteFile();
            File target = null;
            while (folder != null && folder.exists()) {
                target = new File(folder, "poultry-settings.txt");
                if (target.exists() && target.canRead()) {
                    break;
                }
                target = null;
                folder = folder.getParentFile();
            }
            if (target == null) {
                throw new FileNotFoundException("Could not find folders.");
            }
            BufferedReader fin = new BufferedReader(new FileReader(target));
            try {
                while (true) {
                    String line = fin.readLine();
                    if (line == null) {
                        break;
                    }
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    String[] pts = line.split("=", 2);
                    if (pts.length == 1) {
                        throw new IOException("Bad line: no =.");
                    }
                    folderList.add(new Folder(pts[0].trim(), pts[1].trim()));
                }
            } finally {
                fin.close();
            }
        } catch (IOException ex) {
            Logger.warning("Could not set up folder list!", ex);
        }
        return folderList.toArray(new Folder[folderList.size()]);
    }

    boolean open = false, hascontents = false;
    final CArrayList<Remote> contents = new CArrayList<Remote>();
    int place;
    private final String id;
    private final String regex;

    private Folder(String id, String regex) {
        super("", 0);
        this.regex = regex;
        this.id = id;
    }

    @Override
    protected Object checkout() {
        Logger.warning("Can't checkout a folder!");
        return null;
    }

    /**
     * Check if the specified Remote is inside this Folder.
     *
     * @param r The remote to check.
     * @return if the remote is within this folder.
     */
    public boolean isInside(Remote r) {
        return r.path.matches(regex);
    }

    @Override
    public String toString() {
        return (!hascontents ? "x " : open ? "- " : "+ ") + id;
    }
}
