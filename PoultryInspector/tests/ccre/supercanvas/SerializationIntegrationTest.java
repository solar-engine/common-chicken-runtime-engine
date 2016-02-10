/*
 * Copyright 2015-2016 Cel Skeggs.
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
package ccre.supercanvas;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.Test;

import ccre.channel.BooleanCell;
import ccre.channel.EventCell;
import ccre.channel.FloatCell;
import ccre.cluck.Cluck;
import ccre.cluck.rpc.SimpleProcedure;
import ccre.log.LogLevel;
import ccre.log.LoggingTarget;
import ccre.rconf.RConfable;
import ccre.rconf.RConf.Entry;
import ccre.supercanvas.components.*;
import ccre.supercanvas.components.palette.*;
import ccre.supercanvas.components.pinned.*;
import ccre.util.LineCollectorOutputStream;

@SuppressWarnings("javadoc")
public class SerializationIntegrationTest {

    @Test
    public void test() throws IOException, ClassNotFoundException {
        // Not creating anything visible on the network.
        Cluck.publish("test-boolean", new BooleanCell());
        Cluck.publish("test-float", new FloatCell());
        Cluck.publish("test-event", new EventCell());
        Cluck.publish("test-logging", new LoggingTarget() {
            @Override
            public void log(LogLevel level, String message, Throwable throwable) {
            }

            @Override
            public void log(LogLevel level, String message, String extended) {
            }
        });
        Cluck.publish("test-output", new LineCollectorOutputStream() {
            @Override
            protected void collect(String param) {
            }
        });
        Cluck.publishRConf("test-rconf", new RConfable() {
            @Override
            public boolean signalRConf(int field, byte[] data) throws InterruptedException {
                return false;
            }

            @Override
            public Entry[] queryRConf() throws InterruptedException {
                return new Entry[0];
            }
        });
        Cluck.getNode().getRPCManager().publish("test-rpc", new SimpleProcedure() {
            @Override
            protected byte[] invoke(byte[] in) {
                return new byte[] { '\n' };
            }
        });
        SuperCanvasPanel canvas = new SuperCanvasPanel();
        canvas.add(new LoggingComponent(312, 300));
        canvas.add(new CluckNetworkingComponent(CluckNetworkingComponent.DO_NOT_CONNECT));
        canvas.add(new EditModeComponent());
        canvas.add(new StartComponent());
        canvas.add(new SaveLoadComponent(0, 0));
        canvas.add(new FolderComponent(30, 30));
        canvas.add(new TextComponent(100, 200, "Some Text"));
        canvas.add(new TopLevelRConfComponent(50, 50));
        canvas.add(new TrashComponent(200, 200));
        canvas.add(new ListPaletteComponent(400, 400));
        NetworkPaletteComponent npc;
        canvas.add(npc = new NetworkPaletteComponent(300, 400));
        canvas.add(new TopLevelPaletteComponent(400, 300));
        int x = 0;
        for (String name : new String[] { "test-boolean", "test-float", "test-event", "test-boolean.input", "test-float.input", "test-event.input", "test-boolean.output", "test-float.output", "test-event.output", "test-logging", "test-output", "test-rconf", "test-rpc" }) {
            canvas.add(npc.getComponentFor(x, 300, name));
            x += 50;
        }
        File f = File.createTempFile("pstest-", ".ser");
        f.deleteOnExit();
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f))) {
            canvas.save(out);
        }
        canvas = new SuperCanvasPanel();
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(f))) {
            canvas.load(in);
        }
    }
}
