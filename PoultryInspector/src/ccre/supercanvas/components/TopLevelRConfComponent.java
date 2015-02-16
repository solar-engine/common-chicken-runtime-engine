package ccre.supercanvas.components;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import ccre.cluck.Cluck;
import ccre.log.Logger;
import ccre.rconf.RConf;
import ccre.rconf.RConf.Entry;
import ccre.rconf.RConfable;
import ccre.supercanvas.components.channels.RConfComponent;
import ccre.util.CList;

public class TopLevelRConfComponent extends RConfComponent {

    public static class InspectionRConfable implements RConfable {

        // transient so that we don't carry over anything.
        private transient final Object target;
        private transient final Class<?> as;
        private transient final boolean asList;
        private transient final TopLevelRConfComponent component;

        public InspectionRConfable(TopLevelRConfComponent component, Object target, Class<?> as, boolean possiblyAsList) {
            this.component = component;
            this.target = target;
            this.as = as;
            this.asList = possiblyAsList && (as.isArray() || target instanceof Iterable);
        }

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
                    for (int i=0; i<len; i++) {
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
                addInspection(component, target, asList ? as : as.getSuperclass(), false);
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
                addInspection(component, selected);
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
                    Logger.warning("Could not access field " + f, e);
                }
            }
            return null;
        }
    }

    private static class TopLevelRConfable implements RConfable {

        private TopLevelRConfComponent master;

        public Entry[] queryRConf() throws InterruptedException {
            return new Entry[] { RConf.title("Local Configuration"), RConf.button("Send Notify"), RConf.button("Inspect Root") };
        }

        public boolean signalRConf(int field, byte[] data) throws InterruptedException {
            if (field == 1) {
                Logger.fine("Notifying...");
                Cluck.getNode().notifyNetworkModified();
                Logger.fine("Notified!");
            } else if (field == 2) {
                addInspection(master, master.getPanel());
            }
            return false;
        }
    }
    
    public static void addInspection(TopLevelRConfComponent component, Object obj, Class<?> as, boolean possiblyAsList) {
        component.getPanel().add(new RConfComponent(component.getDragRelX(0), component.getDragRelY(0),
                "0x" + Integer.toHexString(System.identityHashCode(obj)), new InspectionRConfable(component, obj, as, possiblyAsList)));
    }

    public static void addInspection(TopLevelRConfComponent component, Object obj, Class<?> as) {
        addInspection(component, obj, as, true);
    }

    public static void addInspection(TopLevelRConfComponent component, Object obj) {
        addInspection(component, obj, obj.getClass());
    }

    public TopLevelRConfComponent(int cx, int cy) {
        super(cx, cy, "Local Configuration", new TopLevelRConfable());
        ((TopLevelRConfable) device).master = this;
    }

    private static final long serialVersionUID = 3408977443946166053L;

}
