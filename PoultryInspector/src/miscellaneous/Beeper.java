/*
 * Copyright 2013-2015 Colby Skeggs
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
package miscellaneous;

import java.util.HashMap;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * A system of procedurally generating sounds, and some presets thereof. This
 * isn't actually used anywhere, but it's in the library in case it will be
 * useful. If it's useful to you, please figure out how to merge it nicely into
 * the library.
 *
 * @author skeggsc
 */
public class Beeper {

    private static final HashMap<String, BeepType> lookups = new HashMap<String, BeepType>(16);
    private static BeepType current = null;
    private static final Object cursync = new Object();

    static {
        for (BeepType bt : BeepType.values()) {
            lookups.put(bt.name().toUpperCase(), bt);
        }
    }

    static {
        Thread t = new Thread("beeper") {
            @Override
            public void run() {
                while (true) {
                    BeepType bt;
                    synchronized (cursync) {
                        while (current == null) {
                            try {
                                cursync.wait();
                            } catch (InterruptedException ex) {
                            }
                        }
                        bt = current;
                        current = null;
                    }
                    beep(bt);
                }
            }
        };
        t.setDaemon(true);
        t.start();
    }

    /**
     * Test one of the Beep Types: ALARM, ANNOY, LOW, FASTALARM, SIREN, OOPS,
     * AIMED, CORRAL.
     *
     * The type should be passed as an argument.
     *
     * @param args contains the beep type to play.
     * @throws InterruptedException if the main thread gets interrupted while
     * waiting for the effect to complete.
     */
    public static void main(String[] args) throws InterruptedException {
        beep(args.length == 0 ? "corral" : args[0]); //ALARM, ANNOY, LOW, FASTALARM, SIREN, OOPS, AIMED, CORRAL;
        Thread.sleep(5000);
    }

    /**
     * Start playing the named beep type.
     *
     * Possible types: ALARM, ANNOY, LOW, FASTALARM, SIREN, OOPS, AIMED, CORRAL.
     *
     * If an invalid type is specified, play OOPS.
     *
     * @param name the type of beep to play.
     */
    public static void beep(String name) {
        BeepType bt = lookups.get(name.toUpperCase());
        if (bt == null) {
            bt = BeepType.OOPS;
        }
        synchronized (cursync) {
            current = bt;
            cursync.notifyAll();
        }
    }

    /**
     * Start playing the specified beep type.
     *
     * @param bt the beep type to play.
     * @see BeepType
     */
    public static void beep(BeepType bt) {
        System.out.println("Beeping: " + bt);
        try {
            AudioFormat format = new AudioFormat(bt.rate(), 8, 1, false, true);
            SourceDataLine line = AudioSystem.getSourceDataLine(format);
            try {
                line.open(format);
                line.start();
                long time = 0;
                while (true) {
                    byte[] more = new byte[1024];
                    int i = 0;
                    try {
                        for (; i < more.length; i++) {
                            more[i] = bt.generateOne(time++);
                        }
                    } catch (CompletedException ex) {
                        line.write(more, 0, i);
                        break;
                    }
                    line.write(more, 0, i);
                }
                line.drain();
            } finally {
                line.close();
            }
        } catch (LineUnavailableException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * The types of beeps that this generator can generate.
     *
     * @author skeggsc
     */
    public static enum BeepType {

        /**
         * Repetitive warning beeps.
         */
        ALARM,
        /**
         * A repetitive annoying sound.
         */
        ANNOY,
        /**
         * A sequence of ascending tones.
         */
        LOW,
        /**
         * A repeated fast alarm noise.
         */
        FASTALARM,
        /**
         * An approximate siren noise.
         */
        SIREN,
        /**
         * A high-pitched beep.
         */
        OOPS,
        /**
         * Two short beeps, like a lock-on effect.
         */
        AIMED,
        /**
         * A single beep, like a notification.
         */
        CORRAL;

        private byte generateOne(long l) throws CompletedException {
            long max;
            switch (this) {
            case SIREN:
            case FASTALARM:
            case ALARM:
            case LOW:
            case ANNOY:
                max = 32000;
                break;
            case CORRAL:
                max = 1600;
                break;
            case OOPS:
                max = 4000;
                break;
            case AIMED:
                max = 3000;
                break;
            default:
                throw new IllegalStateException();
            }
            if (l > max) {
                throw new CompletedException();
            }
            switch (this) {
            case AIMED:
                return (byte) ((l * ((l % 1500 < 600) ? 480 : 0)) & 0xFF);
            case OOPS:
                return (byte) (l * 64);
            case SIREN:
                return (byte) ((l % 6400) * (12 + (l / 800.0) % 8));
            case FASTALARM:
                return (byte) ((l % 800 >= 400) ? 0 : l * 66);
            case CORRAL:
                return (byte) ((l % 1600 >= 800) ? 0 : l * 57);
            case ALARM:
                return (byte) ((l >= 800) ? 0 : l * 57);
            case LOW:
                return (byte) ((l % 2400 >= 1200) ? 0 : l * (6 + ((l / 2400) % 5)));
            case ANNOY:
                return (byte) ((l % 400 >= 200) ? 0 : (l % 1200 >= 800) ? l << 7 : (l % 1200 >= 400) ? l << 4 : l << 3);
            default:
                throw new IllegalStateException();
            }
        }

        private float rate() {
            return this == AIMED ? 16000f : 8000f;
        }
    }

    @SuppressWarnings(value = "serial")
    private static final class CompletedException extends Exception {
    }
}
