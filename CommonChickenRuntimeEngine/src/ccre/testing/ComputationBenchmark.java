/*
 * Copyright 2013 Colby Skeggs
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
 * A system to test the speed of various computations.
 *
 * @author skeggsc
 */
public class ComputationBenchmark {
    public static void main(String[] args) {
        final int magnitude = 1000, reps = 1000000;
        // Test copying operations
        float[] rawdata1 = new float[magnitude];
        float[] rawdata2 = new float[magnitude];
        float[] rawdata3 = new float[magnitude];
        Random r = new Random();
        for (int i=0; i<magnitude; i++) {
            rawdata1[i] = Float.intBitsToFloat(r.nextInt());
            rawdata3[i] = Float.intBitsToFloat(r.nextInt());
        }
        // Just to cause stuff to take more time so that the starting-up delay is mitigated.
        for (int i=0; i<reps; i++) {
            for (int j=0; j<magnitude; j++) {
                rawdata2[j] = rawdata1[j];
            }
        }
        long startAt = System.currentTimeMillis();
        for (int i=0; i<reps; i++) {
            for (int j=0; j<magnitude; j++) {
                rawdata2[j] = rawdata1[j];
            }
        }
        long endAt = System.currentTimeMillis();
        Logger.info("Benchmark 1: " + (endAt - startAt) + " ms");
        startAt = System.currentTimeMillis();
        for (int i=0; i<reps; i++) {
            System.arraycopy(rawdata1, 0, rawdata2, 0, magnitude);
        }
        endAt = System.currentTimeMillis();
        Logger.info("Benchmark 2: " + (endAt - startAt) + " ms");
        startAt = System.currentTimeMillis();
        for (int i=0; i<reps; i++) {
            for (int j=0; j<magnitude; j++) {
                rawdata2[j] = rawdata1[j] + 0.7124f;
            }
        }
        endAt = System.currentTimeMillis();
        Logger.info("Benchmark 3: " + (endAt - startAt) + " ms");
        startAt = System.currentTimeMillis();
        for (int i=0; i<reps; i++) {
            for (int j=0; j<magnitude; j++) {
                rawdata2[j] = rawdata1[j] + rawdata3[j];
            }
        }
        endAt = System.currentTimeMillis();
        Logger.info("Benchmark 4: " + (endAt - startAt) + " ms");
        /*startAt = System.currentTimeMillis();
        for (int i=0; i<reps; i++) {
            float tot = 0.71f;
            for (int j=0; j<magnitude; j++) {
                tot += rawdata1[j];
                rawdata2[j] = tot;
            }
        }
        endAt = System.currentTimeMillis();
        Logger.info("Benchmark 5: " + (endAt - startAt) + " ms");
        startAt = System.currentTimeMillis();
        for (int i=0; i<reps; i++) {
            for (int j=0; j<magnitude; j++) {
                rawdata2[j] = rawdata1[j] * 0.7124f;
            }
        }
        endAt = System.currentTimeMillis();
        Logger.info("Benchmark 6: " + (endAt - startAt) + " ms");
        startAt = System.currentTimeMillis();
        for (int i=0; i<reps; i++) {
            for (int j=0; j<magnitude; j++) {
                rawdata2[j] = rawdata1[j] * rawdata3[j];
            }
        }
        endAt = System.currentTimeMillis();
        Logger.info("Benchmark 7: " + (endAt - startAt) + " ms");
        startAt = System.currentTimeMillis();
        for (int i=0; i<reps; i++) {
            for (int j=0; j<magnitude; j++) {
                rawdata2[j] = rawdata1[j] / 0.7124f;
            }
        }
        endAt = System.currentTimeMillis();
        Logger.info("Benchmark 8: " + (endAt - startAt) + " ms");
        startAt = System.currentTimeMillis();
        for (int i=0; i<reps; i++) {
            for (int j=0; j<magnitude; j++) {
                rawdata2[j] = rawdata1[j] / rawdata3[j];
            }
        }
        endAt = System.currentTimeMillis();
        Logger.info("Benchmark 9: " + (endAt - startAt) + " ms");*/
        boolean[][] outs = new boolean[640][480];
        byte[][] chan = new byte[640][480];
        for (int i=0; i<chan.length; i++) {
            r.nextBytes(chan[i]);
        }
        startAt = System.nanoTime();
        int count = 0;
        for (int i=0; i<10000; i++) {
            for (int x=0; x<640; x++) {
                for (int y=0; y<480; y++) {
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
        endAt = System.nanoTime();
        Logger.info("Benchmark 10: " + (endAt - startAt) + " ns");
    }
}
