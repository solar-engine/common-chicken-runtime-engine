/*
 * Copyright 2015 Cel Skeggs
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

import static org.junit.Assert.fail;

import org.junit.Test;

import ccre.testing.CountingEventOutput;

@SuppressWarnings("javadoc")
public class DerivedUpdateTest {

    @Test
    public void testDerivedUpdate() {
        CountingEventOutput ceo1 = new CountingEventOutput();
        CountingEventOutput ceo2 = new CountingEventOutput();
        CountingEventOutput target = new CountingEventOutput();
        UpdatingInput vi1 = new UpdatingInput() {
            @Override
            public CancelOutput onUpdate(EventOutput notify) {
                target.ifExpected = true;
                notify.event();
                target.check();
                ceo1.event();
                return () -> fail("Unbind should not have been called.");
            }
        };
        UpdatingInput vi2 = new UpdatingInput() {
            @Override
            public CancelOutput onUpdate(EventOutput notify) {
                target.ifExpected = true;
                notify.event();
                target.check();
                ceo2.event();
                return () -> fail("Unbind should not have been called.");
            }
        };
        ceo1.ifExpected = ceo2.ifExpected = true;
        new DerivedUpdate(vi1, vi2) {
            @Override
            protected void update() {
                target.event();
            }
        };
        ceo1.check();
        ceo2.check();
    }

    @Test(expected = NullPointerException.class)
    public void testDerivedUpdateNull() {
        new DerivedUpdate(EventInput.never, null) {
            @Override
            protected void update() {
            }
        };
    }

    @Test(expected = NullPointerException.class)
    public void testDerivedUpdateNullB() {
        new DerivedUpdate((UpdatingInput) null) {
            @Override
            protected void update() {
            }
        };
    }

    @Test(expected = NullPointerException.class)
    public void testDerivedUpdateNullArray() {
        new DerivedUpdate((UpdatingInput[]) null) {
            @Override
            protected void update() {
            }
        };
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDerivedUpdateEmpty() {
        new DerivedUpdate() {
            @Override
            protected void update() {
            }
        };
    }
}
