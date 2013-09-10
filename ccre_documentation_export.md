# What is the CCRE?

The CCRE is a very powerful library for building robot control programs. It was originally created to run FIRST Robotics Competition (FRC) robots.

The primary advantage that the CCRE has over the traditional systems for building FRC code is that everything is modular and easily connected. For example, every motor is the same kind of output, which is the same kind of output as a servo, or a PID error value, or an output shared over the network.

The CCRE distribution has multiple subprojects:

* The core CCRE project is a library of java classes that provide the majority of the CCRE's functionality.
* The CCRE Igneous project is the framework for FRC-specific programming, include the framework into which the user program sits.
* The CCRE Poultry Inspector is the application that allows for inspecting and controlling systems over the network.
* The CCRE Igneous Emulator project is the drop-in replacement that allows for projects using the CCRE Igneous framework to be ran and tested on a normal desktop computer, without actually controlling a robot.
* The Sample Igneous Robot project contains all the buildscripts needed to easily compile and deploy Igneous projects and run them in the emulator.

# CCRE License

The CCRE is distributed under the terms of the LGPL version 3. TODO: Include the terms.
The documentation is licensed under creative commons Attribution + ShareAlike. TODO: Include the terms.

# Overview of the CCRE documentation
There are four major parts of the CCRE documentation:

* CCRE description
* CCRE License
* Overview of Documentation
* Getting Started Guide
* System overview for users
* Example programs
* Javadoc
* System overview for maintainers
