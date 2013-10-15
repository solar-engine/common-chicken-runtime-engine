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
package ccre.cluck2;

import ccre.log.LogLevel;
import ccre.log.Logger;
import ccre.util.CHashMap;
import java.io.IOException;

/**
 * A local cluck module. This is a self-hosting module, not remote.
 *
 * @author skeggsc
 */
public class LocalCluckModule implements CluckModule {

    protected final CHashMap<String, CluckConnector> ports = new CHashMap<String, CluckConnector>();
    protected final CHashMap<String, CluckModule> remotes = new CHashMap<String, CluckModule>();

    public CluckConnection connect(String route) throws IOException {
        int rend = route.indexOf('/');
        if (rend == -1) { // Local route
            CluckConnector contr = ports.get(route);
            if (contr == null) {
                throw new IOException("No such Cluck port!");
            }
            return contr.connect();
        }
        String remote = route.substring(0, rend);
        CluckModule mod = remotes.get(remote);
        if (mod == null) {
            throw new IOException("No such remote: " + remote);
        }
        return mod.connect(route.substring(rend + 1));
    }

    public void sendFlag(LogLevel level, String message, String extended, boolean hasBeenLogged) {
        if (!hasBeenLogged) {
            Logger.logExt(level, message, extended);
        }
        for (String modname : remotes) {
            remotes.get(modname).sendFlag(level, message, extended, true);
        }
    }
}
