/*
 * Copyright 2015 Colby Skeggs.
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
package ccre.deployment;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that specifies that a static method on a main deployment class
 * should be made available as an option to the user.
 *
 * @author skeggsc
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DepTask {
    /**
     * Returns the name that should be displayed for this task. If not
     * specified, a name will be automatically generated based on the method
     * name.
     *
     * @return the name for the task, or the empty string for the default.
     */
    public String name() default "";

    /**
     * Returns whether or not this task should be run in a separate JVM from any
     * host JVM (such as Eclipse.)
     *
     * @return if this task should run separately.
     */
    public boolean fork() default false;
}
