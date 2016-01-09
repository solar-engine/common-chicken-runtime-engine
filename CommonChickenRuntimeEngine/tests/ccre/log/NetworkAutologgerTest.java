/*
 * Copyright 2016 Colby Skeggs
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
package ccre.log;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ccre.cluck.CluckConstants;
import ccre.cluck.CluckNode;
import ccre.cluck.CluckPublisher;
import ccre.util.Utils;
import ccre.util.Values;

@SuppressWarnings("javadoc")
public class NetworkAutologgerTest {

    private NetworkAutologger logger;
    private CluckNode node;
    private VerifyingLoggingTarget vlt, vlt2;

    @Before
    public void setUp() throws Exception {
        this.node = new CluckNode();
        this.logger = new NetworkAutologger(this.node);
        this.vlt = new VerifyingLoggingTarget();
        this.vlt2 = new VerifyingLoggingTarget();
        VerifyingLogger.begin();
    }

    @After
    public void tearDown() throws Exception {
        VerifyingLogger.checkAndEnd();
        this.node = null;
        this.logger = null;
        this.vlt = null;
        this.vlt2 = null;
    }

    @Test
    public void testGetLocalPath() {
        assertTrue(node.hasLink(logger.getLocalPath()));
    }

    @Test
    public void testLogSimple() {
        CluckPublisher.publish(node, "auto-example", vlt);
        VerifyingLogger.configure(LogLevel.CONFIG, "[LOCAL] Loaded logger: auto-example");
        logger.handle("auto-example", CluckConstants.RMT_LOGTARGET);
        VerifyingLogger.check();
        for (LogLevel level : LogLevel.allLevels) {
            for (String s : Values.getRandomStrings(10)) {
                // all of these are configureString because it's all encoded as
                // strings over the network
                vlt.configureString(level, s, null);
                logger.log(level, s, (Throwable) null);
                vlt.check();

                vlt.configureString(level, s, null);
                logger.log(level, s, (String) null);
                vlt.check();

                vlt.configureString(level, s, null);
                logger.log(level, s, "");
                vlt.check();

                String s2 = Values.getRandomString();
                vlt.configureString(level, s, s2.isEmpty() ? null : s2);
                logger.log(level, s, s2);
                vlt.check();

                Exception exc = new Exception();
                vlt.configureString(level, s, Utils.toStringThrowable(exc));
                logger.log(level, s, exc);
                vlt.check();

                // drop [LOCAL]
                logger.log(level, "[LOCAL] " + s, (Throwable) null);
                vlt.check();

                logger.log(level, "[LOCAL] " + s, (String) null);
                vlt.check();

                // drop [NET]
                logger.log(level, "[NET] " + s, (Throwable) null);
                vlt.check();

                logger.log(level, "[NET] " + s, (String) null);
                vlt.check();
            }
        }
    }

    @Test
    public void testStartBefore() {
        logger.start();
        CluckPublisher.publish(node, "auto-example", vlt);
        CluckPublisher.publish(node, "example3", vlt2);
        VerifyingLogger.configure(LogLevel.FINE, "[LOCAL] Rechecking logging...");
        VerifyingLogger.get().onNext = () -> {
            VerifyingLogger.check();
            VerifyingLogger.configure(LogLevel.CONFIG, "[LOCAL] Loaded logger: auto-example");
        };
        node.notifyNetworkModified();
        VerifyingLogger.check();
        vlt.configureString(LogLevel.SEVERE, "some text", null);
        logger.log(LogLevel.SEVERE, "some text", (String) null);
        vlt.check();
        vlt.configureString(LogLevel.SEVERE, "some text", null);
        logger.log(LogLevel.SEVERE, "some text", (Throwable) null);
        vlt.check();
        vlt2.check(); // for lingering failures
    }

    @Test
    public void testStartAfter() {
        CluckPublisher.publish(node, "auto-example", vlt);
        CluckPublisher.publish(node, "example3", vlt2);
        VerifyingLogger.configure(LogLevel.CONFIG, "[LOCAL] Loaded logger: auto-example");
        logger.start();
        VerifyingLogger.check();
        vlt.configureString(LogLevel.SEVERE, "some text", null);
        logger.log(LogLevel.SEVERE, "some text", (String) null);
        vlt.check();
        vlt.configureString(LogLevel.SEVERE, "some text", null);
        logger.log(LogLevel.SEVERE, "some text", (Throwable) null);
        vlt.check();
        vlt2.check(); // for lingering failures
    }

    @Test
    public void testHandle() {
        CluckPublisher.publish(node, "auto-example", vlt);
        CluckPublisher.publish(node, "auto-example2", vlt2);
        CluckPublisher.publish(node, "example3", vlt2);
        VerifyingLogger.configure(LogLevel.CONFIG, "[LOCAL] Loaded logger: auto-example");
        logger.handle("auto-example", CluckConstants.RMT_LOGTARGET);
        VerifyingLogger.check();
        logger.handle("auto-example2", CluckConstants.RMT_BOOLINPUT);
        logger.handle("example3", CluckConstants.RMT_LOGTARGET);
        logger.handle(logger.getLocalPath(), CluckConstants.RMT_LOGTARGET);
        logger.handle("auto-example", CluckConstants.RMT_LOGTARGET);
        vlt.configureString(LogLevel.SEVERE, "some text", null);
        logger.log(LogLevel.SEVERE, "some text", (String) null);
        vlt.check();
        vlt.configureString(LogLevel.SEVERE, "some text", null);
        logger.log(LogLevel.SEVERE, "some text", (Throwable) null);
        vlt.check();
        vlt2.check(); // for lingering failures
    }

    @Test(expected = NullPointerException.class)
    public void testLogThrowableNullA() {
        logger.log(null, "message", new Exception());
    }

    @Test(expected = NullPointerException.class)
    public void testLogStringNullA() {
        logger.log(null, "message", "hello");
    }

    @Test(expected = NullPointerException.class)
    public void testLogThrowableNullB() {
        logger.log(LogLevel.FINEST, null, new Exception());
    }

    @Test(expected = NullPointerException.class)
    public void testLogStringNullB() {
        logger.log(LogLevel.FINEST, null, "hello");
    }
}
