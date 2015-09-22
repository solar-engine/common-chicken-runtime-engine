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
package ccre.supercanvas.components;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccre.cluck.Cluck;
import ccre.rconf.RConf;
import ccre.rconf.RConf.Entry;
import ccre.rconf.RConfable;
import ccre.supercanvas.components.channels.RConfComponent;
import ccre.supercanvas.components.pinned.CluckNetworkingComponent;

/**
 * A RConfComponent that allows for some entire-PoultryInspector debugging
 * tools.
 *
 * @author skeggsc
 */
public class TopLevelRConfComponent extends RConfComponent {
    private static Logger logger = LoggerFactory.getLogger(TopLevelRConfComponent.class);

    /**
     * An RConfable that allows inspection of Java objects.
     *
     * @author skeggsc
     */
    public static class InspectionRConfable implements RConfable, Serializable {

        private static final long serialVersionUID = 9039257278527972486L;
        // transient so that we don't carry over anything.
        private transient final Object target;
        private transient final Class<?> as;
        private transient final boolean asList;
        private transient final TopLevelRConfComponent component;

        private InspectionRConfable(TopLevelRConfComponent component, Object target, Class<?> as, boolean possiblyAsList) {
            this.component = component;
            this.target = target;
            this.as = as;
            this.asList = possiblyAsList && (as.isArray() || target instanceof Iterable);
        }

        @SuppressWarnings("unused")
        public Entry[] queryRConf() throws InterruptedException {
            if (target == null) {
                return new Entry[] { RConf.title("Deleted"), RConf.string("cannot save inspections") };
            }
            ArrayList<Entry> out = new ArrayList<Entry>();
            String asStr = target.toString();
            out.add(RConf.title("Inspecting: " + (asStr.length() > 30 ? asStr.substring(0, 27) + "..." : asStr)));
            if (asList) {
                addEntry(out, "as", "iterable/array");
                addEntry(out, "as object", as);
                if (target instanceof Iterable) {
                    int size = 0;
                    for (Object entry : ((Iterable<?>) target)) {
                        size++;
                    }
                    addEntry(out, "size", size);
                    int i = 0;
                    for (Object entry : ((Iterable<?>) target)) {
                        addEntry(out, "[" + i + "]", entry);
                        i++;
                    }
                } else {
                    int len = Array.getLength(target);
                    addEntry(out, "length", len);
                    for (int i = 0; i < len; i++) {
                        addEntry(out, "[" + i + "]", Array.get(target, i));
                    }
                }
            } else {
                addEntry(out, "as", as);
                addEntry(out, "as super", as.getSuperclass());
                addAsClass(out, as);
            }
            return out.toArray(new Entry[out.size()]);
        }

        private void addAsClass(ArrayList<Entry> out, Class<? extends Object> clazz) {
            for (Field f : clazz.getDeclaredFields()) {
                f.setAccessible(true);
                try {
                    addEntry(out, clazz.getName() + "." + f.getName(), f.get(target));
                } catch (IllegalAccessException e) {
                    addEntry(out, clazz.getName() + "." + f.getName() + " failed", e);
                }
            }
        }

        private void addEntry(ArrayList<Entry> out, String key, Object value) {
            String valueStr = value == null ? "null" : value.toString();
            int ml = Math.max(30, 60 - key.length());
            out.add(RConf.button(key + ": " + (valueStr.length() > ml ? valueStr.substring(0, ml - 3) + "..." : valueStr)));
        }

        public boolean signalRConf(int field, byte[] data) throws InterruptedException {
            Object selected = null;
            if (field == 1) {
                selected = target.getClass();
            } else if (field == 2 && as.getSuperclass() != null) {
                component.addInspection(target, asList ? as : as.getSuperclass(), false);
                return true;
            } else if (field >= 3) {
                if (asList) {
                    if (field >= 4) {
                        if (target instanceof Iterable) {
                            int remaining = field - 4;
                            for (Object o : (Iterable<?>) target) {
                                if (remaining == 0) {
                                    selected = o;
                                    break;
                                } else {
                                    remaining--;
                                }
                            }
                        } else {
                            selected = Array.get(target, field - 4);
                        }
                    }
                } else {
                    selected = readAsClass(field - 3);
                }
            }
            if (selected != null) {
                component.addInspection(selected);
                return true;
            }
            return false;
        }

        private Object readAsClass(int field) {
            Field[] fields = as.getDeclaredFields();
            if (field < fields.length) {
                Field f = fields[field];
                try {
                    f.setAccessible(true);
                    return f.get(target);
                } catch (IllegalAccessException e) {
                    logger.warn("Could not access field " + f, e);
                }
            }
            return null;
        }
    }

    private static class TopLevelRConfable implements RConfable, Serializable {

        private static final long serialVersionUID = 7564831796084759163L;
        private TopLevelRConfComponent master;

        public Entry[] queryRConf() throws InterruptedException {
            return new Entry[] { RConf.title("Local Configuration"), RConf.button("Send Notify"), RConf.button("Inspect Root"), RConf.string("Use Deep Logging:"), RConf.fieldBoolean(CluckNetworkingComponent.useLoggingConnection) };
        }

        public boolean signalRConf(int field, byte[] data) throws InterruptedException {
            if (field == 1) {
                logger.debug("Notifying...");
                Cluck.getNode().notifyNetworkModified();
                logger.debug("Notified!");
                return true;
            } else if (field == 2) {
                master.addInspection(master.getPanel());
                return true;
            } else if (field == 4 && data.length > 0) {
                CluckNetworkingComponent.useLoggingConnection = data[0] != 0;
                return true;
            }
            return false;
        }
    }

    private void addInspection(Object obj, Class<?> as, boolean possiblyAsList) {
        getPanel().add(new RConfComponent(getDragRelX(0), getDragRelY(0),
                "0x" + Integer.toHexString(System.identityHashCode(obj)), new InspectionRConfable(this, obj, as, possiblyAsList)));
    }

    private void addInspection(Object obj, Class<?> as) {
        addInspection(obj, as, true);
    }

    /**
     * Add an inspection pane for the given object to the SuperCanvasPanel.
     *
     * @param obj the object to inspect.
     */
    public void addInspection(Object obj) {
        addInspection(obj, obj.getClass());
    }

    /**
     * Create a new TopLevelRConfComponent at the specified location.
     *
     * @param cx the X coordinate.
     * @param cy the Y coordinate.
     */
    public TopLevelRConfComponent(int cx, int cy) {
        super(cx, cy, "Local Configuration", new TopLevelRConfable());
        ((TopLevelRConfable) device).master = this;
    }

    private static final long serialVersionUID = 3408977443946166053L;
}
