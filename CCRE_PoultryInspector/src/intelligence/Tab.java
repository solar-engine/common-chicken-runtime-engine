/*
 * Copyright 2013-2014 Gregor Peach
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

import ccre.log.LogLevel;
import ccre.log.Logger;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A rectangle on screen that can be clicked to show a set of remotes.
 * @author peachg
 */
public class Tab {
    /**
     * The paths to find the monitored entities.
     */
    String[] monitoredEntitys;
    /**
     * The locations of the monitored entities.
     */
    final int[] monitoredX;
    final int[] monitoredY;
    /**
     * The name of this tab.
     */
    final String name;
    public static List<Tab> getTabs(){
        List<Tab> tabs=new ArrayList<Tab>();
        try {
            File folder = new File(".").getAbsoluteFile();
            File target = null;
            while (folder != null && folder.exists()) {
                target = new File(folder, "tab-settings.txt");
                if (target.exists() && target.canRead()) {
                    break;
                }
                target = null;
                folder = folder.getParentFile();
            }
            if (target == null) {
                throw new FileNotFoundException("Could not find tab settings.");
            }
            BufferedReader fin = new BufferedReader(new FileReader(target));
            try {
                while (true) {
                    String line = fin.readLine();
                    if (line == null) {
                        break;
                    }
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    tabs.add(Tab.line2Tab(line));
                }
            } finally {
                fin.close();
            }
        } catch (IOException ex) {
            Logger.log(LogLevel.WARNING, "Could not set up tab list!", ex);
        }
        return tabs;
    }
    /**
     * 
     * @param line The string that represents a tab.
     * @return A tab represented by the string.
     */
    public static Tab line2Tab(String line) {
        String[] splitLine = line.split("\1");
        String[] names = splitLine[0].split("\2");
        String[] rawX = splitLine[1].split("\2");
        String[] rawY = splitLine[2].split("\2");
        String name = splitLine[3];
        int[] xs = new int[rawX.length];
        for (int indexX = 0; indexX < xs.length; indexX++) {
            xs[indexX] = Integer.parseInt(rawX[indexX]);
        }
        int[] ys = new int[rawY.length];
        for (int indexY = 0; indexY < xs.length; indexY++) {
            ys[indexY] = Integer.parseInt(rawY[indexY]);
        }
        return new Tab(name, names, xs, ys);
    }
    /**
     * 
     * @return A string that represents this tab.
     */
    @Override
    public String toString() {
        StringBuilder build = new StringBuilder();
        for (String s : monitoredEntitys) {
            build.append(s);
            build.append("\2");
        }
        build.deleteCharAt(build.lastIndexOf("\2"));
        build.append("\1");
        for (int s : monitoredX) {
            build.append(s);
            build.append("\2");
        }
        build.deleteCharAt(build.lastIndexOf("\2"));
        build.append("\1");
        for (int s : monitoredY) {
            build.append(s);
            build.append("\2");
        }
        build.deleteCharAt(build.lastIndexOf("\2"));
        build.append("\1" + name);
        return build.toString();
    }
    
    public Tab(String n, Entity[] rem) {
        name = n;
        String[] names = new String[rem.length];
        int[] xs = new int[rem.length];
        int[] ys = new int[rem.length];
        for (int x = 0; x < rem.length; x++) {
            names[x] = rem[x].toString();
            xs[x] = rem[x].centerX;
            ys[x] = rem[x].centerY;
        }
        monitoredEntitys = names;
        monitoredX = xs;
        monitoredY = ys;
    }

    public Tab(String n, String[] remotes, int[] xs, int[] ys) {
        name = n;
        monitoredEntitys = remotes;
        monitoredX = xs;
        monitoredY = ys;
    }
    /**
     * Enforce the tab.
     * @param ents the entities needed.
     * @param rems the remotes needed.
     */
    public void enforceTab(Map<String,Entity> ents,Map<String,Remote> rems) {
        for(Entity e:ents.values()){
            e.centerX = 0;
            e.centerY = 0;
        }
        for(int index=0;index<monitoredEntitys.length;index++){
            if(!ents.containsKey(monitoredEntitys[index])){
                if(rems.containsKey(monitoredEntitys[index])){
                    Remote rem=rems.get(monitoredEntitys[index]);
                    Entity ent = new Entity(rem, 0, 0);
                    ents.put(rem.path, ent);
                }
                else{
                    Logger.info("Couldn't find path:"+monitoredEntitys[index]);
                }
            }
            ents.get(monitoredEntitys[index]).centerX=monitoredX[index];
            ents.get(monitoredEntitys[index]).centerY=monitoredY[index];
        }
    }
}
