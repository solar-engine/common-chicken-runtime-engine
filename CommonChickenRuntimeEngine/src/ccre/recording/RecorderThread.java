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
package ccre.recording;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

import ccre.concurrency.ReporterThread;

class RecorderThread {
    private static final RecordSnapshot SENTINEL = new RecordSnapshot();

    static {
        SENTINEL.timestamp = Long.MAX_VALUE; // always ordered LAST
    }

    private final ReporterThread thread = new ReporterThread("Recorder") {
        @Override
        protected void threadBody() throws Throwable {
            try {
                while (true) {
                    // flush once per second
                    RecordSnapshot rs = queue.poll(1, TimeUnit.SECONDS);
                    if (rs == null) {
                        enc.flush();
                        rs = queue.take();
                    }
                    if (rs == SENTINEL) {
                        enc.close();
                        queue.clear();
                        return;
                    } else {
                        enc.encode(rs);
                        pool.offer(rs); // and if not, just drop it
                    }
                }
            } finally {
                terminated.countDown();
            }
        }
    };

    private final StreamEncoder enc;

    public RecorderThread(OutputStream output) throws IOException {
        enc = new StreamEncoder(output);
        thread.setDaemon(true);
    }

    private final CountDownLatch terminated = new CountDownLatch(1);
    private final ArrayBlockingQueue<RecordSnapshot> pool = new ArrayBlockingQueue<>(64);
    private final PriorityBlockingQueue<RecordSnapshot> queue = new PriorityBlockingQueue<>();

    private RecordSnapshot getOrAllocateSnapshot() {
        RecordSnapshot rs = pool.poll();
        return rs == null ? new RecordSnapshot() : rs;
    }

    public void close() throws InterruptedException {
        queue.add(SENTINEL);
        terminated.await();
    }

    public void record(long timestamp, int channel, byte type, long value) {
        RecordSnapshot rs = getOrAllocateSnapshot();
        rs.timestamp = timestamp;
        rs.channel = channel;
        rs.type = type;
        rs.value = value;
        rs.data = null;
        queue.add(rs);
    }

    public void record(long timestamp, int channel, byte[] data) {
        RecordSnapshot rs = getOrAllocateSnapshot();
        rs.timestamp = timestamp;
        rs.channel = channel;
        rs.type = RecordSnapshot.T_BYTES;
        rs.data = data;
        queue.add(rs);
    }

    public void start() {
        thread.start();
    }
}
