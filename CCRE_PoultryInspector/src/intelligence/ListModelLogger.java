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
package intelligence;

import ccre.log.LogLevel;
import ccre.log.Logger;
import ccre.log.LoggingTarget;
import ccre.util.CArrayList;
import ccre.util.CList;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * A logging target that adds what it receives to a list in the GUI. It supports
 * collapsing of exceptions so that you don't need to see the entire traceback
 * all the time.
 *
 * @author skeggsc
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ListModelLogger implements LoggingTarget, ListSelectionListener {

    private static Method getSuppressed;

    static {
        try {
            // Print suppressed exceptions, if any
            getSuppressed = Class.forName("java.lang.Throwable").getMethod("getSuppressed");
        } catch (ClassNotFoundException ex) {
            Logger.warning("Could not find Throwable!", ex);
        } catch (NoSuchMethodException ex) {
            // Do nothing.
        } catch (SecurityException ex) {
            Logger.warning("Could not init getSuppressed", ex);
        }
    }
    /**
     * The list model to update.
     */
    public final DefaultListModel model;
    /**
     * The list that has the model.
     */
    public final JList lstErrors;
    /**
     * The last known index of the selection.
     */
    protected int lastIndex = -1;

    /**
     * Create a new ListModelLogger from a specified model and JList.
     *
     * @param errorListing the model to store data in.
     * @param lstErrors the JList to determine what the selection is.
     */
    public ListModelLogger(DefaultListModel errorListing, JList lstErrors) {
        model = errorListing;
        this.lstErrors = lstErrors;
        lstErrors.addListSelectionListener(this);
    }

    /**
     * Add the given element to the model, using the AWT EventQueue.
     *
     * @param elem the element to add.
     */
    protected void add(final Element elem) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                model.addElement(elem);
            }
        });
    }

    @Override
    public void log(LogLevel level, String msg, Throwable thr) {
        add(new Element(level, msg, thr));
    }

    @Override
    public void log(LogLevel level, String msg, String extended) {
        add(new Element(level, msg, extended));
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (lastIndex == lstErrors.getSelectedIndex()) {
            return;
        }
        lastIndex = lstErrors.getSelectedIndex();
        if (lastIndex == -1) {
            return;
        }
        Object o = model.get(lastIndex);
        if (o instanceof Element) {
            for (int i = 0; i < model.size(); i++) {
                if (model.get(i) instanceof Element) {
                    continue;
                }
                model.remove(i--);
            }
            int index = model.indexOf(o);
            Throwable thr = ((Element) o).thr;
            String body = ((Element) o).body;
            if (thr != null) {
                CList<String> data = computeDisplay(thr);
                for (String s : data) {
                    model.add(++index, s);
                }
            }
            if (body != null && !body.isEmpty()) {
                for (String line : body.split("\n")) {
                    model.add(++index, line);
                }
            }
        }
    }

    /**
     * Compute the display of the specified throwable manually so that it can
     * directly go into the output.
     *
     * @param thr the throwable to describe.
     * @return the list of description lines.
     */
    private CList<String> computeDisplay(Throwable thr) { // Taken from builtin implementation in OpenJDK.
        CArrayList<String> out = new CArrayList<String>();
        // Guard against malicious overrides of Throwable.equals by
        // using a Set with identity equality semantics.
        Set<Throwable> dejaVu = Collections.newSetFromMap(new IdentityHashMap<Throwable, Boolean>());
        dejaVu.add(thr);

        // Print our stack trace
        StackTraceElement[] trace = thr.getStackTrace();
        for (StackTraceElement traceElement : trace) {
            out.add("    at " + traceElement);
        }

        Throwable[] thrs = new Throwable[0];
        if (getSuppressed != null) {
            try {
                thrs = (Throwable[]) getSuppressed.invoke(thr);
            } catch (IllegalAccessException ex) {
                getSuppressed = null;
                Logger.warning("Cannot log message!", ex);
            } catch (InvocationTargetException ex) {
                getSuppressed = null;
                Logger.warning("Cannot log message!", ex);
            }
        }

        for (Throwable se : thrs) {
            printEnclosedStackTrace(se, out, trace, "Suppressed: ", "\t", dejaVu);
        }

        // Print cause, if any
        Throwable ourCause = thr.getCause();
        if (ourCause != null) {
            printEnclosedStackTrace(ourCause, out, trace, "Caused by: ", "", dejaVu);
        }

        return out;
    }

    // Used in computeDisplay
    private void printEnclosedStackTrace(Throwable thr, CList<String> s, StackTraceElement[] enclosingTrace, String caption, String prefix, Set<Throwable> dejaVu) {
        if (dejaVu.contains(thr)) {
            s.add("\t[CIRCULAR REFERENCE:" + thr + "]");
        } else {
            dejaVu.add(thr);
            // Compute number of frames in common between this and enclosing trace
            StackTraceElement[] trace = thr.getStackTrace();
            int m = trace.length - 1;
            int n = enclosingTrace.length - 1;
            while (m >= 0 && n >= 0 && trace[m].equals(enclosingTrace[n])) {
                m--;
                n--;
            }
            int framesInCommon = trace.length - 1 - m;

            // Print our stack trace
            s.add(prefix + caption + thr);
            for (int i = 0; i <= m; i++) {
                s.add(prefix + "    at " + trace[i]);
            }
            if (framesInCommon != 0) {
                s.add(prefix + "    ... " + framesInCommon + " more");
            }

            Throwable[] thrs = new Throwable[0];
            if (getSuppressed != null) {
                try {
                    thrs = (Throwable[]) getSuppressed.invoke(thr);
                } catch (IllegalAccessException ex) {
                    getSuppressed = null;
                    Logger.warning("Cannot log message!", ex);
                } catch (InvocationTargetException ex) {
                    getSuppressed = null;
                    Logger.warning("Cannot log message!", ex);
                }
            }

            // Print suppressed exceptions, if any
            for (Throwable se : thrs) {
                printEnclosedStackTrace(se, s, trace, "Suppressed: ", prefix + "    ", dejaVu);
            }

            // Print cause, if any
            Throwable ourCause = thr.getCause();
            if (ourCause != null) {
                printEnclosedStackTrace(ourCause, s, trace, "Caused by: ", prefix, dejaVu);
            }
        }
    }

    /**
     * An element of the log, which may include a log level, message, and
     * optionally either a throwable or message body.
     */
    public static class Element {

        public final LogLevel level;
        public final String msg;
        public final Throwable thr;
        public final String body;

        Element(LogLevel level, String msg, Throwable thr) {
            this.level = level;
            this.msg = msg;
            this.thr = thr;
            this.body = null;
        }

        Element(LogLevel level, String msg, String body) {
            this.level = level;
            this.msg = msg;
            this.thr = null;
            this.body = body;
        }

        @Override
        public String toString() {
            if (thr != null) {
                return level + ": " + msg + ": " + thr;
            } else {
                return level + ": " + msg;
            }
        }
    }
}
