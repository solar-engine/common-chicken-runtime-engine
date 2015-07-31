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
 * @deprecated unused by anything within the CCRE, and probably never will be.
 */
@Deprecated // NOTE: this is duplicated in tests.xml to exclude this from testing
public class Tokenizer {

    private String input = "";
    private int index;

    /**
     * Create a new Tokenizer with no current input.
     */
    public Tokenizer() {
    }

    /**
     * Set the specified block of text
     *
     * @param input The text to process.
     */
    public void setInput(String input) {
        this.input = input;
        index = 0;
    }

    /**
     * @return whether or not more tokens are available.
     */
    public boolean hasNext() {
        return index < input.length();
    }

    /**
     * Return and consume the next character.
     *
     * @return The next character.
     * @throws EOFException If there are no more characters.
     */
    public char nextChar() throws EOFException {
        if (!hasNext()) {
            throw new EOFException();
        }
        return input.charAt(index++);
    }

    /**
     * Return, but don't consume, the next character.
     *
     * @return The next character.
     * @throws EOFException If there are no more characters.
     */
    public char peekChar() throws EOFException {
        if (!hasNext()) {
            throw new EOFException();
        }
        return input.charAt(index);
    }

    /**
     * If the specified character is next in the input, consume it.
     *
     * @param c The character to accept.
     * @return If the character was consumed.
     */
    public boolean acceptChar(char c) {
        if (hasNext() && input.charAt(index) == c) {
            index++;
            return true;
        }
        return false;
    }

    /**
     * If any of the specified characters are next in the input, consume it.
     *
     * @param s The characters to accept.
     * @return If any character was consumed.
     */
    public boolean acceptAnyChar(String s) {
        if (hasNext() && s.indexOf(input.charAt(index)) != -1) {
            index++;
            return true;
        }
        return false;
    }

    /**
     * If any specified character is next in the input, consume it and return
     * the index in the parameter string.
     *
     * @param s The characters to accept.
     * @return The index is the parameter of the accepted character, or -1 if
     * none.
     */
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

    /**
     * Read in the next integer from the stream. If no integer, return zero.
     *
     * @return The next integer.
     */
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

    /**
     * @return the rest of the input.
     */
    public String remaining() {
        return input.substring(index);
    }

    /**
     * If the specified string is next in the stream, consume it.
     *
     * @param str The string to accept.
     * @return If the string was consumed.
     */
    public boolean acceptString(String str) {
        if (index + str.length() <= input.length() && input.substring(index, index + str.length()).equals(str)) {
            index += str.length();
            return true;
        }
        return false;
    }

    /**
     * Accept a word up to the specified delimiter (or the end of the string if
     * not found.)
     *
     * @param delimit The delimiter to search for.
     * @return The word up to the delimiter.
     */
    public String acceptWord(char delimit) {
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
