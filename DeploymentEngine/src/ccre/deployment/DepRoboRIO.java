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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.jar.Manifest;

import ccre.frc.FRCApplication;
import ccre.log.Logger;

public class DepRoboRIO {

    public static final int EXPECTED_IMAGE = 23;
    public static final boolean LIBS_THIN = false;
    public static final boolean LIBS_THICK = true;

    private static final Random random = new Random();

    public class RIOShell extends Shell {

        private RIOShell(InetAddress ip, String username, String password, boolean alwaysTrust) throws IOException {
            super(ip, username, password, alwaysTrust);
        }

        public boolean checkJRE() throws IOException {
            return exec("test -d /usr/local/frc/JRE") == 0;
        }

        public void archiveLogsTo(File destdir) throws IOException {
            if (this.exec("ls ccre-storage/log-* >/dev/null 2>/dev/null") == 0) {
                long name = random.nextLong();
                this.execCheck("tar -czf logs-" + name + ".tgz ccre-storage/log-*");
                this.execCheck("mkdir /tmp/logs-" + name + "/ && mv ccre-storage/log-* /tmp/logs-" + name + "/");
                Files.copy(this.receiveFile("logs-" + name + ".tgz"), new File(destdir, "logs-" + name + ".tgz").toPath());
                this.execCheck("rm logs-" + name + ".tgz");
            }
        }

        public void verifyRIO() throws IOException {
            verifyRIO(EXPECTED_IMAGE);
        }

        public void verifyRIO(int expected_image) throws IOException {
            int image = getRIOImage();
            if (image != expected_image) {
                throw new RuntimeException("Unsupported roboRIO image number! You need to have " + EXPECTED_IMAGE + " instead of " + image);
            }

            if (!checkJRE()) {
                throw new RuntimeException("JRE not installed! See https://wpilib.screenstepslive.com/s/4485/m/13503/l/288822-installing-java-8-on-the-roborio-using-the-frc-roborio-java-installer-java-only");
            }
        }

        public void downloadCode(File jar, RIOShell adminshell) throws IOException {
            Logger.info("Starting deployment...");
            sendFileTo(jar, "/home/lvuser/FRCUserProgram.jar");
            Logger.info("Primary deployment complete.");

            // prevent any text-busy issues
            execCheck("rm /usr/local/frc/bin/netconsole-host");
            adminshell.sendBinResourceTo(DepRoboRIO.class, "/edu/wpi/first/wpilibj/binaries/netconsole-host", "/usr/local/frc/bin/", 0755);
            sendTextResourceTo(DepRoboRIO.class, "/edu/wpi/first/wpilibj/binaries/robotCommand", "/home/lvuser/", 0755);
            Logger.info("Download complete.");
        }

        public void stopRobot() throws IOException {
            // it's okay if this fails
            exec("killall netconsole-host");
        }

        public void startRobot() throws IOException {
            execCheck(". /etc/profile.d/natinst-path.sh; /usr/local/frc/bin/frcKillRobot.sh -t -r");
        }

        public void downloadAndStart(File code) throws IOException {
            try (DepRoboRIO.RIOShell ashell = openAdminShell()) {
                ashell.stopRobot();
                downloadCode(code, ashell);
            }
            startRobot();
        }

        public void downloadAndStart(Artifact result) throws IOException {
            downloadAndStart(result.toJar(false).toFile());
        }
    }

    private static final String VERSION_BEGIN = "FRC_roboRIO_2015_v";
    private static final String DEFAULT_USERNAME = "lvuser";
    private static final String DEFAULT_PASSWORD = "";
    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "";
    private static final boolean DEFAULT_TRUST = true;

    public static File getJarFile(boolean thick) {
        File out = new File(DepProject.ccreProject("roboRIO"), thick ? "roboRIO.jar" : "roboRIO-lite.jar");
        if (!out.exists() || !out.isFile()) {
            throw new RuntimeException("roboRIO Jar cannot be found!");
        }
        return out;
    }

    public static Jar getJar(boolean thick) throws IOException {
        return new Jar(getJarFile(thick));
    }

    public static Manifest manifest(String main) {
        return DepJar.manifest("Main-Class", "ccre.frc.DirectFRCImplementation", "CCRE-Main", main, "Class-Path", ".");
    }

    public static Manifest manifest(Class<? extends FRCApplication> main) {
        // repeated FRCApplication check just to avoid getting around it at
        // runtime.
        return manifest(main.asSubclass(FRCApplication.class).getName());
    }

    public static RIOShell discoverAndVerify(int team_number) throws IOException {
        DepRoboRIO rio = discover(team_number);
        RIOShell shell = rio.openDefaultShell();
        try {
            shell.verifyRIO();
            return shell;
        } catch (Throwable thr) {
            try {
                shell.close();
            } catch (IOException ex) {
                thr.addSuppressed(ex);
            }
            throw thr;
        }
    }

    public static DepRoboRIO discover(int team_number) throws UnknownHostException {
        DepRoboRIO rio = byNameOrIP("roboRIO-" + team_number + ".local");
        if (rio == null) {
            rio = byNameOrIP("172.22.11.2");
        }
        if (rio == null) {
            rio = byNameOrIP("10." + (team_number / 100) + "." + (team_number % 100) + ".2");
        }
        if (rio == null) {
            throw new UnknownHostException("Cannot reach roboRIO over mDNS, ethernet-over-USB, or via static 10." + (team_number / 100) + "." + (team_number % 100) + ".2 address.");
        }
        return rio;
    }

    public static DepRoboRIO byNameOrIP(String ip) {
        InetAddress inaddr;
        try {
            inaddr = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            return null;
        }
        try {
            if (!inaddr.isReachable(1000)) {
                try {
                    try (Socket sock = new Socket()) {
                        sock.connect(new InetSocketAddress(inaddr, 22), 1000);
                    }
                } catch (IOException e) {
                    return null;
                }
                // otherwise, we were able to connect... so assume we're good!
            }
        } catch (IOException e) {
            return null;
        }
        return new DepRoboRIO(inaddr);
    }

    private final InetAddress ip;

    private DepRoboRIO(InetAddress ip) {
        this.ip = ip;
    }

    public RIOShell openShell(String username, String password, boolean alwaysTrust) throws IOException {
        return new RIOShell(ip, username, password, alwaysTrust);
    }

    public RIOShell openDefaultShell() throws IOException {
        return openShell(DEFAULT_USERNAME, DEFAULT_PASSWORD, DEFAULT_TRUST);
    }

    public RIOShell openAdminShell() throws IOException {
        return openShell(DEFAULT_ADMIN_USERNAME, DEFAULT_ADMIN_PASSWORD, DEFAULT_TRUST);
    }

    public int getRIOImage() throws IOException {
        URLConnection connection = new URL("http://" + ip.getHostAddress() + "/nisysapi/server").openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
            StringBuilder content = new StringBuilder();
            HashMap<String, String> map = new HashMap<>();
            map.put("Function", "GetPropertiesOfItem");
            map.put("Plugins", "nisyscfg");
            map.put("Items", "system");
            Iterator<Entry<String, String>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                content.append(URLEncoder.encode(entry.getKey(), "UTF-16LE")).append('=').append(URLEncoder.encode(entry.getValue(), "UTF-16LE"));
                if (iterator.hasNext()) {
                    content.append('&');
                }
            }
            outputStream.writeBytes(content.toString());
            outputStream.flush();
        }
        StringBuilder file = new StringBuilder();
        try (BufferedReader rin = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-16LE"))) {
            String line;
            while ((line = rin.readLine()) != null) {
                file.append(line);
            }
        }
        String contents = file.toString();
        if (!contents.contains(VERSION_BEGIN)) {
            throw new IOException("Cannot find roboRIO image version response!");
        }
        int index = contents.indexOf(VERSION_BEGIN) + VERSION_BEGIN.length();
        int end = index;
        while (Character.isDigit(contents.charAt(end))) {
            end++;
        }
        try {
            return Integer.parseInt(contents.substring(index, end));
        } catch (NumberFormatException ex) {
            throw new IOException("Could not parse roboRIO image version!", ex);
        }
    }

    public static Artifact build(File source, Class<? extends FRCApplication> main) throws IOException {
        // we need to compile against all the libraries because, if we don't,
        // the Deployment class won't build.
        // TODO: could there be a better solution for this?
        Artifact newcode = DepJava.build(source, DepRoboRIO.getJarFile(LIBS_THICK));
        return DepJar.combine(DepRoboRIO.manifest(main), JarBuilder.DELETE, newcode, DepRoboRIO.getJar(LIBS_THIN));
    }

    public static Artifact buildProject(Class<? extends FRCApplication> main) throws IOException {
        return build(DepProject.directory("src"), main);
    }
}
