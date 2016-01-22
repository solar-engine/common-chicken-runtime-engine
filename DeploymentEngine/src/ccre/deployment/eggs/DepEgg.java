/*
 * Copyright 2015-2016 Alexander Mackworth, 2016 Colby Skeggs
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
package ccre.deployment.eggs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.jar.Manifest;

import ccre.deployment.Artifact;
import ccre.deployment.DepProject;
import ccre.deployment.DepJar;
import ccre.deployment.DepRoboRIO;
import ccre.deployment.Jar;
import ccre.deployment.JarBuilder;
import ccre.log.Logger;

/**
 * A utility for packaging an artifact and artifact hatching action into an
 * executable Jar.
 *
 * @author amackworth
 */
public class DepEgg {
    static final String userCodeJarName = "usercode.jar";

    /**
     * Packages together an artifact and an action to take to deploy that
     * artifact, and puts them in the "eggs" folder of the current project.
     *
     * <code>hatchAction</code> must be an instance of a class or anonymous
     * class with a constructor that takes no arguments. It may not be a lambda.
     * The constructor must take no arguments, because the action will be
     * re-instantiated during deployment, and no data will be preserved other
     * than the class name.
     *
     * @param userCode the artifact to include
     * @param hatchAction the action to run on hatching
     * @throws IOException if the packaging fails for some reason
     */
    public static void layEgg(Artifact userCode, ArtifactDeployer hatchAction) throws IOException {
        Logger.info("Laying egg...");

        File userJar = userCode.toJar(JarBuilder.DELETE).toFile();
        Manifest manifest = DepJar.manifest("Main-Class", EggHatcher.class.getName(), "Hatch-Action", hatchAction.getClass().getName());

        JarBuilder builder = new JarBuilder(manifest, JarBuilder.DELETE);
        builder.addResource(userCodeJarName, userJar);

        try (Jar roboRIOProject = new Jar(DepRoboRIO.getJarFile(DepRoboRIO.LIBS_THICK))) {
            builder.addAll(roboRIOProject, JarBuilder.DISCARD_MANIFEST);
            try (Jar egg = builder.build()) {
                File folder = new File(DepProject.root(), "eggs");
                if (!folder.isDirectory() && !folder.mkdir()) {
                    Logger.severe("Could not create egg folder!");
                    return;
                }

                LocalDateTime now = LocalDateTime.now();
                String formattedDate = now.getMonthValue() + "_" + now.getDayOfMonth() + "_" + now.getHour() + "_" + now.getMinute();

                File sourceEgg = egg.toFile();
                File targetEgg = new File(folder, DepProject.name() + "_" + formattedDate + ".jar");
                Files.copy(sourceEgg.toPath(), targetEgg.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}
