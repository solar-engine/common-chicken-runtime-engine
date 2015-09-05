/*
 * Copyright 2015 Colby Skeggs.
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

import ccre.concurrency.ReporterThread;

class DirectoryDeleterThread extends ReporterThread {

    private final File directory;

    public DirectoryDeleterThread(File directory) {
        super("DirectoryDeleter-" + directory);
        this.directory = directory;
    }

    @Override
    protected void threadBody() throws Throwable {
        deleteRecursive(directory);
    }

    private static void deleteRecursive(File dir) {
        if (dir.exists()) {
            if (dir.isDirectory()) {
                for (File f : dir.listFiles()) {
                    deleteRecursive(f);
                }
            }
            dir.delete();
        }
    }
}
