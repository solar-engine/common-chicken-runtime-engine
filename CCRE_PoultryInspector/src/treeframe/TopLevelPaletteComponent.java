/*
 * Copyright 2014 (YOUR NAME HERE).
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
package treeframe;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class TopLevelPaletteComponent extends PaletteComponent {

    private static final ArrayList<PaletteComponent.PaletteEntry> topLevel = new ArrayList<PaletteComponent.PaletteEntry>();

    static {
        topLevel.add(new AllocationPaletteEntry(LoggingComponent.class));
        topLevel.add(new AllocationPaletteEntry(PhidgetMonitorComponent.class));
        topLevel.add(new AllocationPaletteEntry(NetworkPaletteComponent.class));
        topLevel.add(new AllocationPaletteEntry(ListPaletteComponent.class));
        topLevel.add(new AllocationPaletteEntry(FolderComponent.class));
        topLevel.add(new AllocationPaletteEntry(TrashComponent.class));
    }

    public TopLevelPaletteComponent(int x, int y) {
        super(x, y, topLevel);
    }

    private static class AllocationPaletteEntry implements PaletteEntry {

        private final Class<? extends SuperCanvasComponent> aClass;

        public AllocationPaletteEntry(Class<? extends SuperCanvasComponent> aClass) {
            this.aClass = aClass;
        }

        @Override
        public String getName() {
            return aClass.getSimpleName();
        }

        @Override
        public SuperCanvasComponent fetch(int x, int y) {
            try {
                return aClass.getConstructor(Integer.TYPE, Integer.TYPE).newInstance(x, y);
            } catch (NoSuchMethodException ex) {
                throw new RuntimeException(ex);
            } catch (InstantiationException ex) {
                throw new RuntimeException(ex);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            } catch (InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

}
