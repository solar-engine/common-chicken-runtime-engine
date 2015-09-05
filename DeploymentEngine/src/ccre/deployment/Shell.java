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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.xfer.FileSystemFile;
import net.schmizz.sshj.xfer.InMemorySourceFile;
import net.schmizz.sshj.xfer.scp.SCPFileTransfer;

public class Shell implements AutoCloseable {
    private final SSHClient client;

    public Shell(InetAddress ip, String username, String password, boolean alwaysTrust) throws IOException {
        client = new SSHClient();
        client.authPassword(username, password);
        client.connect(ip);
    }

    public int exec(String command) throws IOException {
        try (Session session = client.startSession()) {
            try (Command running = session.exec(command)) {
                running.join(1, TimeUnit.MINUTES);
                Integer status = running.getExitStatus();
                return status == null ? 257 : status;
            }
        }
    }

    public InputStream receiveFile(String sourcePath) throws IOException {
        SCPFileTransfer transfer = client.newSCPFileTransfer();
        File tempFile = File.createTempFile("scp-", ".recv");
        tempFile.deleteOnExit();
        transfer.download(sourcePath, new FileSystemFile(tempFile));// TODO: does this actually tell us if it fails?
        return new FileInputStream(tempFile);
    }

    public void sendFileTo(File localFile, String remotePath) throws IOException {
        SCPFileTransfer transfer = client.newSCPFileTransfer();
        transfer.upload(new FileSystemFile(localFile), remotePath);
    }

    public void sendFileTo(InputStream stream, String name, String remotePath) throws IOException {
        SCPFileTransfer transfer = client.newSCPFileTransfer();
        File temp = File.createTempFile("scp-", ".send");
        temp.deleteOnExit();
        Files.copy(stream, temp.toPath());
        transfer.upload(new InMemorySourceFile() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public long getLength() {
                return temp.length();
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new FileInputStream(temp);
            }
        }, remotePath);
    }

    public void sendResourceTo(Class<?> clazz, String resource, String remotePath) throws IOException {
        sendFileTo(clazz.getResourceAsStream(resource), resource.substring(resource.lastIndexOf('/') + 1), remotePath);
    }

    @Override
    public void close() throws IOException {
        client.close();
    }
}
