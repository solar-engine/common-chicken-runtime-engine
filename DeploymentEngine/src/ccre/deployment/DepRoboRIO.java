/*
 * Copyright 2015-2016 Cel Skeggs
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
import ccre.verifier.PhaseVerifier;

/**
 * A collection of utilities for building and downloading code for the roboRIO.
 * An instance of this class represents a specific discovered roboRIO on the
 * network.
 *
 * @author skeggsc
 */
public class DepRoboRIO {

    /**
     * The expected image number for the roboRIO to have.
     */
    public static final int EXPECTED_IMAGE = 2016019;
    /**
     * Specifies that the THIN version of the roboRIO libraries should be used,
     * which does not include the DeploymentEngine or Emulator.
     */
    public static final boolean LIBS_THIN = false;
    /**
     * Specifies that the THICK version of the roboRIO libraries should be used,
     * which includes the DeploymentEngine and Emulator.
     */
    public static final boolean LIBS_THICK = true;

    private static final Random random = new Random();

    /**
     * A SSH connection to the roboRIO as a specific account. This connection
     * can be used to interact with the roboRIO in various ways.
     *
     * @author skeggsc
     */
    public class RIOShell extends Shell {

        private RIOShell(InetAddress ip, String username, String password) throws IOException {
            super(ip, username, password);
        }

        /**
         * Check to see if the JRE is currently installed on the roboRIO.
         *
         * @return true if the JRE is installed, or false otherwise.
         * @throws IOException if the connection fails.
         */
        public boolean checkJRE() throws IOException {
            return exec("test -d /usr/local/frc/JRE") == 0;
        }

        /**
         * If there are any logfiles on the robot, packages them up and stick
         * the tar.gz package them into the specified directory.
         *
         * @param destdir the directory to archive logfiles into.
         * @throws IOException if the connection fails, or the archive cannot be
         * written out.
         */
        public void archiveLogsTo(File destdir) throws IOException {
            if (this.exec("ls ccre-storage/log-* >/dev/null 2>/dev/null") == 0) {
                long name = random.nextLong();
                this.execCheck("tar -czf logs-" + name + ".tgz ccre-storage/log-*");
                this.execCheck("mkdir /tmp/logs-" + name + "/ && mv ccre-storage/log-* /tmp/logs-" + name + "/");
                Files.copy(this.receiveFile("logs-" + name + ".tgz"), new File(destdir, "logs-" + name + ".tgz").toPath());
                this.execCheck("rm logs-" + name + ".tgz");
            }
        }

        /**
         * Verifies that the roboRIO is set up with the
         * {@link DepRoboRIO#EXPECTED_IMAGE} and an installed JRE. An
         * explanatory exception is thrown if not.
         *
         * @throws IOException if something fails during attempts to verify.
         */
        public void verifyRIO() throws IOException {
            verifyRIO(EXPECTED_IMAGE);
        }

        /**
         * Verifies that the roboRIO is set up with <code>expected_image</code>
         * as the image and a properly installed JRE. An explanatory exception
         * is thrown if not.
         *
         * @param expected_image the image number that is expected to be found.
         * If it is less than 1000, 2015000 is added to match the new image
         * number versioning scheme.
         * @throws IOException if something fails during attempts to verify.
         */
        public void verifyRIO(int expected_image) throws IOException {
            if (expected_image < 1000) {
                expected_image += 2015000;
            }
            int image = getRIOImageAndYear();
            if (image != expected_image) {
                throw new RuntimeException("Unsupported roboRIO image number! You need to have " + expected_image + " instead of " + image);
            }

            if (!checkJRE()) {
                throw new RuntimeException("JRE not installed! See https://wpilib.screenstepslive.com/s/4485/m/13503/l/288822-installing-java-8-on-the-roborio-using-the-frc-roborio-java-installer-java-only");
            }
        }

        /**
         * Downloads the specified Jar as a program to the roboRIO, along with
         * the necessary supporting scripts. A RIOShell for a connection with
         * administrator access is required.
         *
         * @param jar the Jar to download.
         * @param adminshell a RIOShell with administrator access.
         * @throws IOException if something fails during download.
         */
        public void downloadCode(File jar, RIOShell adminshell) throws IOException {
            Logger.info("Starting deployment...");
            sendFileTo(jar, "/home/lvuser/FRCUserProgram.jar");
            Logger.info("Primary deployment complete.");

            // prevent any text-busy issues
            adminshell.execCheck("rm -f /usr/local/frc/bin/netconsole-host");
            adminshell.sendBinResourceTo(DepRoboRIO.class, "/edu/wpi/first/wpilibj/binaries/netconsole-host", "/usr/local/frc/bin/", 0755);
            sendTextResourceTo(DepRoboRIO.class, "/edu/wpi/first/wpilibj/binaries/robotCommand", "/home/lvuser/", 0755);
            Logger.info("Download complete.");
        }

        /**
         * Attempts to stop any running robot code. If the code cannot be
         * stopped, or was not running, it doesn't report any errors, as this
         * doesn't usually end up being a problem.
         *
         * @throws IOException if the connection fails.
         */
        public void stopRobot() throws IOException {
            // it's okay if this fails
            exec("killall netconsole-host");
        }

        /**
         * Starts the currently-loaded robot code, including stopping any
         * currently-running robot code.
         *
         * @throws IOException if the connection or the attempt fails.
         */
        public void startRobot() throws IOException {
            execCheck(". /etc/profile.d/natinst-path.sh; /usr/local/frc/bin/frcKillRobot.sh -t -r");
        }

        /**
         * Downloads the Jar file <code>code</code> to the robot, and restarts
         * the robot code.
         *
         * @param code the Jar file to download.
         * @throws IOException if something fails.
         */
        public void downloadAndStart(File code) throws IOException {
            try (DepRoboRIO.RIOShell ashell = openAdminShell()) {
                ashell.stopRobot();
                downloadCode(code, ashell);
            }
            startRobot();
        }

        /**
         * Downloads the Artifact <code>result</code> to the robot, once
         * converted to a Jar, and restarts the robot code.
         *
         * @param result the Artifact to download.
         * @throws IOException if something fails.
         */
        public void downloadAndStart(Artifact result) throws IOException {
            downloadAndStart(result.toJar(false).toFile());
        }
    }

    private static final String VERSION_BEGIN = "FRC_roboRIO_";
    private static final String DEFAULT_USERNAME = "lvuser";
    private static final String DEFAULT_PASSWORD = "";
    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "";

    /**
     * Finds the path to the roboRIO compiled Jar file, either the thick or thin
     * version depending on whether {@link #LIBS_THICK} or {@link #LIBS_THIN} is
     * specified.
     *
     * The difference between the two is that LIBS_THICK also includes the
     * Deployment Engine and Emulator.
     *
     * @param thick if the thick version should be used.
     * @return the discovered Jar file.
     */
    public static File getJarFile(boolean thick) {
        File out = new File(DepProject.ccreProject("roboRIO"), thick ? "roboRIO.jar" : "roboRIO-lite.jar");
        if (!out.exists() || !out.isFile()) {
            throw new RuntimeException("roboRIO Jar cannot be found!");
        }
        return out;
    }

    /**
     * Provides the roboRIO compiled Jar as a {@link Jar}, either the thick or
     * thin version depending on whether {@link #LIBS_THICK} or
     * {@link #LIBS_THIN} is specified.
     *
     * The difference between the two is that LIBS_THICK also includes the
     * Deployment Engine and Emulator.
     *
     * @param thick if the thick version should be used.
     * @return the Jar artifact.
     * @throws IOException if the Jar is not properly found
     */
    public static Jar getJar(boolean thick) throws IOException {
        return new Jar(getJarFile(thick));
    }

    /**
     * Generates the correct Manifest for a roboRIO application that has the
     * specified CCRE main class.
     *
     * The main class must implement {@link ccre.frc.FRCApplication}
     *
     * @param main the main class in dot form, for example
     * <code>org.team1540.example.Example</code>.
     * @return the generated Manifest.
     */
    public static Manifest manifest(String main) {
        return DepJar.manifest("Main-Class", "ccre.frc.DirectFRCImplementation", "CCRE-Main", main, "Class-Path", ".");
    }

    /**
     * Generates the correct Manifest for a roboRIO application that has the
     * specified CCRE main class. The class must implement
     * {@link ccre.frc.FRCApplication}.
     *
     * @param main the main class as a Class object.
     * @return the generated Manifest.
     */
    public static Manifest manifest(Class<? extends FRCApplication> main) {
        // repeated FRCApplication check just to avoid getting around it at
        // runtime.
        return manifest(main.asSubclass(FRCApplication.class).getName());
    }

    /**
     * Discovers a roboRIO on the network for the specified team number, and
     * then verifies that it is set up properly.
     *
     * This discovers a roboRIO in the same way as {@link #discover(int)}, and
     * then verifies it in the same way as {@link RIOShell#verifyRIO()}.
     *
     * @param team_number the team number, used for determining where to look
     * for roboRIOs.
     * @return a {@link RIOShell} providing access to the roboRIO.
     * @throws IOException if something fails during these steps.
     */
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

    /**
     * Discovers a roboRIO on the network for the specified team number. This
     * tries mDNS, USB, and the fallback 10.XX.YY.2 address.
     *
     * @param team_number the team number to use to calculate mDNS names and
     * fallback addresses.
     * @return the discovered DepRoboRIO instance that represents the discovered
     * remote roboRIO.
     * @throws UnknownHostException if a roboRIO cannot be found.
     */
    public static DepRoboRIO discover(int team_number) throws UnknownHostException {
        DepRoboRIO rio = byNameOrIP("roboRIO-" + team_number + "-FRC.local");
        if (rio == null) {
            rio = byNameOrIP("172.22.11.2");
        }
        if (rio == null) {
            rio = byNameOrIP("10." + (team_number / 100) + "." + (team_number % 100) + ".2");
        }
        if (rio == null) {
            // 2015 mDNS name format
            rio = byNameOrIP("roboRIO-" + team_number + ".local");
        }
        if (rio == null) {
            throw new UnknownHostException("Cannot reach roboRIO over mDNS, ethernet-over-USB, or via static 10." + (team_number / 100) + "." + (team_number % 100) + ".2 address.");
        }
        return rio;
    }

    /**
     * Attempts to find a roboRIO on the network at <code>ip</code>.
     *
     * @param ip the IP address or hostname to try to connect to.
     * @return the discovered DepRoboRIO instance that represents the discovered
     * remote roboRIO, or null if it isn't found.
     */
    public static DepRoboRIO byNameOrIP(String ip) {
        InetAddress inaddr;
        try {
            inaddr = InetAddress.getByName(ip);
            try (Socket sock = new Socket()) {
                sock.connect(new InetSocketAddress(inaddr, 22), 500);
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

    /**
     * Connects to this roboRIO with a username and password.
     *
     * @param username the username for the user, often <code>lvuser</code> or
     * <code>admin</code>.
     * @param password the password to use to connect, often the empty string.
     * @return the newly-opened connection.
     * @throws IOException if the connection cannot be established.
     */
    public RIOShell openShell(String username, String password) throws IOException {
        return new RIOShell(ip, username, password);
    }

    /**
     * Connects to this roboRIO with the default username and password for the
     * main user account.
     *
     * @return the newly-opened connection.
     * @throws IOException if the connection cannot be established.
     */
    public RIOShell openDefaultShell() throws IOException {
        return openShell(DEFAULT_USERNAME, DEFAULT_PASSWORD);
    }

    /**
     * Connects to this roboRIO with the default username and password for the
     * administrator user account.
     *
     * @return the newly-opened connection.
     * @throws IOException if the connection cannot be established.
     */
    public RIOShell openAdminShell() throws IOException {
        return openShell(DEFAULT_ADMIN_USERNAME, DEFAULT_ADMIN_PASSWORD);
    }

    /**
     * Determines the image version installed on this roboRIO, excluding the
     * year.
     *
     * @return the image number. For example, 23 for
     * <code>FRC_roboRIO_2015_v23</code>.
     * @throws IOException if the roboRIO's responses do not match the format
     * expectations.
     * @deprecated the year is very important! do not use this otherwise.
     */
    @Deprecated
    public int getRIOImage() throws IOException {
        return getRIOImageAndYear() % 1000;
    }

    /**
     * Determines the image version installed on this roboRIO, including the
     * year.
     *
     * @return the image number. For example, 2015023 for
     * <code>FRC_roboRIO_2015_v23</code>, or 2016019 for
     * <code>FRC_roboRIO_2016_v19</code>.
     * @throws IOException if the roboRIO's responses do not match the format
     * expectations.
     */
    public int getRIOImageAndYear() throws IOException {
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
        int year;
        try {
            year = Integer.parseInt(contents.substring(index, end));
        } catch (NumberFormatException ex) {
            throw new IOException("Could not parse roboRIO image version!", ex);
        }
        if (contents.charAt(end) != '_' || contents.charAt(end + 1) != 'v') {
            throw new IOException("Cannot find valid roboRIO image version response!");
        }
        index = end += 2;
        while (Character.isDigit(contents.charAt(end))) {
            end++;
        }
        int rev;
        try {
            rev = Integer.parseInt(contents.substring(index, end));
        } catch (NumberFormatException ex) {
            throw new IOException("Could not parse roboRIO image version!", ex);
        }
        return year * 1000 + rev; // for example, 2015_v19 becomes 2015019
    }

    /**
     * Builds a directory of source files for a project against the roboRIO
     * support classes, and combines it with the roboRIO support libraries.
     *
     * @param source the directory of source files.
     * @param main the main class of the application.
     * @return the resulting Artifact of everything combined.
     * @throws IOException if the build or combination fails.
     */
    public static Artifact build(File source, Class<? extends FRCApplication> main) throws IOException {
        // we need to compile against all the libraries because, if we don't,
        // the Deployment class won't build.
        // TODO: could there be a better solution for this?
        Artifact newcode = DepJava.build(source, DepRoboRIO.getJarFile(LIBS_THICK));
        try (Jar jar = DepRoboRIO.getJar(LIBS_THICK)) {
            PhaseVerifier.verify(newcode, jar);
        }
        return DepJar.combine(DepRoboRIO.manifest(main), JarBuilder.DELETE, newcode, DepRoboRIO.getJar(LIBS_THIN));
    }

    /**
     * Builds the current project against the roboRIO support classes, and
     * combines it with the roboRIO support libraries.
     *
     * This expects that the current project has a <code>src</code> directory.
     *
     * @param main the main class of the application.
     * @return the resulting Artifact of everything combined.
     * @throws IOException if the build or combination fails.
     */
    public static Artifact buildProject(Class<? extends FRCApplication> main) throws IOException {
        return build(DepProject.directory("src"), main);
    }
}
