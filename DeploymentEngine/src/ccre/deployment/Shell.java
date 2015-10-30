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
import java.security.PublicKey;
import java.util.concurrent.TimeUnit;

import ccre.util.Utils;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;
import net.schmizz.sshj.xfer.FileSystemFile;
import net.schmizz.sshj.xfer.InMemorySourceFile;
import net.schmizz.sshj.xfer.scp.SCPFileTransfer;

public class Shell implements AutoCloseable {
    private final SSHClient client;

    public Shell(InetAddress ip, String username, String password, boolean alwaysTrust) throws IOException {
        client = new SSHClient();
        client.setConnectTimeout(5000);
        client.setTimeout(5000);
        client.addHostKeyVerifier(new HostKeyVerifier() {
            @Override
            public boolean verify(String hostname, int port, PublicKey key) {
                return true; // TODO: is this a huge security hole? the official version does this too.
            }
        });
        client.connect(ip);
        client.authPassword(username, password);
    }

    public void execCheck(String command) throws IOException {
        int code = exec(command);
        if (code != 0) {
            throw new IOException("Command return nonzero exit code " + code + ": '" + command + "'");
        }
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

    public void sendFileTo(InputStream stream, String name, String remotePath, int permissions) throws IOException {
        if (stream == null) {
            throw new NullPointerException("Stream is NULL!");
        }
        SCPFileTransfer transfer = client.newSCPFileTransfer();
        File temp = File.createTempFile("scp-", ".send"); // TODO: clean this part up?
        temp.delete();
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

            @Override
            public int getPermissions() throws IOException {
                return permissions;
            }
        }, remotePath);
    }

    public void sendBinResourceTo(Class<?> clazz, String resource, String remotePath, int permissions) throws IOException {
        try (InputStream resin = clazz.getResourceAsStream(resource)) {
            if (resin == null) {
                throw new RuntimeException("Cannot find resource: " + resource);
            }
            sendFileTo(resin, resource.substring(resource.lastIndexOf('/') + 1), remotePath, permissions);
        }
    }

    public void sendTextResourceTo(Class<?> clazz, String resource, String remotePath, int permissions) throws IOException {
        // rewrites CRLF to LF.
        try (InputStream resin = clazz.getResourceAsStream(resource)) {
            if (resin == null) {
                throw new RuntimeException("Cannot find resource: " + resource);
            }
            InputStream stripped = Utils.stripCarriageReturns(resin);
            sendFileTo(stripped, resource.substring(resource.lastIndexOf('/') + 1), remotePath, permissions);
        }
    }

    @Override
    public void close() throws IOException {
        client.close();
    }
}
