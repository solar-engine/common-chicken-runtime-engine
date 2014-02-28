/*
 * Copyright 2014 Gregor Peach, Colby Skeggs (bugfixes)
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
package intelligence;

import ccre.log.Logger;
import ccre.util.CArrayList;

/**
 * A folder displayed on the intelligence panel.
 *
 * @author peachg
 */
public final class Folder extends Remote {

    protected boolean open = false, hascontents = false;
    protected final CArrayList<Remote> contents = new CArrayList<Remote>();
    protected int place;
    protected final String ID;
    protected final String REGEX;

    public Folder(String ID, String regex) {
        super("", 0, null);
        this.REGEX = regex;
        this.ID = ID;
    }

    @Override
    protected Object checkout() {
        Logger.warning("Can't checkout a folder!");
        return null;
    }

    public boolean isInside(Remote s) {
        return s.path.matches(REGEX);
    }

    @Override
    public String toString() {
        return (!hascontents ? "x " : open ? "- " : "+ ") + ID;
    }
}
