# What is the CCRE?

The CCRE is a very powerful library for building robot control programs. It was originally created to run FIRST Robotics Competition (FRC) robots.

The primary advantage that the CCRE has over the traditional systems for building FRC code is that everything is modular and easily connected. For example, every motor is the same kind of output, which is the same kind of output as a servo, or a PID error value, or an output shared over the network.

The CCRE distribution has multiple subprojects:

* The core CCRE project is a library of java classes that provide the majority of the CCRE's functionality.
* The CCRE Igneous project is the framework for FRC-specific programming, include the framework into which the user program sits.
* The CCRE Poultry Inspector is the application that allows for inspecting and controlling systems over the network.
* The CCRE Igneous Emulator project is the drop-in replacement that allows for projects using the CCRE Igneous framework to be ran and tested on a normal desktop computer, without actually controlling a robot.
* The Sample Igneous Robot project contains all the buildscripts needed to easily compile and deploy Igneous projects and run them in the emulator.

The current maintainer of the CCRE can be contacted at skeggsc (at) catlin (dot) edu.

# CCRE License

The CCRE is distributed under the terms of the LGPL version 3. #TODO: Include the terms.
The documentation is licensed under creative commons Attribution + ShareAlike. #TODO: Include the terms.

# Overview of the CCRE documentation
There are eight major parts of the CCRE documentation:

* CCRE description
* CCRE License
* Overview of Documentation (here)
* Getting Started Guide
* System overview for users
* Example programs
* Javadoc
* System overview for maintainers

# Getting Started Guide

## System Requirements

1. A Java SE 6 or 7 installation including the JDK.
2. An IDE with the FRC squawk plugins installed. (This will only be required for the Igneous project at some point, but is required for all CCRE users currently)
3. A Git client, for example the client included in NetBeans
4. A recent version of NetBeans, since all projects are currently only stored as NetBeans projects.

## Download guide

This is the official bitbucket project for the CCRE: <https://bitbucket.org/col6y/common-chicken-runtime-engine>

Currently, the Git link is <https://bitbucket.org/col6y/common-chicken-runtime-engine.git> - this can be found on the project page.

Use your Git client to check out the full repository. This will include all five subprojects.

In NetBeans, open all projects relevant to your interests. Most likely, this means all five of them.

## Building guide

Clean and Build each project. This can be performed by right-clicking the project name and selecting 'Clean and Build'.

## Notes

Igneous projects are the only projects current described in this documentation, and the only projects currently found in the code. You will need to figure out other systems yourself - there may be useful information spread throughout this document. You may also contact the Team 1540 maintainer (see the first section).

## Creating a new Igneous project

Right-click the 'Sample Igneous Robot' project and select 'Copy...'

Enter a new project name instead of 'Sample_Igneous_Robot'.

Currently, it is recommended to leave the project folder as the folder where you checked out the CCRE to. This should be the default value. This may be modified in the future, but if you do, you will need to update linking to the other projects.

Hit 'Copy' to create your project.

At this point, you will want to decide which codebase you want to start from. You have the following options:

    org.team1540.minimal
    - A tiny program that only displays a short message on the driver station.
    * You will probably want to use this option!
    org.team1540.example
    - A small program that includes driving, shifting, and a compressor.
    org.team1540.infernodante
    - A fully-featured robot implementation,
      converted to the CCRE from its original form as the proof-of-concept.

You will most likely wish to delete both other codebases from your new project. Do not remove them from the 'Sample Igneous Robot' project.

Then, rename the package to a new package name. This is recommended to include your team number. You may also wish to rename the main class at this time.

Open 'main.properties', which can be found either under 'src' or under 'default package'.
It will likely contain a similar line to the following:

    igneous.main=org.team1540.minimal.Minimal

Modify the part after the '=' to be the full path to the class that you have created. For example, if you had named your package 'test' and your main class 'Testy', you would put:

    igneous.main=test.Testy

At this point, your project should be ready. Build the project, either by the button in the toolbar or by right-clicking the project and selecting 'Clean and Build'.

There should be no errors at this time. If there are, either try to work them out yourself, create a new issue in the issue tracker on the bitbucket project (see *Download Guide* above), or contact a Team 1540 representative (see the first section for an email address).

## Writing your first robot drivecode

This short guide will explain how to create a new main Igneous class with basic drivecode.

For the purposes of this short subguide, you will want to remove the contents of the main class for the codebase that you selected.

You are assumed to use NetBeans's auto-import-statement-generation, or otherwise figure out the imports yourself. They will not be spoken about further, except that the classes that you want to import will probably be within the ccre top-level package.

Start by adding the following outline for your main class:

    // Most main Igneous applications will extend SimpleCore.
    // Don't change this unless you know what you are doing.
    public class Main extends SimpleCore {
        // createSimpleControl() is where the code is placed that sets up the robot.
        protected void createSimpleControl() {
            // Main code goes here
        }
    }

Unlike standard IterativeRobot or SimpleRobot FRC systems, the majority of your code is not ran every cycle - the code that you write instead sets up the machinery that will run the robot, much like snapping together prefabricated blocks.

The rest of the code contained here should be placed within the createSimpleControl() method.

Next, you will want to find the outputs for your motors:

    FloatOutput leftMotorOutput = makeTalonMotor(1, MOTOR_FORWARD);
    FloatOutput rightMotorOutput = makeTalonMotor(2, MOTOR_REVERSE);

makeTalonMotor is a method inherited from SimpleCore - it takes a PWM port number and a motor direction. makeVictorMotor and makeJaguarMotor also exist depending on which kind of Speed Controller you are using.

MOTOR_FORWARD and MOTOR_REVERSE are also inherited from SimpleCore - they are constants than can be passed to motor creation methods to control reversal of the motors. In this case, one motor runs forward and one in reverse because one motor points the opposite direction of the other. This is standard fare when controlling a two-motor drive system.

FloatOutput is a basic building block of the CCRE. Unlike makeTalonMotor and MOTOR_REVERSE, it is generic to all CCRE applications instead of just Igneous robots.

A FloatOutput is an interface with one method:

    public void writeValue(float value);

Many systems of the CCRE will return a FloatOutput, all of which can have any floating-point value written to them.

In the case of a motor represented as a FloatOutput, writing a value from -1.0f to 1.0f will change the speed of the motor to the specified value.

Next, you will want to find the inputs to control the motors:

    FloatInputPoll leftJoystickAxis = joystick1.getYChannel();
    FloatInputPoll rightJoystickAxis = joystick2.getYChannel();

joystick1, joystick2, joystick3, and joystick4 are the four joysticks attached to the driver station. In our case, we only care about their method getYChannel(), which returns a FloatInputPoll representing the current value of the joystick.

FloatInputPoll is another basic building block of the CCRE. It is also generic to all CCRE applications. It is an interface with one method:

    public float readValue();

Many systems of the CCRE will return a FloatInputPoll, all of which can have a floating-point value read from them.

In the case of a joystick axis represented as a FloatInputPoll, reading a value will return a value from -1.0f to 1.0f.

WORKING HERE...
