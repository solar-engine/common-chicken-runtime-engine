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

/**
 * Operations that might need to be accessed via the reflection console.
 *
 * @author skeggsc
 */
public class ReflectionOps {

    private ReflectionOps() {
    }

    // TODO: Invokes?
    public static Object getStatic(Class<?> c, String fieldname) throws Throwable {
        ReflectionEngine inst = ReflectionEngine.getInstance();
        String prefix = c.getName() + "/";
        String suffix = "?_0RS";
        for (String symbol : inst.getSymbolIterable()) {
            if (symbol.startsWith(prefix) && symbol.endsWith(suffix)) {
                return inst.dispatch(inst.lookup(symbol), null, null);
            }
        }
        throw new RuntimeException("Cannot find reflection for static field " + fieldname + " on " + c);
    }

    public static Object getVirtual(Object o, String fieldname) throws Throwable {
        String c = o.getClass().getName();
        ReflectionEngine inst = ReflectionEngine.getInstance();
        while (c != null) {
            String prefix = c + "/";
            String suffix = "?_0R";
            for (String symbol : inst.getSymbolIterable()) {
                if (symbol.startsWith(prefix) && symbol.endsWith(suffix)) {
                    return inst.dispatch(inst.lookup(symbol), o, null);
                }
            }
            c = inst.getSuperclass(c);
        }
        throw new RuntimeException("Cannot find reflection for field " + fieldname + " on " + o);
    }

    public static void putStatic(Class<?> c, String fieldname, Object value) throws Throwable {
        ReflectionEngine inst = ReflectionEngine.getInstance();
        String prefix = c.getName() + "/";
        String suffix = "!_1VS";
        for (String symbol : inst.getSymbolIterable()) {
            if (symbol.startsWith(prefix) && symbol.endsWith(suffix)) {
                inst.dispatch(inst.lookup(symbol), null, new Object[]{value});
                return;
            }
        }
        throw new RuntimeException("Cannot find reflection for static field " + fieldname + " on " + c);
    }

    public static void putVirtual(Object o, String fieldname, Object v) throws Throwable {
        String c = o.getClass().getName();
        ReflectionEngine inst = ReflectionEngine.getInstance();
        while (c != null) {
            String prefix = c + "/";
            String suffix = "!_1V";
            for (String symbol : inst.getSymbolIterable()) {
                if (symbol.startsWith(prefix) && symbol.endsWith(suffix)) {
                    inst.dispatch(inst.lookup(symbol), o, new Object[]{v});
                    return;
                }
            }
            c = inst.getSuperclass(c);
        }
        throw new RuntimeException("Cannot find reflection for field " + fieldname + " on " + o);
    }

    public static Object load(Object[] a, int i) {
        return a[i];
    }

    public static void store(Object[] a, int i, Object o) {
        a[i] = o;
    }

    public static int length(Object[] a) {
        return a.length;
    }

    public static Object[] newarrayO(int len) {
        return new Object[len];
    }

    public static boolean load(boolean[] a, int i) {
        return a[i];
    }

    public static void store(boolean[] a, int i, boolean o) {
        a[i] = o;
    }

    public static int length(boolean[] a) {
        return a.length;
    }

    public static boolean[] newarrayZ(int len) {
        return new boolean[len];
    }

    public static byte load(byte[] a, int i) {
        return a[i];
    }

    public static void store(byte[] a, int i, byte o) {
        a[i] = o;
    }

    public static int length(byte[] a) {
        return a.length;
    }

    public static byte[] newarrayB(int len) {
        return new byte[len];
    }

    public static char load(char[] a, int i) {
        return a[i];
    }

    public static void store(char[] a, int i, char o) {
        a[i] = o;
    }

    public static int length(char[] a) {
        return a.length;
    }

    public static char[] newarrayC(int len) {
        return new char[len];
    }

    public static double load(double[] a, int i) {
        return a[i];
    }

    public static void store(double[] a, int i, double o) {
        a[i] = o;
    }

    public static int length(double[] a) {
        return a.length;
    }

    public static double[] newarrayD(int len) {
        return new double[len];
    }

    public static float load(float[] a, int i) {
        return a[i];
    }

    public static void store(float[] a, int i, float o) {
        a[i] = o;
    }

    public static int length(float[] a) {
        return a.length;
    }

    public static float[] newarrayF(int len) {
        return new float[len];
    }

    public static int load(int[] a, int i) {
        return a[i];
    }

    public static void store(int[] a, int i, int o) {
        a[i] = o;
    }

    public static int length(int[] a) {
        return a.length;
    }

    public static int[] newarrayI(int len) {
        return new int[len];
    }

    public static long load(long[] a, int i) {
        return a[i];
    }

    public static void store(long[] a, int i, long o) {
        a[i] = o;
    }

    public static int length(long[] a) {
        return a.length;
    }

    public static long[] newarrayL(int len) {
        return new long[len];
    }

    public static short load(short[] a, int i) {
        return a[i];
    }

    public static void store(short[] a, int i, short o) {
        a[i] = o;
    }

    public static int length(short[] a) {
        return a.length;
    }

    public static short[] newarrayS(int len) {
        return new short[len];
    }

    public static void throwAny(Throwable thr) throws Throwable {
        throw thr;
    }

    public static double todouble(float n) {
        return (double) n;
    }

    public static double todouble(int n) {
        return (double) n;
    }

    public static double todouble(byte n) {
        return (double) n;
    }

    public static double todouble(short n) {
        return (double) n;
    }

    public static double todouble(long n) {
        return (double) n;
    }

    public static double todouble(char n) {
        return (double) n;
    }

    public static float tofloat(int n) {
        return (float) n;
    }

    public static float tofloat(double n) {
        return (float) n;
    }

    public static float tofloat(byte n) {
        return (float) n;
    }

    public static float tofloat(short n) {
        return (float) n;
    }

    public static float tofloat(long n) {
        return (float) n;
    }

    public static float tofloat(char n) {
        return (float) n;
    }

    public static int toint(float n) {
        return (int) n;
    }

    public static int toint(double n) {
        return (int) n;
    }

    public static int toint(byte n) {
        return (int) n;
    }

    public static int toint(short n) {
        return (int) n;
    }

    public static int toint(long n) {
        return (int) n;
    }

    public static int toint(char n) {
        return (int) n;
    }

    public static byte tobyte(float n) {
        return (byte) n;
    }

    public static byte tobyte(int n) {
        return (byte) n;
    }

    public static byte tobyte(double n) {
        return (byte) n;
    }

    public static byte tobyte(short n) {
        return (byte) n;
    }

    public static byte tobyte(long n) {
        return (byte) n;
    }

    public static byte tobyte(char n) {
        return (byte) n;
    }

    public static short toshort(float n) {
        return (short) n;
    }

    public static short toshort(int n) {
        return (short) n;
    }

    public static short toshort(double n) {
        return (short) n;
    }

    public static short toshort(byte n) {
        return (short) n;
    }

    public static short toshort(long n) {
        return (short) n;
    }

    public static short toshort(char n) {
        return (short) n;
    }

    public static long tolong(float n) {
        return (long) n;
    }

    public static long tolong(int n) {
        return (long) n;
    }

    public static long tolong(double n) {
        return (long) n;
    }

    public static long tolong(byte n) {
        return (long) n;
    }

    public static long tolong(short n) {
        return (long) n;
    }

    public static long tolong(char n) {
        return (long) n;
    }

    public static char tochar(float n) {
        return (char) n;
    }

    public static char tochar(int n) {
        return (char) n;
    }

    public static char tochar(double n) {
        return (char) n;
    }

    public static char tochar(byte n) {
        return (char) n;
    }

    public static char tochar(short n) {
        return (char) n;
    }

    public static char tochar(long n) {
        return (char) n;
    }

    public static double add(double a, double b) {
        return a + b;
    }

    public static double sub(double a, double b) {
        return a - b;
    }

    public static double mul(double a, double b) {
        return a * b;
    }

    public static double div(double a, double b) {
        return a / b;
    }

    public static double rem(double a, double b) {
        return a % b;
    }

    public static double neg(double d) {
        return -d;
    }

    public static float add(float a, float b) {
        return a + b;
    }

    public static float sub(float a, float b) {
        return a - b;
    }

    public static float mul(float a, float b) {
        return a * b;
    }

    public static float div(float a, float b) {
        return a / b;
    }

    public static float rem(float a, float b) {
        return a % b;
    }

    public static float neg(float d) {
        return -d;
    }

    public static int add(int a, int b) {
        return a + b;
    }

    public static int sub(int a, int b) {
        return a - b;
    }

    public static int mul(int a, int b) {
        return a * b;
    }

    public static int div(int a, int b) {
        return a / b;
    }

    public static int rem(int a, int b) {
        return a % b;
    }

    public static int neg(int d) {
        return -d;
    }

    public static int and(int a, int b) {
        return a & b;
    }

    public static int or(int a, int b) {
        return a | b;
    }

    public static int shl(int a, int b) {
        return a << b;
    }

    public static int shr(int a, int b) {
        return a >> b;
    }

    public static int ushr(int a, int b) {
        return a >>> b;
    }

    public static int xor(int a, int b) {
        return a ^ b;
    }

    public static long add(long a, long b) {
        return a + b;
    }

    public static long sub(long a, long b) {
        return a - b;
    }

    public static long mul(long a, long b) {
        return a * b;
    }

    public static long div(long a, long b) {
        return a / b;
    }

    public static long rem(long a, long b) {
        return a % b;
    }

    public static long neg(long d) {
        return -d;
    }

    public static long and(long a, long b) {
        return a & b;
    }

    public static long or(long a, long b) {
        return a | b;
    }

    public static long shl(long a, long b) {
        return a << b;
    }

    public static long shr(long a, long b) {
        return a >> b;
    }

    public static long ushr(long a, long b) {
        return a >>> b;
    }

    public static long xor(long a, long b) {
        return a ^ b;
    }
}
