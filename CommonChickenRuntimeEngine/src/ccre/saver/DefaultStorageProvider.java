/*
 * Copyright 2013-2015 Colby Skeggs
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
package ccre.saver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The default storage provider. This will store data in the subdirectory
 * 'ccre_storage' of the current directory, using builtin java.io classes. This
 * is not suitable for running on Squawk.
 *
 * @author skeggsc
 */
public class DefaultStorageProvider extends StorageProvider {

    private final File basedir;

    /**
     * Create a new StorageProvider backed by the filesystem in the current
     * working directory.
     */
    public DefaultStorageProvider() {
        basedir = new File(".");
    }

    /**
     * Create a new StorageProvider backed by the filesystem in basedir.
     * 
     * @param basedir the directory to store CCRE data in.
     */
    public DefaultStorageProvider(File basedir) {
        this.basedir = basedir;
    }

    /**
     * Register a new StorageProvider backed by the filesystem in basedir.
     * 
     * @param basedir the directory to store CCRE data in.
     */
    public static void register(File basedir) {
        StorageProvider.setProvider(new DefaultStorageProvider(basedir));
    }

    @Override
    protected OutputStream openOutputFile(String name) throws IOException {
        return new FileOutputStream(new File(basedir, name));
    }

    @Override
    protected InputStream openInputFile(String name) throws IOException {
        try {
            return new FileInputStream(new File(basedir, name));
        } catch (FileNotFoundException ex) {
            return null;
        }
    }

}
