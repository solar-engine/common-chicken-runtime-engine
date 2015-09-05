package deployment;

import java.io.IOException;

import ccre.deployment.Artifact;
import ccre.deployment.DepProject;
import ccre.deployment.DepRoboRIO;
import ccre.deployment.DepTask;
import teamNNNN.RobotTemplate;

public class Deployment {

    @DepTask("Deploy")
    public static void deploy() throws IOException {
        Artifact result = DepRoboRIO.buildProject(RobotTemplate.class);

        DepRoboRIO.RIOShell rshell = DepRoboRIO.discoverAndVerify(1540);

        rshell.archiveLogsTo(DepProject.root());

        rshell.downloadAndStart(result);
    }
}
