package deployment;

import ccre.deployment.Artifact;
import ccre.deployment.DepEmulator;
import ccre.deployment.DepProject;
import ccre.deployment.DepRoboRIO;
import ccre.deployment.DepTask;
import ccre.frc.FRCApplication;

public class Deployment {

    public static final Class<? extends FRCApplication> robot = org.team1540.minimal.Minimal.class;

    @DepTask
    public static void deploy() throws Exception {
        Artifact result = DepRoboRIO.buildProject(robot);

        DepRoboRIO.RIOShell rshell = DepRoboRIO.discoverAndVerify(1540);

        rshell.archiveLogsTo(DepProject.root());

        rshell.downloadAndStart(result);
    }

    @DepTask(fork=true)
    public static void emulate() throws Exception {
        Artifact result = DepRoboRIO.buildProject(robot);
        DepEmulator.emulate(result);
    }
}
