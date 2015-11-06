/*
 * Copyright 2015 Colby Skeggs
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
package ccre.channel;

import static org.junit.Assert.*;

import java.util.NoSuchElementException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ccre.testing.CountingEventOutput;

@SuppressWarnings("javadoc")
public class AbstractUpdatingInputTest {

    private final EventOutput evil = new EventOutput() {
        @Override
        public void event() {
            throw new NoSuchElementException("Attempted to fire an EVIL event.");
        }
    };
    private CountingEventOutput ceo, ceo2, ceo3;

    @Before
    public void setUp() throws Exception {
        ceo = new CountingEventOutput();
        ceo2 = new CountingEventOutput();
        ceo3 = new CountingEventOutput();
    }

    @After
    public void tearDown() throws Exception {
        ceo = ceo2 = ceo3 = null;
        update = null;
    }

    // No tests for update() or the constructor are provided, as they are
    // essentially a copy of the functionality from DerivedUpdate.

    EventOutput update;

    @Test
    public void testOnUpdate() {
        AbstractUpdatingInput dui = new AbstractUpdatingInput() {
            {
                update = () -> {
                    for (int i = 0; i < 10; i++) {
                        ceo.ifExpected = true;
                        ceo3.ifExpected = true;
                        perform();
                        ceo.check();
                        ceo3.check();
                    }
                    ceo2.event();
                };
            }
        };
        dui.onUpdate(ceo);
        dui.onUpdate(ceo3);
        for (int i = 0; i < 10; i++) {
            ceo2.ifExpected = true;
            update.event();
            ceo2.check();
        }
    }

    private boolean enabled, enabled3;

    @Test
    public void testOnUpdateR() {
        AbstractUpdatingInput dui = new AbstractUpdatingInput() {
            {
                update = () -> {
                    for (int i = 0; i < 10; i++) {
                        ceo.ifExpected = enabled;
                        ceo3.ifExpected = enabled3;
                        perform();
                        ceo.check();
                        ceo3.check();
                    }
                    ceo2.event();
                };
            }
        };
        enabled = enabled3 = true;
        EventOutput dereg = dui.onUpdate(ceo);
        EventOutput dereg3 = dui.onUpdate(ceo3);
        for (int i = 0; i < 10; i++) {
            ceo2.ifExpected = true;
            update.event();
            ceo2.check();
        }
        dereg.event();
        enabled = false;
        for (int i = 0; i < 10; i++) {
            ceo2.ifExpected = true;
            update.event();
            ceo2.check();
        }
        dereg3.event();
        enabled3 = false;
        for (int i = 0; i < 10; i++) {
            ceo2.ifExpected = true;
            update.event();
            ceo2.check();
        }
    }

    @Test(expected = NullPointerException.class)
    public void testOnUpdateNull() {
        new AbstractUpdatingInput() {
        }.onUpdate(null);
    }

    @Test(expected = NoSuchElementException.class)
    public void testSimpleError() {
        new AbstractUpdatingInput() {
            {
                update = this::perform;
            }
        }.onUpdate(evil);
        update.event();
    }

    @Test(expected = NoSuchElementException.class)
    public void testDualErrorA() {
        AbstractUpdatingInput dui = new AbstractUpdatingInput() {
            {
                update = this::perform;
            }
        };
        dui.onUpdate(evil);
        dui.onUpdate(ceo);
        ceo.ifExpected = true;
        try {
            update.event();
        } catch (NoSuchElementException e) {
            assertEquals(e.getSuppressed().length, 0);
            ceo.check();
            throw e;
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void testDualErrorB() {
        AbstractUpdatingInput dui = new AbstractUpdatingInput() {
            {
                update = this::perform;
            }
        };
        dui.onUpdate(ceo);
        dui.onUpdate(evil);
        ceo.ifExpected = true;
        try {
            update.event();
            fail();
        } catch (NoSuchElementException e) {
            assertEquals(e.getSuppressed().length, 0);
            ceo.check();
            throw e;
        }
    }

    @Test
    public void testDualErrorBoth() {
        AbstractUpdatingInput dui = new AbstractUpdatingInput() {
            {
                update = this::perform;
            }
        };
        dui.onUpdate(evil);
        dui.onUpdate(evil);
        try {
            update.event();
            fail();
        } catch (NoSuchElementException e) {
            assertEquals(e.getSuppressed().length, 1);
            assertTrue(e.getSuppressed()[0] instanceof NoSuchElementException);
        }
    }

    @Test
    public void testTrualErrorFirst() {
        AbstractUpdatingInput dui = new AbstractUpdatingInput() {
            {
                update = this::perform;
            }
        };
        dui.onUpdate(evil);
        dui.onUpdate(ceo);
        dui.onUpdate(ceo3);
        ceo.ifExpected = ceo3.ifExpected = true;
        try {
            update.event();
            fail();
        } catch (NoSuchElementException e) {
            ceo.check();
            ceo3.check();
            assertEquals(e.getSuppressed().length, 0);
        }
    }

    @Test
    public void testTrualErrorMiddle() {
        AbstractUpdatingInput dui = new AbstractUpdatingInput() {
            {
                update = this::perform;
            }
        };
        dui.onUpdate(ceo);
        dui.onUpdate(evil);
        dui.onUpdate(ceo3);
        ceo.ifExpected = ceo3.ifExpected = true;
        try {
            update.event();
            fail();
        } catch (NoSuchElementException e) {
            ceo.check();
            ceo3.check();
            assertEquals(e.getSuppressed().length, 0);
        }
    }

    @Test
    public void testTrualErrorLast() {
        AbstractUpdatingInput dui = new AbstractUpdatingInput() {
            {
                update = this::perform;
            }
        };
        dui.onUpdate(ceo);
        dui.onUpdate(ceo3);
        dui.onUpdate(evil);
        ceo.ifExpected = ceo3.ifExpected = true;
        try {
            update.event();
            fail();
        } catch (NoSuchElementException e) {
            ceo.check();
            ceo3.check();
            assertEquals(e.getSuppressed().length, 0);
        }
    }

    @Test
    public void testTrualErrorTwice() {
        AbstractUpdatingInput dui = new AbstractUpdatingInput() {
            {
                update = this::perform;
            }
        };
        dui.onUpdate(evil);
        dui.onUpdate(evil);
        dui.onUpdate(ceo);
        ceo.ifExpected = true;
        try {
            update.event();
            fail();
        } catch (NoSuchElementException e) {
            ceo.check();
            assertEquals(e.getSuppressed().length, 1);
            assertTrue(e.getSuppressed()[0] instanceof NoSuchElementException);
        }
    }
}
