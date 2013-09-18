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
* System overview for users (incomplete)
* Example programs (incomplete)
* Javadoc (complete, but not available)
* System overview for maintainers (incomplete)

# Getting Started Guide

Follow the following guides in order to get started with the CCRE. Knowledge of a standard FRC java system is currently assumed.

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

Next, you will want to connect the inputs to the motors.

    DriverImpls.createSynchTankDriver(duringTeleop, leftJoystickAxis, rightJoystickAxis, leftMotorOutput, rightMotorOutput);

An EventSource is an event emitter - when the event occurs (is produced), all registered listeners on the EventSource are fired. It is an interface with the following methods:

    boolean addListener(EventConsumer listener);
    void removeListener(EventConsumer listener) throws IllegalStateException;

An EventConsumer is the opposite, an event receiver. It can be fired, at which point whatever it is associated with will be executed. It is an interface with the following method:

    void eventFired();

duringTeleop is an EventSource that is fired about each ~20ms during teleoperated mode to update the state of the robot. Many systems use EventSources in order to decide when to update their outputs.

DriverImpls is a utility class containing various prewritten pieces of drive code.
DriverImpls.createSynchTankDriver(when, inA, inB, outA, outB) is a method that writes the specified inputs to the specified outputs wheneven the given event is produced.

So, the piece of code, about every ~20 milliseconds in teleop, will update the motors with the values from the joysticks. This is equivalent behavior to a simple piece of drive code created without the CCRE.

All of the code together:

    public class Main extends SimpleCore {
        protected void createSimpleControl() {
            FloatOutput leftMotorOutput = makeTalonMotor(1, MOTOR_FORWARD);
            FloatOutput rightMotorOutput = makeTalonMotor(2, MOTOR_REVERSE);
            FloatInputPoll leftJoystickAxis = joystick1.getYChannel();
            FloatInputPoll rightJoystickAxis = joystick2.getYChannel();
            DriverImpls.createSynchTankDriver(duringTeleop, leftJoystickAxis, rightJoystickAxis, leftMotorOutput, rightMotorOutput);
        }
    }

And that snippet of code is basic working drive code for a robot.

## Testing robot code in the emulator

At this point, you will most likely want to test your code. In an ordinary FRC setting, this must be done with a real robot. However, the CCRE includes an emulator that allows for partial testing code without a real robot.

To run an application in the emulator, right-click your robot project and select 'Test'.

A window will pop up with readouts and controls for all inputs and outputs supported by Igneous.

Ensure that TELEOPERATED is selected and then hit ENABLE.

Assuming that the code that you are testing is the code created in the previous section, drag the first sliders under Joystick 1 and Joystick 2 and watch as the motor outputs change for the first and second motors. If this works, congratulations! You've just written your first piece of CCRE drive code.

## Deploying robot code to a real robot

Once you want to test your code in the real world, and when you want to actually drive your robot using your code, ensure that your IDE is set up to download standard FRC robot code, and then hit 'Run' in your main robot project

If you used the drive code tutorial above, you will want to ensure that the motor ports are the appropriate motors ports on the real robot.

This should download the new robot code and reboot the cRIO.

At this point, everything is done exactly as if you had written normal FRC code. Plug in joysticks to your driver station, and enable the robot. You can now watch your code working on your real robot!

# System overview for users

The CCRE and Igneous contain very large amounts of code, and it would not be worth your time to understand all of the internals. Below are quick overviews of all the relevant parts of the CCRE. More detailed information, for those attempting to modify the CCRE, can be found at the end of the overall documentation.

## General CCRE interfaces and systems

This section contains more generalized sections of the CCRE that are important for using the CCRE.

### CCRE CCollections

In standard SE Java, you can use classes such as ArrayList, Collection, LinkedList, HashMap, et cetera. On an FRC robot, these classes are not available.

This poses a problem for writing general code that works on both robots and driver stations.

The CCRE, to solve this issue, contains a small collection of commonly-used classes that provide the most-used majority of the features needed from the Java SE Collections system.

When not otherwise specified, these work the same as the similarly-named versions from the Java SE standard library.

1. CCollection is the base interface of all CCRE collections. It is similar to Java SE's Collection.
2. CList is the base interface for ordered lists, and extends CCollection. It is similar to Java SE's List.
3. CArrayList and CLinkedList are implementations of lists similar to Java SE's ArrayList and LinkedList.
4. CAbstractList is an abstract implementation of a list that is used internally to implement other lists. This is similar to Java SE's AbstractList.
5. CArrayUtils is a collection of useful methods for dealing with arrays and lists. It is similar to Java SE's Collections and Arrays classes.
6. CHashMap is a rudimentary HashMap implementation similar to Java SE's HashMap.

These are not all fully tested, so please report any bugs via the BitBucket issue tracker or by emailing the Team 1540 maintainer.

### CCRE Events

The CCRE contains an event system, by which modules can publish and subscribe to events, and wait for events to be fired.

#### ccre.event.EventConsumer

EventConsumer is an interface with a single method:

    void eventFired();

To fire an EventConsumer (cause its event to be executed), simply call its eventFired() method.

To create a new anonymous EventConsumer (to run code when it is executed), the following code is recommended.

    EventConsumer out = new EventConsumer() {
        public void eventFired() {
            // Code goes here
        }
    };

#### ccre.event.EventSource

EventSource is an interface with two methods:

    boolean addListener(EventConsumer listener);
    void removeListener(EventConsumer listener) throws IllegalStateException;

A user of EventSource can add and remove EventConsumers such that the consumers are fired when the event is produced.

Any attempt to remove a listener that is not listening will create an IllegalStateException.

In general, it is a bad idea to implement this interface yourself. Instead, you should use an Event object, as explained below.

#### ccre.event.Event

An Event is a powerful implementation of EventSource, and should be used when an EventSource needs to be created. It contains multiple methods and constructors:

    Event()
    Event(EventConsumer event)
    Event(EventConsumer... events)
    void produce();
    boolean hasConsumers();
    static EventConsumer consumerForRunnable(Runnable target);

In addition to these methods, it implements the interfaces EventSource, Runnable, and EventConsumer. When an Event is fired as an EventConsumer, ran as a Runnable, or has its produce() method called, it will produced an event.

When this happens, all EventConsumers registered using addListener will have their eventFired() method called.

hasConsumers() will return false if calling produce() will cause no effect, and could be used for optimization purposes.

consumerForRunnable(Runnable) is a utility method included to offer the opposite of Event's implied usability as a Runnable object. You can pass a Runnable to this method, and it will return an EventConsumer that will run the runnable when the event is fired.

Creating an Event with some number of EventConsumers is the same as creating an Event without any and then adding all the specified EventConsumers using addListener.

### CCRE Channels

The CCRE contains a channel system, with which current values of binary and gradient systems can be propagated in multiple directions.

There are two overall categories of channels: Boolean and Float. These are the kinds of values that the channel will carry.

There are four subtypes for each value type: Outputs, Inputs, InputPolls, and InputProducers.

#### Outputs

An Output represents something that a value can be written to - for example a motor or readout gauge. It is only updated when a value is *sent* to it using writeValue(value).

For example, here is the method provided by a BooleanOutput:

    void writeValue(boolean value);

#### InputPolls

An InputPoll represents something that can be polled (read) whenever a users wants to know the current value, for example a joystick axis or an analog input. It does not notify anything when the value changes.

For example, here is the method provided by a FloatInputPoll:

    float readValue();

#### InputProducers

An InputProducer is like the opposite of an Output. Instead of getting sent values, it sends values. These values can be subscribed, by specifying an Output for the values to be automatically sent to. However, this has no way to fetch the current value.

For example, here are the methods provided by a BooleanInputProducer:

    void addTarget(BooleanOutput output);
    boolean removeTarget(BooleanOutput output);

This relation to Outputs is similar to the relation between EventSources and EventConsumers.

WORKING HERE - Next are Inputs.

### CCRE Logging

### Cluck

### CCRE Networked Tuning

### CCRE Networked Strings

## Specific CCRE classes for implementation assistance.

This section contains more specific sections of the CCRE that are useful but not as important for using the CCRE.

### CArrayUtils

### Concurrency

### Mixing, DriverImpls

### General Networking

### PhidgetReader

### Data storage

### Throwable printing

## Igneous classes

WORKING HERE...
