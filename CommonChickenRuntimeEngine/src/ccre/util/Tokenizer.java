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
package ccre.util;

import java.io.EOFException;

/**
 * A system for grabbing tokens from an input string.
 *
 * @author skeggsc
 */
public class Tokenizer {

    private String input = "";
    private int index;

    public Tokenizer() {
    }

    public Tokenizer(String input) {
        setInput(input);
    }

    public void setInput(String input) {
        this.input = input;
        index = 0;
    }

    public boolean hasNext() {
        return index < input.length();
    }

    public char nextChar() throws EOFException {
        if (!hasNext()) {
            throw new EOFException();
        }
        return input.charAt(index++);
    }

    public char peekChar() throws EOFException {
        if (!hasNext()) {
            throw new EOFException();
        }
        return input.charAt(index);
    }

    public boolean acceptChar(char c) {
        if (hasNext() && input.charAt(index) == c) {
            index++;
            return true;
        }
        return false;
    }

    public boolean acceptAnyChar(String s) {
        if (hasNext() && s.indexOf(input.charAt(index)) != -1) {
            index++;
            return true;
        }
        return false;
    }

    public int acceptCharIndexed(String s) {
        if (!hasNext()) {
            return -1;
        }
        int out = s.indexOf(input.charAt(index));
        if (out != -1) {
            index++;
        }
        return out;
    }

    public int nextInteger() {
        int out = 0;
        while (true) {
            int id = acceptCharIndexed("0123456789");
            if (id == -1) {
                break;
            }
            out = out * 10 + id;
        }
        return out;
    }

    public String remaining() {
        return input.substring(index);
    }

    public boolean acceptString(String str) {
        if (index + str.length() <= input.length() && input.substring(index, index + str.length()).equals(str)) {
            index += str.length();
            return true;
        }
        return false;
    }

    public String acceptWord(char delimit) throws EOFException {
        int oid = index;
        index = input.indexOf(delimit, index) + 1;
        if (index == 0) {
            index = oid;
            String out = remaining();
            index = input.length();
            return out;
        }
        return input.substring(oid, index - 1);
    }
}
