/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package intelligence;

import ccre.log.Logger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author peachg
 */
public class Tab {

    String[] monitoredEntitys;
    int[] monitoredX;
    int[] monitoredY;
    String name;

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

    public void enforceTab(List<Entity> entities) {
        Map<String, Entity> touse = new HashMap<String, Entity>();
        for (Entity e : entities) {
            touse.put(e.toString(), e);
        }
        enforceTab(touse);
    }

    public void enforceTab(Map<String, Entity> entities) {
        for (int x = 0; x < monitoredEntitys.length; x++) {
            Entity mine = entities.get(monitoredEntitys[x]);
            if (mine != null) {
                mine.centerX = monitoredX[x];
                mine.centerY = monitoredY[x];
            } else {
                Logger.warning("Could not find entity:" + monitoredEntitys[x]);
            }
        }
    }
}
