/*
 * Copyright 2013 Vincent Miller
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
package ccre.obsidian.comms;

/**
 *
 * @author MillerV
 */
public class CommsID {
    public static byte ID_HEARTBEAT = 0x00;
    public static byte ID_ENABLED = 0x01;
    
    public static byte ID_JOYSTICK_X = 0x02;
    public static byte ID_JOYSTICK_Y = 0x03;
    public static byte ID_JOYSTICK_Z = 0x04;
    
    public static byte ID_JOYSTICK_B1 = 0x05;
    public static byte ID_JOYSTICK_B2 = 0x06;
    public static byte ID_JOYSTICK_B3 = 0x07;
    public static byte ID_JOYSTICK_B4 = 0x08;
    public static byte ID_JOYSTICK_B5 = 0x09;
    public static byte ID_JOYSTICK_B6 = 0x0a;
}
