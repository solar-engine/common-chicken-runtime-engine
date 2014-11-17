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

import ccre.log.Logger;

import com.sun.squawk.ExecutionPoint;
import com.sun.squawk.GC;
import com.sun.squawk.Klass;
import com.sun.squawk.Method;
import com.sun.squawk.NativeUnsafe;
import com.sun.squawk.VM;
import com.sun.squawk.vm.FieldOffsets;

import java.io.PrintStream;

/**
 * A throwable printer that works on Squawk and the FRC robot. This is because
 * Java ME doesn't use the same interface as Java SE.
 * 
 * This is very hacky, but it works.
 *
 * @author skeggsc
 */
public class IgneousThrowablePrinter extends ThrowablePrinter {

    /**
     * Has this yet been registered as the storage provider?
     */
    private static boolean registered = false;

    /**
     * Ensure that this is registered as the storage provider. A warning will be
     * logged if this is called a second time.
     */
    public static void register() {
        if (registered) {
            Logger.warning("IgneousThrowablePrinter already registered!");
            return;
        }
        registered = true;
        ThrowablePrinter.setProvider(new IgneousThrowablePrinter());
    }

    private IgneousThrowablePrinter() {
    }

    public void send(Throwable thr, PrintStream pstr) {
        // The code here works similarly to VM.printExceptionAndTrace
        pstr.print(GC.getKlass(thr).getName());
        String message = thr.getMessage();
        if (message == null) {
            pstr.println();
        } else {
            pstr.print(": ");
            pstr.println(message);
        }
        ExecutionPoint[] trace = (ExecutionPoint[]) NativeUnsafe.getObject(thr, (int) FieldOffsets.java_lang_Throwable$trace);
        if (trace != null) {
            for (int i = 0; i < trace.length; ++i) {
                if (trace[i] == null) {
                    pstr.println("    undecipherable");
                } else {
                    pstr.print("    ");
                    trace[i].print(pstr);
                    pstr.println();
                }
            }
        }
    }

    @Override
    public CallerInfo findMethodCaller(int index) {
        int targetIndex = index + 1;
        int reqLength = targetIndex + 1;
        ExecutionPoint[] stack = VM.reifyCurrentStack(reqLength);
        if (stack.length < reqLength || stack[targetIndex] == null) {
            return null;
        } else {
            ExecutionPoint target = stack[targetIndex];
            Klass k = target.getKlass();
            String kName = k.getName();
            Method m = target.getMethod();
            String mName;
            if (m == null) {
                int mtId;
                if ((mtId = k.getMethodIndex(target.mp, true)) >= 0) {
                    mName = "static #" + mtId;
                } else if ((mtId = k.getMethodIndex(target.mp, false)) >= 0) {
                    mName = "virtual #" + mtId;
                } else {
                    mName = "unknown at " + target.mp;
                }
            } else {
                mName = m.getName();
            }
            String sourceFileName = k.getSourceFileName();
            if (sourceFileName == null) {
                sourceFileName = "<UNKNOWN>";
            }
            int[] lnTab = m == null ? null : m.getLineNumberTable();
            int lineNo = lnTab == null ? -1 : Method.getLineNumber(lnTab, target.bci.toPrimitive());
            return new CallerInfo(kName, mName, sourceFileName, lineNo);
        }
    }
}
