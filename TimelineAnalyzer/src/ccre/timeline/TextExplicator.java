/*
 * Copyright 2016 Cel Skeggs
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
package ccre.timeline;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFileChooser;

import ccre.recording.Replayer;
import ccre.recording.Replayer.ReplayChannel;
import ccre.recording.Replayer.ReplaySample;

public class TextExplicator {
    public static void main(String[] args) throws IOException {
        JFileChooser chooser = new JFileChooser("../SampleRobot/emulator-logs/");

        int retval = chooser.showOpenDialog(null);
        if (retval == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (FileInputStream fis = new FileInputStream(file)) {
                explicate(fis);
            }
        }
    }

    public static void explicate(InputStream in) throws IOException {
        List<ReplayChannel> decoded = new Replayer(in).decode();
        System.out.println("Explication: (" + decoded.size() + " channels)");
        int i = 0;
        for (ReplayChannel rc : decoded) {
            System.out.println("Channel " + rc.type + " " + (i++) + ": (" + rc.samples.size() + " samples)");
            for (ReplaySample rs : rc.samples) {
                System.out.println("\t" + rs.timestamp + " " + rs.value + " " + Arrays.toString(rs.data) + " " + (rs.data == null ? "null" : new String(rs.data)));
            }
        }
    }
}
