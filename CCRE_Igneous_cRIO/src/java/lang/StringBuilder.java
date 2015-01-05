/*
 * Copyright 2014-2015 Colby Skeggs.
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
package java.lang;

/**
 * Used during compilation, but then removed completely. This lets the
 * second-stage compilation (once StringBuilder has be translated to
 * StringBuffer) to compile properly.
 * 
 * @author skeggsc
 */
@SuppressWarnings("javadoc")
public final class StringBuilder { // Just a stub. Will be deleted later in the build process.

    public StringBuilder() {
        throw new Error("Stub class.");
    }

    public StringBuilder(int a) {
        throw new Error("Stub class.");
    }

    public StringBuilder(String a) {
        throw new Error("Stub class.");
    }

    public int length() {
        throw new Error("Stub class.");
    }

    public int capacity() {
        throw new Error("Stub class.");
    }

    public void ensureCapacity(int a) {
        throw new Error("Stub class.");
    }

    public void setLength(int a) {
        throw new Error("Stub class.");
    }

    public char charAt(int a) {
        throw new Error("Stub class.");
    }

    public void getChars(int a, int b, char[] c, int d) {
        throw new Error("Stub class.");
    }

    public void setCharAt(int a, char b) {
        throw new Error("Stub class.");
    }

    public StringBuilder append(Object a) {
        throw new Error("Stub class.");
    }

    public StringBuilder append(String a) {
        throw new Error("Stub class.");
    }

    public StringBuilder append(char[] a) {
        throw new Error("Stub class.");
    }

    public StringBuilder append(char[] a, int b, int c) {
        throw new Error("Stub class.");
    }

    public StringBuilder append(boolean a) {
        throw new Error("Stub class.");
    }

    public StringBuilder append(char b) {
        throw new Error("Stub class.");
    }

    public StringBuilder append(int a) {
        throw new Error("Stub class.");
    }

    public StringBuilder append(long a) {
        throw new Error("Stub class.");
    }

    public StringBuilder append(float a) {
        throw new Error("Stub class.");
    }

    public StringBuilder append(double a) {
        throw new Error("Stub class.");
    }

    public StringBuilder insert(int a, float b) {
        throw new Error("Stub class.");
    }

    public StringBuilder insert(int a, double b) {
        throw new Error("Stub class.");
    }

    public StringBuilder delete(int a, int b) {
        throw new Error("Stub class.");
    }

    public StringBuilder deleteCharAt(int a) {
        throw new Error("Stub class.");
    }

    public StringBuilder insert(int a, Object b) {
        throw new Error("Stub class.");
    }

    public StringBuilder insert(int a, String b) {
        throw new Error("Stub class.");
    }

    public StringBuilder insert(int a, char[] b) {
        throw new Error("Stub class.");
    }

    public StringBuilder insert(int a, boolean b) {
        throw new Error("Stub class.");
    }

    public StringBuilder insert(int a, char b) {
        throw new Error("Stub class.");
    }

    public StringBuilder insert(int a, int b) {
        throw new Error("Stub class.");
    }

    public StringBuilder insert(int a, long b) {
        throw new Error("Stub class.");
    }

    public StringBuilder reverse() {
        throw new Error("Stub class.");
    }
}