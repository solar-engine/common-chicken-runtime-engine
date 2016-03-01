/*
 * Copyright 2013-2016 Cel Skeggs
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
package ccre.poultryinspector;

import java.io.Serializable;
import java.util.function.Supplier;

import ccre.log.FileLogger;
import ccre.log.NetworkAutologger;
import ccre.net.TrafficCounting;
import ccre.supercanvas.SuperCanvasComponent;
import ccre.supercanvas.SuperCanvasFrame;
import ccre.supercanvas.components.FolderComponent;
import ccre.supercanvas.components.LoggingComponent;
import ccre.supercanvas.components.TextComponent;
import ccre.supercanvas.components.TopLevelRConfComponent;
import ccre.supercanvas.components.TrashComponent;
import ccre.supercanvas.components.palette.ListPaletteComponent;
import ccre.supercanvas.components.palette.NetworkPaletteComponent;
import ccre.supercanvas.components.palette.TopLevelPaletteComponent;
import ccre.supercanvas.components.pinned.CluckNetworkingComponent;
import ccre.supercanvas.components.pinned.EditModeComponent;
import ccre.supercanvas.components.pinned.SaveLoadComponent;
import ccre.supercanvas.components.pinned.StartComponent;
import ccre.viewer.HighlightComponent;
import ccre.viewer.WebcamComponent;

/**
 * The launcher for the Poultry Inspector.
 *
 * @author skeggsc
 */
public class PoultryInspector {

    /**
     * The main method of the Poultry Inspector.
     *
     * @param args the unused program arguments.
     */
    public static void main(String args[]) {
        TrafficCounting.setCountingEnabled(true);
        NetworkAutologger.register();
        FileLogger.register();

        Supplier<SuperCanvasComponent> popup = (Supplier<SuperCanvasComponent> & Serializable) () -> {
            return new TopLevelPaletteComponent(200, 200, WebcamComponent.class, HighlightComponent.class, LoggingComponent.class, NetworkPaletteComponent.class, ListPaletteComponent.class, FolderComponent.class, TrashComponent.class, TextComponent.class, TopLevelRConfComponent.class);
        };

        Supplier<SuperCanvasComponent> popupNet = (Supplier<SuperCanvasComponent> & Serializable) () -> {
            return new NetworkPaletteComponent(200, 200);
        };

        SuperCanvasComponent[] components = new SuperCanvasComponent[] {

                new LoggingComponent(312, 300), new CluckNetworkingComponent(),

                new EditModeComponent(), new StartComponent(popup, "PALETTE", 0), new StartComponent(popupNet, "NETWORK", 1),

                new SaveLoadComponent(0, 0)

        };

        new SuperCanvasFrame("Poultry Inspector", popup, components).start();
    }
}
