package deployment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import javax.swing.JOptionPane;

import ccre.deployment.Artifact;
import ccre.deployment.DepEmulator;
import ccre.deployment.DepProject;
import ccre.deployment.DepRoboRIO;
import ccre.deployment.DepTask;
import ccre.deployment.eggs.ArtifactDeployer;
import ccre.deployment.eggs.DepEgg;
import ccre.frc.FRCApplication;
import ccre.log.Logger;

public class Deployment {

    private static Class<? extends FRCApplication> getRobot() {
        File src = DepProject.directory("src");
        ArrayList<Class<? extends FRCApplication>> classes = new ArrayList<>();
        for (File f : src.listFiles()) {
            walk(f, f.getName(), classes);
        }
        Iterator<Class<? extends FRCApplication>> iterator = classes.iterator();
        while (iterator.hasNext()) {
            Class<? extends FRCApplication> app = iterator.next();
            if (app.getName().contains("firstfare")) {
                iterator.remove();
            }
        }
        Collections.sort(classes, (a, b) -> a.getName().compareTo(b.getName()));
        return selectFrom(classes);
    }

    private static <T> T selectFrom(ArrayList<T> classes) {
        Object[] poss = classes.toArray();
        Object s = JOptionPane.showInputDialog(null, "Select the main class to deploy:", "Sample Selector", JOptionPane.PLAIN_MESSAGE, null, poss, poss[0]);
        if (!classes.contains(s)) {
            throw new RuntimeException("Selected nothing.");
        }
        return (T) s;
    }

    private static void walk(File f, String path, Collection<Class<? extends FRCApplication>> out) {
        if (f.isDirectory()) {
            for (File f2 : f.listFiles()) {
                walk(f2, path + "." + f2.getName().split("[.]")[0], out);
            }
        } else {
            if (f.getName().endsWith(".java")) {
                try {
                    Class<?> other = Class.forName(path, false, Deployment.class.getClassLoader());
                    out.add(other.asSubclass(FRCApplication.class));
                } catch (ClassCastException e) {
                    // ignore and move on - clearly not a main class
                } catch (ClassNotFoundException e) {
                    Logger.warning("Could not find class that we thought we found", e);
                }
            }
        }
    }

    @DepTask
    public static void deploy() throws Exception {
        Artifact result = DepRoboRIO.buildProject(getRobot());

        try (DepRoboRIO.RIOShell rshell = DepRoboRIO.discoverAndVerify(1540)) {
            rshell.archiveLogsTo(DepProject.root());

            rshell.downloadAndStart(result);
        }
    }

    @DepTask(fork = true)
    public static void emulate() throws Exception {
        Artifact result = DepRoboRIO.buildProject(getRobot());
        DepEmulator.emulate(result);
    }

    @DepTask
    public static void layEgg() throws Exception {
        Artifact result = DepRoboRIO.buildProject(getRobot());
        DepEgg.layEgg(result, new ArtifactDeployer() {
            @Override
            public void deployArtifact(Artifact artifact) throws Exception {
                try (DepRoboRIO.RIOShell rshell = DepRoboRIO.discoverAndVerify(1540)) {
                    rshell.downloadAndStart(artifact);
                }
            }
        });
    }
}
