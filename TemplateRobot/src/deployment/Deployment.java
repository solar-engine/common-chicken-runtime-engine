package deployment;

import ccre.deployment.Artifact;
import ccre.deployment.DepEmulator;
import ccre.deployment.DepProject;
import ccre.deployment.DepRoboRIO;
import ccre.deployment.DepTask;
import ccre.frc.FRCApplication;

public class Deployment {

    public static final Class<? extends FRCApplication> robotMain = robot.RobotTemplate.class;

    @DepTask
    public static void deploy() throws Exception {
        Artifact result = DepRoboRIO.buildProject(robotMain);

        int number = robot.RobotTemplate.TEAM_NUMBER;

        // this seems strange, but it handles the different uses of this file well:
        //  it can be part of a new project or part of an autoupdated project.
        if (number == 0 && "robot.RobotTemplate.TEAM_NUMBER".startsWith("robot.")) {
            throw new RuntimeException("You need to change your TEAM_NUMBER in RobotTemplate.java!");
        }

        DepRoboRIO.RIOShell rshell = DepRoboRIO.discoverAndVerify(number);

        rshell.archiveLogsTo(DepProject.root());

        rshell.downloadAndStart(result);
    }

    @DepTask(fork = true)
    public static void emulate() throws Exception {
        Artifact result = DepRoboRIO.buildProject(robotMain);
        DepEmulator.emulate(result);
    }
}
