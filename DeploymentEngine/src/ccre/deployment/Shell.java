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

/**
 * A connection to a remote SSH server.
 *
 * @author skeggsc
 */
public class Shell implements AutoCloseable {
    private final SSHClient client;

    /**
     * Creates a new SSH connection to <code>ip</code> with
     * <code>username</code> and <code>password</code>.
     *
     * Any host key will be accepted.
     *
     * @param ip the IP address of the remote target.
     * @param username the username of the user to log in as.
     * @param password the password of the user to log in as.
     * @throws IOException if the connection cannot be established.
     */
    public Shell(InetAddress ip, String username, String password) throws IOException {
        client = new SSHClient();
        client.setConnectTimeout(5000);
        client.setTimeout(5000);
        client.addHostKeyVerifier(new HostKeyVerifier() {
            @Override
            public boolean verify(String hostname, int port, PublicKey key) {
                // TODO: is this a huge security hole? the official version does
                // this too.
                return true;
            }
        });
        client.connect(ip);
        client.authPassword(username, password);
    }

    /**
     * Runs <code>command</code> on the remote SSH server, and throw an
     * IOException if it fails.
     *
     * @param command the command to attempt.
     * @throws IOException if the command cannot be executed or if it fails.
     */
    public void execCheck(String command) throws IOException {
        int code = exec(command);
        if (code != 0) {
            throw new IOException("Command return nonzero exit code " + code + ": '" + command + "'");
        }
    }

    /**
     * Runs <code>command</code> on the remote SSH server, and return its exit
     * code, or 257 if it timed out (took more than a minute.)
     *
     * @param command the command to attempt.
     * @return the command's exit code, or 257 if it timed out.
     * @throws IOException if the command cannot be executed.
     */
    public int exec(String command) throws IOException {
        try (Session session = client.startSession()) {
            try (Command running = session.exec(command)) {
                running.join(1, TimeUnit.MINUTES);
                Integer status = running.getExitStatus();
                return status == null ? 257 : status;
            }
        }
    }

    /**
     * Downloads a file from the remote server.
     *
     * This first downloads the file to a local temporary file and then provides
     * an input stream to read from that file.
     *
     * @param sourcePath the path on the remote end to receive a file from.
     * @return the InputStream reading from that file.
     * @throws IOException if the file cannot be received.
     */
    public InputStream receiveFile(String sourcePath) throws IOException {
        SCPFileTransfer transfer = client.newSCPFileTransfer();
        File tempFile = File.createTempFile("scp-", ".recv");
        tempFile.deleteOnExit();
        // TODO: does this actually tell us if it fails?
        transfer.download(sourcePath, new FileSystemFile(tempFile));
        return new FileInputStream(tempFile);
    }

    /**
     * Uploads a file to the remote SSH server.
     *
     * @param localFile the local file to upload.
     * @param remotePath the file or directory to upload the file to.
     * @throws IOException if the file cannot be sent.
     */
    public void sendFileTo(File localFile, String remotePath) throws IOException {
        SCPFileTransfer transfer = client.newSCPFileTransfer();
        transfer.upload(new FileSystemFile(localFile), remotePath);
    }

    /**
     * Uploads an InputStream as a file to the remote SSH server.
     *
     * @param stream an InputStream to use as the file's data.
     * @param name the name of the file on the remote end, if the remote path is
     * a directory.
     * @param remotePath the file or directory to upload the file to.
     * @param permissions the permissions for the file to have on the remote
     * end.
     * @throws IOException if the file cannot be sent.
     */
    public void sendFileTo(InputStream stream, String name, String remotePath, int permissions) throws IOException {
        if (stream == null) {
            throw new NullPointerException("Stream is NULL!");
        }
        SCPFileTransfer transfer = client.newSCPFileTransfer();
        // TODO: clean this part up?
        File temp = File.createTempFile("scp-", ".send");
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

    /**
     * Uploads a binary resource from a class to the remote SSH server. The
     * difference from {@link #sendTextResourceTo(Class, String, String, int)}
     * is that this will not modify the linefeeds on the file.
     *
     * @param clazz the class to find the resource on.
     * @param resource the resource name.
     * @param remotePath the remote path to upload the file to.
     * @param permissions the permissions for the file to have.
     * @throws IOException if the file cannot be sent.
     */
    public void sendBinResourceTo(Class<?> clazz, String resource, String remotePath, int permissions) throws IOException {
        try (InputStream resin = clazz.getResourceAsStream(resource)) {
            if (resin == null) {
                throw new RuntimeException("Cannot find resource: " + resource);
            }
            sendFileTo(resin, resource.substring(resource.lastIndexOf('/') + 1), remotePath, permissions);
        }
    }

    /**
     * Uploads a textual resource from a class to the remote SSH server. The
     * difference from {@link #sendBinResourceTo(Class, String, String, int)} is
     * that this will convert CRLFs to LFs.
     *
     * @param clazz the class to find the resource on.
     * @param resource the resource name.
     * @param remotePath the remote path to upload the file to.
     * @param permissions the permissions for the file to have.
     * @throws IOException if the file cannot be sent.
     */
    public void sendTextResourceTo(Class<?> clazz, String resource, String remotePath, int permissions) throws IOException {
        try (InputStream resin = clazz.getResourceAsStream(resource)) {
            if (resin == null) {
                throw new RuntimeException("Cannot find resource: " + resource);
            }
            // rewrites CRLF to LF.
            InputStream stripped = Utils.stripCarriageReturns(resin);
            sendFileTo(stripped, resource.substring(resource.lastIndexOf('/') + 1), remotePath, permissions);
        }
    }

    @Override
    public void close() throws IOException {
        client.close();
    }
}
