/*
 * Copyright 2013-2014 Colby Skeggs
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
package ccre.testing;

import ccre.log.Logger;
import java.util.Random;

/**
 * A system to test the speed of a set of computations.
 *
 * @author skeggsc
 */
public class ComputationBenchmark {

    /**
     * Run the computation benchmark test.
     *
     * @param args
     */
    public static void main(String[] args) {
        Logger.info("31");
        final int magnitude = 1000, reps = 1000;
        // Test copying operations
        float[] rawdata1 = new float[magnitude];
        Logger.info("35");
        float[] rawdata2 = new float[magnitude];
        Logger.info("37");
        float[] rawdata3 = new float[magnitude];
        Logger.info("39");
        Random r = new Random();
        Logger.info("41");
        for (int i = 0; i < magnitude; i++) {
            rawdata1[i] = Float.intBitsToFloat(r.nextInt());
            rawdata3[i] = Float.intBitsToFloat(r.nextInt());
        }
        Logger.info("46!");
        // Just to cause stuff to take more time so that the starting-up delay is mitigated.
        for (int i = 0; i < reps; i++) {
            for (int j = 0; j < magnitude; j++) {
                rawdata2[j] = rawdata1[j];
            }
        }
        Logger.info("53");
        long startAt = System.currentTimeMillis();
        for (int i = 0; i < reps; i++) {
            for (int j = 0; j < magnitude; j++) {
                rawdata2[j] = rawdata1[j];
            }
        }
        long endAt = System.currentTimeMillis();
        Logger.info("Benchmark 1: " + (endAt - startAt) + " ms");
        startAt = System.currentTimeMillis();
        for (int i = 0; i < reps; i++) {
            System.arraycopy(rawdata1, 0, rawdata2, 0, magnitude);
        }
        endAt = System.currentTimeMillis();
        Logger.info("Benchmark 2: " + (endAt - startAt) + " ms");
        startAt = System.currentTimeMillis();
        for (int i = 0; i < reps; i++) {
            for (int j = 0; j < magnitude; j++) {
                rawdata2[j] = rawdata1[j] + 0.7124f;
            }
        }
        endAt = System.currentTimeMillis();
        Logger.info("Benchmark 3: " + (endAt - startAt) + " ms");
        startAt = System.currentTimeMillis();
        for (int i = 0; i < reps; i++) {
            for (int j = 0; j < magnitude; j++) {
                rawdata2[j] = rawdata1[j] + rawdata3[j];
            }
        }
        endAt = System.currentTimeMillis();
        Logger.info("Benchmark 4: " + (endAt - startAt) + " ms");
        startAt = System.currentTimeMillis();
        for (int i = 0; i < reps; i++) {
            float tot = 0.71f;
            for (int j = 0; j < magnitude; j++) {
                tot += rawdata1[j];
                rawdata2[j] = tot;
            }
        }
        endAt = System.currentTimeMillis();
        Logger.info("Benchmark 5: " + (endAt - startAt) + " ms");
        startAt = System.currentTimeMillis();
        for (int i = 0; i < reps; i++) {
            for (int j = 0; j < magnitude; j++) {
                rawdata2[j] = rawdata1[j] * 0.7124f;
            }
        }
        endAt = System.currentTimeMillis();
        Logger.info("Benchmark 6: " + (endAt - startAt) + " ms");
        startAt = System.currentTimeMillis();
        for (int i = 0; i < reps; i++) {
            for (int j = 0; j < magnitude; j++) {
                rawdata2[j] = rawdata1[j] * rawdata3[j];
            }
        }
        endAt = System.currentTimeMillis();
        Logger.info("Benchmark 7: " + (endAt - startAt) + " ms");
        startAt = System.currentTimeMillis();
        for (int i = 0; i < reps; i++) {
            for (int j = 0; j < magnitude; j++) {
                rawdata2[j] = rawdata1[j] / 0.7124f;
            }
        }
        endAt = System.currentTimeMillis();
        Logger.info("Benchmark 8: " + (endAt - startAt) + " ms");
        startAt = System.currentTimeMillis();
        for (int i = 0; i < reps; i++) {
            for (int j = 0; j < magnitude; j++) {
                rawdata2[j] = rawdata1[j] / rawdata3[j];
            }
        }
        endAt = System.currentTimeMillis();
        Logger.info("Benchmark 9: " + (endAt - startAt) + " ms");
        //boolean[][] outs = new boolean[640][480];
        byte[][] chan = new byte[640][480];
        for (int i = 0; i < chan.length; i++) {
            for (int j = 0; j < chan[i].length; j++) {
                chan[i][j] = (byte) r.nextInt(256);
            }
        }
        startAt = System.currentTimeMillis();
        int count = 0;
        for (int i = 0; i < reps / 100; i++) {
            for (int x = 0; x < 640; x++) {
                for (int y = 0; y < 480; y++) {
                    if (chan[x][y] > 43) {
                        count++;
                    }
                }
            }/*
             for (int x=0; x<640; x++) {
             for (int y=0; y<480; y++) {
             if (outs[x][y]) {
             count++;
             }
             }
             }*/

        } // 238 microseconds per operation.
        endAt = System.currentTimeMillis();
        Logger.info("Benchmark 10: " + count + ":: " + (endAt - startAt) + " each");
    }
}

/*
 * From robot:
 * [cRIO] LOG[INFO] Benchmark 1: 812 ms
 * [cRIO] LOG[INFO] Benchmark 2: 72 ms
 * [cRIO] LOG[INFO] Benchmark 3: 1084 ms
 * [cRIO] LOG[INFO] Benchmark 4: 1217 ms
 * [cRIO] LOG[INFO] Benchmark 5: 1158 ms
 * [cRIO] LOG[INFO] Benchmark 6: 1084 ms
 * [cRIO] LOG[INFO] Benchmark 7: 1218 ms
 * [cRIO] LOG[INFO] Benchmark 8: 1136 ms
 * [cRIO] LOG[INFO] Benchmark 9: 1273 ms
 * [cRIO] LOG[INFO] Benchmark 10: 1010090:: 2533 each
 * [cRIO] LOG[INFO] End
 */
