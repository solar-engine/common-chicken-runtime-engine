/*
 * Copyright 2014-2015 Cel Skeggs
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

/**
 * A class representing information about the caller of a method. This is
 * converted into by the appropriate local platform.
 *
 * @author skeggsc
 */
public class CallerInfo {
    private final String className;
    private final String methodName;
    private final String fileName;
    private final int lineNum;

    /**
     * Create a new CallerInfo with the specified pieces of information.
     *
     * Only className is required - the rest can be set to null or -1.
     *
     * @param className the name of the class, required. This will be normalized
     * into dot-form.
     * @param methodName the name of the method, optional.
     * @param fileName the name of the file, optional.
     * @param lineNum the line number, optional. (Set to a negative number for
     * unspecified.)
     * @throws IllegalArgumentException if className is null.
     */
    public CallerInfo(String className, String methodName, String fileName, int lineNum) {
        if (className == null) {
            throw new IllegalArgumentException();
        }
        this.className = className.replace('/', '.');
        this.methodName = methodName;
        this.fileName = fileName;
        this.lineNum = lineNum;
    }

    /**
     * @return the name of the class, in dot-form.
     */
    public String getClassName() {
        return className;
    }

    /**
     * @return the name of the method, or null if not known.
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * @return the name of the file, or null if not known.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @return the line number, or a negative number if not known.
     */
    public int getLineNum() {
        return lineNum;
    }

    /**
     * Describe the CallerInfo with a basic string.
     *
     * CallerInfo returns a string in the form of
     * "[CLASSNAME].[METHODNAME]([FILENAME]:[LINENUM])", where each "[VAR]" is
     * replaced with the corresponding value. CLASSNAME is in dot-form.
     *
     * @return the generated string.
     */
    public String toString() {
        return className + "." + (methodName == null ? "<unknown>" : methodName) + "(" + (fileName == null ? "<unknown>" : fileName) + ":" + (lineNum == -1 ? "?" : lineNum) + ")";
    }
}
