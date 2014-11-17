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
package ccre.workarounds;

import java.io.PrintStream;

/**
 * The default ThrowablePrinter that is used when a fully-features java system
 * is available. This does the same as <code>thr.printStackTrace(pstr);</code>
 *
 * @author skeggsc
 */
class DefaultThrowablePrinter extends ThrowablePrinter {

    public static void main(String[] args) {
        test();
    }

    private static void test() {
        System.out.println("Handler is " + ThrowablePrinter.getMethodCaller(-2));
        System.out.println("Dispatch is " + ThrowablePrinter.getMethodCaller(-1));
        System.out.println("Callee is " + ThrowablePrinter.getMethodCaller(0));
        System.out.println("Caller by " + ThrowablePrinter.getMethodCaller(1));
    }

    @Override
    public void send(Throwable thr, PrintStream pstr) {
        thr.printStackTrace(pstr);
    }

    @Override
    public CallerInfo findMethodCaller(int index) {
        int traceIndex = index + 1;
        StackTraceElement[] trace = new Throwable().getStackTrace();
        if (traceIndex < 0 || traceIndex >= trace.length || trace[traceIndex] == null) {
            return null;
        } else {
            StackTraceElement elem = trace[traceIndex];
            return new CallerInfo(elem.getClassName(), elem.getMethodName(), elem.getFileName(), elem.getLineNumber());
        }
    }
}
