/*
 * Copyright 2014 Colby Skeggs
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
package ccre.reflect;

import ccre.cluck.CluckGlobals;
import ccre.cluck.CluckNode;
import ccre.log.LogLevel;
import ccre.log.Logger;
import ccre.net.ClientSocket;
import ccre.net.ConnectionReceiverThread;
import ccre.util.CArrayList;
import ccre.util.LineCollectorOutputStream;
import ccre.util.Tokenizer;
import java.io.EOFException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * An interface designed to allow for working with the reflection system. It
 * provides an OutputStream that can be written to in order to control it and it
 * logs the results.
 *
 * @author skeggsc
 */
public class ReflectionConsole {

    public final OutputStream control = new LineCollectorOutputStream() {
        @Override
        protected void collect(String tostr) {
            execute(tostr);
        }
    };

    public void share(CluckNode node) {
        node.publish("reflection-console", control);
    }

    public static void attach() {
        new ReflectionConsole().share(CluckGlobals.node);
    }

    private final CArrayList<Object> results = new CArrayList<Object>();
    private final ReflectionEngine engine = ReflectionEngine.getInstance();

    private final class ReflectionTokenizer extends Tokenizer {

        private Object acceptAtom() throws EOFException {
            if (acceptChar('"')) {
                StringBuffer out = new StringBuffer();
                while (!acceptChar('"')) {
                    if (acceptChar('\\')) {
                        int id = acceptCharIndexed("nrt0123567");
                        out.append(id == -1 ? nextChar() : "\n\r\t\0\1\2\3\4\5\6\7");
                    }
                    out.append(nextChar());
                }
                return out.toString();
            } else if (Character.isDigit(peekChar())) {
                return nextInteger();
            } else if (acceptChar('$')) {
                return results.get(nextInteger());
            } else if (acceptString("null")) {
                return null;
            } else {
                throw new IllegalArgumentException("Unknown atom at: '" + tkn.remaining() + "'");
            }
        }
    }

    private final ReflectionTokenizer tkn = new ReflectionTokenizer();

    private synchronized void execute(String tostr) {
        logInfo("Reflect$ " + tostr);
        tkn.setInput(tostr);
        try {
            while (tkn.hasNext()) {
                if (tkn.acceptChar(' ')) {
                    continue;
                }
                Object result;
                if (tkn.acceptString("reset")) {
                    results.clear();
                    logInfo("Reflect> Cleared.");
                    continue;
                } else if (tkn.acceptChar('#')) {
                    String name = tkn.acceptWord(' ');
                    Integer lk = engine.lookup(name);
                    if (lk == null) {
                        try {
                            lk = Integer.parseInt(name);
                            name = engine.reverseLookup(lk); // SLOW!
                        } catch (NumberFormatException ex) {
                            String toretry = null;
                            for (String guess : engine.getSymbolIterable()) {
                                if (guess.indexOf(name) != -1) {
                                    if (toretry != null) {
                                        logWarning("Reflect! Multiple matching entries for: " + name + " (" + toretry + ", " + guess + ")", null);
                                        return;
                                    }
                                    toretry = guess;
                                }
                            }
                            if (toretry != null) {
                                lk = engine.lookup(toretry);
                                if (lk == null) {
                                    logWarning("Reflect! Guess failed...???????? what????", null);
                                    return;
                                }
                                name = toretry;
                            } else {
                                logWarning("Reflect! No such entry: " + name, null);
                                return;
                            }
                        }
                    }
                    Object self = null;
                    int curid = results.size();
                    if (name.endsWith("S")) { // If static
                        name = name.substring(0, name.length() - 1);
                    } else { // If virtual
                        self = results.get(--curid);
                    }
                    System.out.println("Name: " + name);
                    int argc = Integer.parseInt(name.substring(name.lastIndexOf('_') + 1, name.length() - 1));
                    Object[] args = new Object[argc];
                    for (int i = args.length - 1; i >= 0; i--) {
                        args[i] = results.get(--curid);
                    }
                    result = engine.dispatch(lk, self, args);
                } else {
                    result = tkn.acceptAtom();
                }
                if (result == null) {
                    logInfo("Reflect> null");
                } else {
                    logInfo("Reflect> $" + results.size() + " = " + result);
                    results.add(result);
                }
            }
        } catch (Throwable thr) {
            logWarning("Reflect!", thr);
        }
    }

    protected void logWarning(String str, Throwable thr) {
        Logger.log(LogLevel.WARNING, str, thr);
    }

    protected void logInfo(String str) {
        Logger.info(str);
    }
}
