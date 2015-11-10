/*
 * Copyright 2015 Colby Skeggs
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
package ccre.upgrade;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import javax.swing.JOptionPane;

public class Upgrade {
    public static void main(String[] args) throws IOException {
        System.out.println("If you have any issues, please ask about them on the forum thread on Chief Delphi at http://www.chiefdelphi.com/forums/showthread.php?t=130813");

        File projectDir = new File(args[0]);
        String project = projectDir.getName();
        File templateDir = new File(args[1]);

        if (!projectDir.exists() || !projectDir.isDirectory() || !templateDir.exists() || !templateDir.isDirectory()) {
            throw new IOException("Expected both project and template to exist.");
        }

        String message = "A project that you are trying to build (" + project + ") is set up for CCRE version 2, but you are using CCRE version 3.\nWould you like to upgrade the project automatically?\nIf you have any issues, please ask about them on the forum thread on Chief Delphi.";

        if (JOptionPane.showConfirmDialog(null, message, "CCRE Project Upgrade", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            System.err.println("Upgrade cancelled.");
            System.exit(1);
        }
        String num = JOptionPane.showInputDialog("Enter your team number");
        if (num == null) {
            System.err.println("Upgrade cancelled.");
            System.exit(1);
            return;
        }
        int team_number;
        try {
            team_number = Integer.parseInt(num);
        } catch (NumberFormatException ex) {
            System.err.println("Upgrade cancelled: not a number.");
            System.exit(1);
            return;
        }

        Properties props = new Properties();
        props.load(new FileInputStream(new File(projectDir, "src/main.properties")));
        String igneous_main = props.getProperty("igneous.main");

        for (String name : new String[] { ".externalToolBuilders/Target Rebuilder.launch", ".classpath" }) {
            Path target = new File(projectDir, name).toPath();
            Files.deleteIfExists(target);
            Files.copy(new File(templateDir, name).toPath(), target);
        }

        rewrite(new File(templateDir, "src/deployment/Deployment.java").toPath(), new File(projectDir, "src/deployment/Deployment.java").toPath(), team_number, igneous_main);

        Path backup = new File(projectDir, "src-backup-" + System.currentTimeMillis()).toPath();
        Path target = new File(projectDir, "src").toPath();
        Files.move(target, backup);

        Iterable<Path> paths = Files.walk(backup)::iterator;
        for (Path p : paths) {
            if (Files.isDirectory(p)) {
                continue;
            }
            Path mirror = target.resolve(backup.relativize(p));
            if (p.toFile().getName().endsWith(".java")) {
                System.out.println("Rewriting: " + p + " to " + mirror + "!");
                rewrite(p, mirror, team_number, igneous_main);
            } else {
                Files.createDirectories(mirror.getParent());
                Files.copy(p, mirror);
            }
        }

        Files.delete(new File(projectDir, "src/main.properties").toPath());

        System.out.println("Completed successfully!");
    }

    private static void rewrite(Path template, Path output, int team_number, String igneous_main) throws IOException, FileNotFoundException {
        try (BufferedReader reader = Files.newBufferedReader(template)) {
            Files.createDirectories(output.getParent());
            try (BufferedWriter w = Files.newBufferedWriter(output)) {
                while (true) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    line = rewrite(line, team_number, igneous_main);
                    w.write(line + "\n");
                }
            }
        }
    }

    private static final String[] replaces = new String[] {

            "InputPoll", "Input",

            "Igneous", "FRC", "igneous", "frc",

            "ccre.ctrl.Ticker", "ccre.timers.Ticker", "ccre.ctrl.ExpirationTimer", "ccre.timers.ExpirationTimer", "ccre.ctrl.PauseTimer", "ccre.timers.PauseTimer",

            "ccre.holders.TuningContext", "ccre.tuning.TuningContext",

            "FRC.getPCMPressureSwitch", "FRC.pressureSwitchPCM", "FRC.usePCMCompressor", "FRC.compressorPCM", "FRC.getPCMCompressorRunning", "FRC.compressorRunningPCM",

            "getSetFalseEvent()", "eventSet(false)", "getSetTrueEvent()", "eventSet(true)", "getSetEvent", "eventSet",

            "getIsTest", "inTestMode", "getIsAutonomous", "inAutonomousMode", "getIsTeleop", "inTeleopMode", "getIsDisabled", "robotDisabled",

            "makeTalonMotor", "talon", "makeVictorMotor", "victor", "makeJaguarMotor", "jaguar",
            
            "makeEncoder", "encoder",
            
            "import ccre.ctrl.Mixing;", "", "import ccre.ctrl.FloatMixing;", "", "import ccre.ctrl.EventMixing;", "", "import ccre.ctrl.BooleanMixing;", "",

            "makeCANTalon", "talonCAN", "makeCANJaguar", "jaguarCAN",

            "makeSolenoid", "solenoid", "makeDigitalInput", "digitalInput", "makeAnalogInput", "analogInput", "makeDigitalOutput", "digitalOutput",

            "makeRS232_MXP", "mxpRS232", "makeRS232_Onboard", "onboardRS232", "makeRS232_USB", "usbRS232",

            "FloatMixing.always", "FloatInput.always", "FloatMixing.always(0)", "FloatInput.zero", "FloatMixing.always(0.0f)", "FloatInput.zero",

            "FloatMixing.ignoredFloatOutput", "FloatOutput.ignored", "BooleanMixing.ignoredFloatOutput", "BooleanOutput.ignored", "EventMixing.ignored", "EventOutput.ignored",

            "BooleanStatus", "BooleanCell", "FloatStatus", "FloatCell", "EventStatus", "EventCell",
            
            "asInvertedInput()", "not()",
            
            "FRC.getPDPChannelCurrent", "FRC.channelCurrentPDP",
            
            "getButtonSource", "onPress", "getButtonChannel", "button",
            
            "getAxisSource", "axis", "getAxisChannel", "axis", "getXAxisSource", "axisX", "getYAxisSource", "axisY", "getXChannel", "axisX", "getYChannel", "axisY",
    };

    private static String rewrite(String line, int team_number, String igneous_main) {
        line = line.replace("robot.RobotTemplate.TEAM_NUMBER", Integer.toString(team_number)).replace("robot.RobotTemplate", igneous_main);
        for (int i = 0; i < replaces.length; i += 2) {
            line = line.replace(replaces[i], replaces[i + 1]);
        }
        return line;
    }
}
