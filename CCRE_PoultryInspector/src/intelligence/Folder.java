/*
 * Copyright 2013 Gregor Peach
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
 *
 * @author peachg
 */
public class Folder extends Remote{
    protected boolean open=true;
    protected CArrayList<Remote> contents=new CArrayList<Remote>();
    protected int place;
    protected String ID;
    protected String REGEX;
    public Folder(String ID,String regex){
        super("",0,null);
        this.REGEX=regex;
        this.ID=ID;
    }
    @Override
    protected void checkout(){
        Logger.warning("Can't checkout a folder");
    }
    public boolean isValid(Remote s){
        return s.toString().contains(REGEX);
    }
    @Override
    public String toString(){
        return ID+" : "+"Folder";
    }
}
