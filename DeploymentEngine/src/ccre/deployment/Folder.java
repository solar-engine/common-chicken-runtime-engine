/*
 * Copyright 2015 Cel Skeggs.
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
package ccre.deployment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A collection of class files and resources inside a folder on the filesystem,
 * often a temporary folder.
 *
 * @author skeggsc
 */
public class Folder extends Artifact {

    private final File folder;

    /**
     * Creates a new Folder that wraps the folder specified by
     * <code>folder</code>.
     *
     * @param folder the folder on the filesystem to wrap.
     * @throws IllegalArgumentException if <code>folder</code> does not exist or
     * is not a directory.
     */
    public Folder(File folder) throws IllegalArgumentException {
        if (!folder.exists() || !folder.isDirectory()) {
            throw new IllegalArgumentException("Not a valid folder: " + folder);
        }
        this.folder = folder;
    }

    @Override
    public void close() throws Exception {
        // nothing to do
    }

    @Override
    protected String[] listClassesAndResources() {
        if (!folder.exists() || !folder.isDirectory()) {
            throw new RuntimeException("Folder is no longer valid!");
        }
        ArrayList<String> files = new ArrayList<>();
        listFiles(folder, files, null);
        return files.toArray(new String[files.size()]);
    }

    private void listFiles(File folder, List<String> files, String prefix) {
        // we don't need to worry about the return value being null because we
        // know that folder exists, so we also know that it's a valid path, and
        // null is only returned on an invalid path.
        for (File f : folder.listFiles()) {
            String name = join(prefix, f.getName());
            if (f.isDirectory()) {
                listFiles(f, files, name);
            } else {
                files.add(name);
            }
        }
    }

    private String join(String prefix, String name) {
        return prefix == null ? "/" + name : prefix + "/" + name;
    }

    @Override
    public InputStream loadResource(String name) throws IOException {
        return new FileInputStream(new File(folder, name));
    }
}
