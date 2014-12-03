/*
 * Copyright 2014 Colby Skeggs.
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
package ccre.igneous.emulator;

import javax.swing.JFrame;

import ccre.igneous.emulator.devices.FloatViewDevice;
import ccre.igneous.emulator.devices.JoystickDevice;
import ccre.igneous.emulator.devices.RobotModeDevice;
import ccre.log.FileLogger;
import ccre.log.NetworkAutologger;

/**
 * The launcher for the DeviceList system.
 *
 * @author skeggsc
 */
public class DeviceListMain extends JFrame {

    private static final long serialVersionUID = 2839583686126149124L;

    public static void main(String args[]) {
        NetworkAutologger.register();
        FileLogger.register();
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new DeviceListMain().setVisible(true);
            }
        });
    }

    private final DeviceListPanel canvas = new DeviceListPanel();

    public DeviceListMain() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        this.setContentPane(canvas);
        this.setSize(640, 480);
        canvas.add(new RobotModeDevice());
        canvas.add(new FloatViewDevice("TALON 0"));
        canvas.add(new FloatViewDevice("TALON 1"));
        canvas.add(new FloatViewDevice("TALON 2"));
        JoystickDevice test = new JoystickDevice(1);
        test.getXAxisSource();
        test.getYAxisSource();
        canvas.add(test);
        /*DeviceGroup testGroup = new DeviceGroup();
        testGroup.add(new HeadingDevice("GROUP TEST 0"));
        testGroup.add(new FillBarDevice("BAR 0"));
        testGroup.add(new BooleanViewDevice("BOOL 0"));
        canvas.add(testGroup);
        canvas.add(new BooleanViewDevice("SOLENOID 0"));
        canvas.add(new BooleanViewDevice("SOLENOID 1"));
        canvas.add(new ControlBarDevice("AXIS 1"));*/
        canvas.start();
    }
}
