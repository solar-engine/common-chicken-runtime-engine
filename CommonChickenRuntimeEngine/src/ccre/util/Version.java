/*
 * Copyright 2015 Colby Skeggs
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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.slf4j.LoggerFactory;

import ccre.storage.StorageSegment;

/**
 * Utilities for accessing the version of the CCRE.
 *
 * @author skeggsc
 */
public class Version {

    /**
     * Get the long-form version of the CCRE. This looks something like
     * ccre-v2.6.0-2-g2c0a136.
     *
     * If the version cannot be found, a string beginning with "unknown version"
     * will be returned.
     *
     * @return the long-form version.
     */
    public static String getVersion() {
        HashMap<String, String> versions = new HashMap<String, String>();
        InputStream props = Version.class.getResourceAsStream("/version.properties");
        if (props == null) {
            return "unknown version: no version.properties";
        }
        try {
            try {
                StorageSegment.loadProperties(props, false, versions);
            } catch (IOException e) {
                LoggerFactory.getLogger(Version.class).warn("IOException while reading /version.properties", e);
                return "unknown version: could not load";
            }
            String version = versions.get("ccre-version");
            if (version == null || version.equals("UNKNOWN")) {
                version = "unknown version: no property ccre-version";
            }
            return version;
        } finally {
            try {
                props.close();
            } catch (IOException e) {
                LoggerFactory.getLogger(Version.class).warn("IOException while closing /version.properties", e);
            }
        }
    }

    /**
     * Get the short-form version of the CCRE. This looks something like 2.6.0,
     * ?.?.?, or 2.6.0+M.
     *
     * The second form means that no version could be found, and the third form
     * means that the release is not exactly the release given by the previous
     * argument. (Changes have been made.)
     *
     * @return the version number.
     */
    public static String getShortVersion() {
        String version = getVersion();
        String[] parts = version.split("-");
        if (parts.length >= 2 && "ccre".equals(parts[0]) && parts[1].startsWith("v")) {
            if (parts.length >= 3 && !"0".equals(parts[2]) || version.contains("dirty")) {
                return parts[1].substring(1) + "+M";
            } else {
                return parts[1].substring(1);
            }
        }
        return "?.?.?";
    }

    private Version() {
    }
}
