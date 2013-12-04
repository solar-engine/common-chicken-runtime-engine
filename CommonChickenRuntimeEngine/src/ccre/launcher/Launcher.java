/*
 * Copyright 2013 Colby Skeggs
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
package ccre.launcher;

import ccre.cluck.tcp.StandaloneCluckServer;
import ccre.reflect.InterfaceReflectionGenerator;
import ccre.rload.RLoadClient;
import ccre.rload.RLoadServer;
import ccre.testing.SuiteOfTests;
import java.io.IOException;

/**
 * The launcher for running utilities directly from the CCRE jar.
 *
 * @author skeggsc
 */
public class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        if (args.length == 0) {
            System.err.println("Usage: java -jar CCRE.jar <TYPE> <ARGS...>");
            System.err.println("Types:");
            System.err.println("    cluck - StandaloneCluckServer");
            System.err.println("    rcli  - RLoadClient");
            System.err.println("    rserv - RLoadServer");
            System.err.println("    tests - SuiteOfTests");
            System.err.println("    reflg - ReflectionGenerator");
            return;
        }
        String a = args[0];
        String[] cargs = new String[args.length - 1];
        System.arraycopy(args, 1, cargs, 0, cargs.length);
        switch (a.charAt(1)) {
            case 'l':
                if (a.equals("cluck")) {
                    StandaloneCluckServer.main(cargs);
                    return;
                }
                break;
            case 'c':
                if (a.equals("rcli")) {
                    RLoadClient.main(cargs);
                    return;
                }
                break;
            case 's':
                if (a.equals("rserv")) {
                    RLoadServer.main(cargs);
                    return;
                }
                break;
            case 'e':
                if (a.equals("tests")) {
                    SuiteOfTests.main(cargs);
                    return;
                } else if (a.equals("reflg")) {
                    ((InterfaceReflectionGenerator) Class.forName("ccre.reflect.ReflectionGenerator").newInstance()).mainV(cargs);
                    return;
                }
                break;
        }
        System.err.println("No such launchee: " + a);
        System.exit(1);
    }
}
