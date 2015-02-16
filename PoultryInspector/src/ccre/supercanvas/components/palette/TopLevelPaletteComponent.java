/*
 * Copyright 2014-2015 Colby Skeggs.
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
package ccre.supercanvas.components.palette;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;

import ccre.supercanvas.SuperCanvasComponent;
import ccre.supercanvas.components.CanvasPhidgetComponent;
import ccre.supercanvas.components.CluckUnitTestComponent;
import ccre.supercanvas.components.FolderComponent;
import ccre.supercanvas.components.LoggingComponent;
import ccre.supercanvas.components.PhysicalPhidgetComponent;
import ccre.supercanvas.components.TextComponent;
import ccre.supercanvas.components.TopLevelRConfComponent;
import ccre.supercanvas.components.TrashComponent;

/**
 * A top-level palette, which means that it contains important components that
 * the user might want to always be able to instantiate regardless of context.
 *
 * @author skeggsc
 */
public class TopLevelPaletteComponent extends PaletteComponent<Iterable<PaletteEntry>> {

    private static final long serialVersionUID = -1847428594657030363L;
    private static final Iterable<PaletteEntry> topLevel;

    static {
        ArrayList<PaletteEntry> local = new ArrayList<PaletteEntry>();
        local.add(new AllocationPaletteEntry(LoggingComponent.class));
        local.add(new AllocationPaletteEntry(CanvasPhidgetComponent.class));
        local.add(new AllocationPaletteEntry(PhysicalPhidgetComponent.class));
        local.add(new AllocationPaletteEntry(NetworkPaletteComponent.class));
        local.add(new AllocationPaletteEntry(ListPaletteComponent.class));
        local.add(new AllocationPaletteEntry(FolderComponent.class));
        local.add(new AllocationPaletteEntry(TrashComponent.class));
        local.add(new AllocationPaletteEntry(TextComponent.class));
        local.add(new AllocationPaletteEntry(CluckUnitTestComponent.class));
        local.add(new AllocationPaletteEntry(TopLevelRConfComponent.class));
        topLevel = Collections.unmodifiableCollection(local);
    }

    /**
     * Create a new TopLevelPaletteComponent.
     *
     * @param x the X-coordinate.
     * @param y the Y-coordinate.
     */
    public TopLevelPaletteComponent(int x, int y) {
        super(x, y, topLevel);
    }

    private static class AllocationPaletteEntry implements PaletteEntry {

        private static final long serialVersionUID = 1218570206587433276L;
        final Class<? extends SuperCanvasComponent> aClass;

        AllocationPaletteEntry(Class<? extends SuperCanvasComponent> aClass) {
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
