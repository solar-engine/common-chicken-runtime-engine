package deployment;

import ccre.deployment.Artifact;
import ccre.deployment.DepEmulator;
import ccre.deployment.DepProject;
import ccre.deployment.DepRoboRIO;
import ccre.deployment.DepTask;
import ccre.frc.FRCApplication;

/**
 * The Deployment class of your project. When your project is built, the static
 * methods in this class that are annotated with <code>@DepTask</code> will be
 * available as options in the Eclipse External Tools menu.
 */
public class Deployment {

    /**
     * The reference to your main class. When you change which class is the main
     * class of your project, make sure to change this line.
     */
    public static final Class<? extends FRCApplication> robotMain = robot.RobotTemplate.class;

    /**
     * A deployment task that downloads your robot code to a roboRIO found based
     * on your team number.
     *
     * @throws Exception
     */
    @DepTask
    public static void deploy() throws Exception {
        Artifact result = DepRoboRIO.buildProject(robotMain);

        int number = robot.RobotTemplate.TEAM_NUMBER;

        if (number == 0) {
            throw new RuntimeException("You need to change your TEAM_NUMBER in RobotTemplate.java!");
        }

        try (DepRoboRIO.RIOShell rshell = DepRoboRIO.discoverAndVerify(number)) {
            rshell.archiveLogsTo(DepProject.root());

            rshell.downloadAndStart(result);
        }
    }

    /**
     * A deployment task that runs your robot code in the CCRE's emulator.
     *
     * @throws Exception
     */
    @DepTask(fork = true)
    public static void emulate() throws Exception {
        Artifact result = DepRoboRIO.buildProject(robotMain);
        DepEmulator.emulate(result);
    }
}
