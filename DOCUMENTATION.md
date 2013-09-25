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
* Example programs (incomplete)
* Javadoc  - this can be found 
* System overview for maintainers (incomplete)

# Getting Started Guide

Follow the following guides in order to get started with the CCRE. Knowledge of a standard FRC java system is currently assumed.

## System Requirements

1. A Java SE 6 or 7 installation including the JDK.
2. A working [NetBeans](http://netbeans.org/) installation.
3. Installation of [FRC Plugins](http://firstforge.wpi.edu/sf/frs/do/viewSummary/projects.wpilib/frs) - see [this page](http://wpilib.screenstepslive.com/s/3120/m/7885/l/79405-installing-the-java-development-tools) for more information.
4. A Git client, for example the client included in NetBeans.

## Download guide

Find the Git checkout link from the [BitBucket project page](https://bitbucket.org/col6y/common-chicken-runtime-engine).

Use your Git client to check out the full repository. This will include all five subprojects.

How to do this in NetBeans:

1. Click on Team->Git->Clone... in the menu.
2. Paste the checkout link into the Repository URL box.
3. Leave the username and password blank for anonymous access.
4. Hit next.
5. It should display a list of branches. Select 'master'
6. Hit next.
7. Select a directory to place the master folder in. It is not recommended to place this in NetBeansProjects.
8. The clone name does not matter.
9. Ensure that the master branch is selected.
10. Ensure that the remote name is 'origin'.
11. Ensure that the checkbox for scanning for projects is checked.
12. Hit Finish.
13. Once it finishes, it will say that five projects have been cloned. Hit okay.
14. Select all five projects.
15. Hit Open.
16. All projects should open. You may wish to create a new project group at this time.

## Team number configuration guide

NetBeans will need you to specify your team number before continuing.

1. Click on Tools->Options (or NetBeans->Preferences... on Mac OS X)
2. Go to the Miscellaneous tab.
3. Go to the FRC Configuration subtab.
4. Enter your team number in the provided box.
5. Hit 'OK'

## Building guide

Clean and Build each project. This can be performed by right-clicking the project name and selecting 'Clean and Build'. You should build them in the following order: CommonChickenRuntimeEngine, CCRE_Igneous, CCRE_PoultryInspector, Igneous Emulator, Sample Igneous Robot.

## Notes

Igneous projects are the only projects current described in this documentation, and the only projects currently found in the code. You will need to figure out other systems yourself - there may be useful information spread throughout this document. You may also contact the Team 1540 maintainer (see the first section).

## Creating a new Igneous project

Right-click the 'Sample Igneous Robot' project and select 'Copy...'

Enter a new project name instead of 'Sample_Igneous_Robot'.

Currently, it is recommended to leave the project folder as the folder where you checked out the CCRE to. This should be the default value. This may be modified in the future, but if you do, you will need to update linking to the other projects.

Hit 'Copy' to create your project.

You may need to manually rename the project itself to your new name at this time.

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

You will want to replace these port numbers with the correct port numbers if you want to run this on a real program!

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

Once you want to test your code in the real world, and when you want to actually drive your robot using your code, follow these instructions. 

If you used the drive code tutorial above, you will want to ensure that the motor ports are the appropriate motors ports on the real robot.

Connect to the robot's wireless network.

Hit 'Run' in your main robot project - this should download the new robot code and reboot the cRIO.

At this point, everything is done exactly as if you had written normal FRC code. Plug in joysticks to your driver station, and enable the robot. You can now watch your code working on your real robot!

# System overview for users

The CCRE and Igneous contain very large amounts of code, and it would not be worth your time to understand all of the internals. Below are overviews of all the relevant parts of the CCRE. More detailed information, for those attempting to modify the CCRE, can be found at the end of the overall documentation.

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

Because of the relative difficulty in creating an implementation of InputProducers, a Status object (BooleanStatus or FloatStatus) is recommended instead of a custom implementation of InputProducer.

#### Inputs

An Input is an interface that combines an InputPoll and an InputProducer. Therefore, you can assign to an InputPoll or InputProducer from an Input.

### CCRE Logging

The CCRE has a relatively comprehensive logging system, that supports sending messages with option exceptions attached to the console, other systems on the network, and anywhere else.

#### Logging Levels

A Logging Level, represented by the psuedo-enumeration ccre.log.LogLevel, represents the importance of a logged message. This is useful because in some cases, messages below a specified importance will not be shown.

    LogLevel.SEVERE:  A severe error. This usually means that something major didn't work, or an impossible condition occurred.
    LogLevel.WARNING: A warning. This usually means that something bad happened, but most things should probably still work.
    LogLevel.INFO:    A piece of info. This usually means something happened that the user might want to know.
    LogLevel.CONFIG:  A piece of configuration information. This usually means something that isn't really important, but is something triggered by configuration instead of normal operation.
    LogLevel.FINE:    A top-level debugging message. This can be caused by anything, but probably shouldn't be logged particularly often.
    LogLevel.FINER:   A mid-level debugging message. This can be caused by anything, and can be logged relatively often.
    LogLevel.FINEST:  A low-level debugging message. This can be caused by anything, and might be called many times per second.

#### Logging

To log a message, the class Logger should generally be used. Logger contains quick methods to log simple messages under any logging level. For example

    Logger.finest("Current value of x: " + x);
    
    Logger.info("I am aliiiiiiiive!");
    
    Logger.severe("Illegal command received! Now calling emergency services.");

As well, these can be logged with a long-form logging method:

    Logger.log(LogLevel.CONFIG, "Reset PID tuning to defaults.");

A throwable (as in, Exception's superclass) can also be included. For example:

    try {
        monitorNuclearReactor();
    } catch (CoreMeltdownException e) {
        Logger.log(LogLevel.SEVERE, "Nuclear meltdown imminent!", e);
    }
    // Disclaimer: do not use this system to run a real nuclear reactor. it's not reliable enough.

### Cluck

Cluck is the name for the subsystem that allows for easy out-of-FRC-band network communication. All objects needed for Cluck communication are created automatically in an Igneous application.

The most useful object for Igneous applications is the CluckEncoder object found in CluckGlobals.encoder

A CluckEncoder allows for channels and other objects to be published for the use of other networked applications and then subscribed to from those applications.

Here is a partial list of what can be published:

    ccre.chan.BooleanInputProducer
    ccre.chan.BooleanOutput
    ccre.chan.FloatInputProducer
    ccre.chan.FloatOutput
    ccre.event.EventSource
    ccre.event.EventConsumer

Here is a short partial usage example of Cluck:

    // Node 1
    FloatInputProducer curTime = ...;
    CluckGlobals.encoder.publishFloatInputProducer("currentTime", curTime);
    
    // Node 2
    FloatInputProducer curTimeOnNode1 = CluckGlobals.encoder.subscribeFloatInputProducer("currentTime");
    ... do something with curTimeOnNode1 ...

### CCRE Networked Tuning

Cluck Networked Tuning is a system that allows for easy tuning of parts of a robot to new values, including saving tuning values.

Here is a simple example of tuning:

    public class TuningExample {
        public static final TuningContext armTuningContext = new TuningContext(CluckGlobals.encoder, "arm_tuning").publishSavingEvent("Arm Tuning");
        public static final FloatStatus armPickupPreset = armTuningContext.getFloat("arm pickup preset", 4.22f);
        
        public static float getTuning() {
            return armPickupPreset.readValue();
        }
    }

Let's break down this example:

        public static final TuningContext armTuningContext

The declaration for the variable that will contain the tuning context. A tuning context can be thought of as a collection of all the related things that could be kept track of. You could use one for the entire robot, or separate contexts for different subsystems.

    armTuningContext = new TuningContext(CluckGlobals.encoder, "arm_tuning")

Here the context is created. There are two main options - the encoder to use, which should be CluckGlobals.encoder unless you have a better idea, and the name of the context. The name is used as the storage key, and must only contain alphanumeric characters, underscore (_), and the currency symbol ($). If you change the name, then settings will be lost.

    .publishSavingEvent("Arm Tuning");

publishSavingEvent(String) will publish an event on the network to allow for saving the tuning settings. Without this, you will need to manually call .flush() on the context when you want to save the tuning settings. With this, there will be an option shared over the network to allow for saving with this name.

    FloatStatus armPickupPreset = armTuningContext.getFloat("arm pickup preset", 4.22f);

This creates a FloatStatus that contains the current value of the tuning. It will always contain the current value of the tuning, and can be read and modified like any other FloatStatus, but will also send new values across the network to any tuning panel, and allow for the value to be changed from across the network.

"arm pickup preset" is the name of the property to tune. It should be different for different properties to prevent confusion within the system.

4.22f is the default value - what is used if nothing is saved. If this value is modified in the source code, it will also modify the saved value on the robot once the code is ran, so you can also tune values from the source code as well as from a tuning panel.

    armPickupPreset.readValue()

This snippet of code will read the current value of the tuning parameter. Generally, this is what you will be doing with tuning parameters.

### Joysticks

Joysticks in general can be found on any Igneous platform.

There are two interfaces for Joysticks - ISimpleJoystick and IDispatchJoystick.

ISimpleJoystick contains pollable values for all axes, including special methods for the X and Y axes, and also pollable values for all buttons.

See the API reference for the relevant method names.

IDispatchJoystick extends ISimpleJoystick and adds features that are dispatched instead - so, FloatInputs instead of FloatInputPolls, and EventSources that are produced once for each time that a button is pressed.

See the API reference for the relevant method names.

## Specific CCRE classes for implementation assistance.

This section contains more specific sections of the CCRE that are useful but not as important for using the CCRE.

### Channel Statuses

Often, InputProducers need to be implemented, but they require handling of a list of listeners. To complete this functionality easily, Status objects exist: `FloatStatus` and `BooleanStatus`.

These are both Inputs and Outputs, so you can write values to them and the values will be sent to all listeners.

For more information, including a number of convenience methods, see the API reference.

### CArrayUtils

CArrayUtils is a utility class for arrays and for CCollections.
Quick overview of features (see javadoc for more details):

    CList CArrayUtils.EMPTY_LIST - A CList that is always empty.
    CList<T> CArrayUtils.getEmptyList() - Get an empty CList with elements of type T
    Object[] CArrayUtils.copyOf(Object[], int) - Make a new copy of the given array, with the copy being of the specified length. Extra elements are filled with nulls, and missing elements are discarded.
    CList<T> CArrayUtils.asList(T...) - Create a new fixed-length list from the given array.

### Concurrency

The CCRE contains a handful of very useful classes to facilitate concurrency programming.

#### ccre.concurrency.ReporterThread

ReporterThread provides a very easy-to-use implementation of Thread that allows for simple threads to be created without a number of pitfalls.

Example usage:

    new ReporterThread("TestThread") {
        protected void threadBody() throws Throwable {
            // Do work.
        }
    }.start();

This will create and start a new thread, automatically doing work of naming the thread (the name is autonumbered based on the name passed to the constructor), work of ensuring that threadBody() is not called multiple times or from outside of the thread, and catching any exceptions thrown from the thread body and reporting them via the CCRE's logging functionality.

#### ccre.concurrency.CollapsingWorkerThread

CollapsingWorkerThread is a special-purpose worker thread for the case when you want a once-at-a-time long-running action to not clog up other threads.

This is used by the Poultry Inspector to reconnect to the robot. When the reconnect button is pushed, it starts reconnencting in a different thread, and if reconnect is pushed during that time, it will be ignored.

See the javadoc for more information.

#### ccre.concurrency.ConcurrentDispatchArray

A concurrent collection that allows concurrent iteration and removal without concurrency errors. The values returned by an iterator are the values that were in the iterator when the iterator was started. This is implemented by copying the entire array when a modification operation is completed.

This performs like an ArrayList except that there is no upper bound of time (although it will be finite) when modification operations are ran.

#### ccre.util.ExpirationTimer

An ExpirationTimer acts sort of like an alarm clock. You can schedule a series of alarms with certain delays, and then start the timer. When each delay passes, the timer will trigger the event associated with the delay. The timer can be fed, which restarts the timer (can be used to implement WatchDogs, hence the name). The timer can also be stopped, which resets the timer and prevents it from running until it is started again.

See the javadoc for more information.

### Mixing, DriverImpls

Mixing and DriverImpls provide a large number of prefabricated components for mixing together various rudimentary channels.

DriverImpls currently contains various pieces of tank-drive code, but will in the future contain more kinds, such as single-joystick drive code (and possibly swerve drive code if someone would like to contribute that)

The full listing of the provided methods can be found by looking in the javadoc, but here are some of the kinds of methods found in Mixing:

* always(float):FloatInput - Get a FloatInput that always has the specified value.
* andBooleans(BooleanInputPoll...):BooleanInputPoll - Get a BooleanInputPoll that is true iff all of the specified BooleanInputPolls are true.
* orBooleans(BooleanInputPoll...):BooleanInputPoll - Get a BooleanInputPoll that is true iff any of the specified BooleanInputPolls are true.
* booleanSelectFloat(...):? - A category of methods that select from two float values (Outputs or InputPolls) based on a boolean value.
* combineBooleans(BooleanOutput, BooleanOutput[, BooleanOutput]):BooleanOutput - A BooleanOutput that sends its values to all of the specified outputs.
* combineFloats(FloatOutput, FloatOutput[, FloatOutput]):FloatOutput - A FloatOutput that sends its values to all of the specified outputs.
* combineEvents(EventConsumer...):EventConsumer - An EventConsumer that fires all of the specified EventConsumers when fired.
* createDispatch(?InputPoll, EventSource):?Input - Converts an InputPoll into an Input by dispatching new values when the specified EventSource is produced.
* deadzone(...):? - A category of methods that apply a deadzone to any kind of Float channel.
* filterEvent(BooleanInputPoll, boolean, EventSource|EventConsumer):EventSource|EventConsumer - Two methods that allow an EventSource or EventConsumer to pass through only when the specified BooleanInputPoll is the same as the specified boolean.
* floatIsAtLeast|floatIsAtMost(FloatInputPoll, float):BooleanInputPoll - Two methods that allow for simple comparison of a FloatInputPoll with a constant float.
* floatIsInRange|floatIsOutsideRange(FloatInputPoll, float, float):BooleanInputPoll - Two methods that allow for checking if a FloatInputPoll is within (or outside) a constant range.
* floatsEqual(FloatInputPoll, FloatInputPoll):BooleanInputPoll - Create a channel that represents when the specified FloatInputPolls are equal.
* invert(...):? - A category of methods that logically invert any Boolean channel.
* negate(...):? - A category of methods that negate any Float channel.
* normalizeFloat(FloatInputPoll, float zero, float one):FloatInputPoll - Provides a scaled version of the specified input, such that when the value from the specified input is the value in the one parameter, the output is 1f, and when the value from the specified input is the value in the zero parameter, the output is 0f.
* pumpWhen(EventSource, ?InputPoll, ?Output) - When the specified event is produced, read the value from the InputPoll and write it to the Output.
* pumpEvent(?InputPoll, ?Output):EventConsumer - An EventConsumer that, when fired, reads the value from the InputPoll and writes it to the Output.
* triggerWhenBooleanChanges(EventConsumer toFalse, EventConsumer toTrue):BooleanOutput - When the last value written to the returned BooleanOutput becomes true, fire the toTrue event. When it becomes false, fire the toFalse event.
* whenBooleanBecomes(BooleanInputProducer, boolean):EventSource - Returns an EventSource that, when the specified BooleanInputProducer becomes the specified boolean, will be produced.
* whenBooleanBecomes(BooleanInputPoll, boolean, EventSource):EventSource - Similar to the previous method, but checks when the specified EventSource is fired.

Mixing also contains a handful of constants:
* alwaysFalse - a BooleanInput that is always false.
* alwaysTrue - a BooleanInput that is always true.
* ignoredBooleanOutput - a BooleanOutput that ignores anything written to it.
* ignoredFloatOutput - a FloatOutput that ignores anything written to it.

### MultipleSourceBooleans

MultipleSourceBooleans is a controller that combines a series of registered BooleanOutputs or BooleanInputPolls or BooleanInputProducers or BooleanInputs to create a single output or input line, based on the logical AND or OR of all the values.

    MultipleSourceBooleanController shouldSelfDestruct = new MultipleSourceBooleanController(MultipleSourceBooleanController.AND);
    valve.addTarget(destructionOutput);
    valve.addInput(operatorKeyAlpha);
    valve.addInput(operatorKeyBeta);
    value.addInput(Mixing.invert(operatorKeyFailsafe));
    // This only self destructs if both operatorKeyAlpha and operatorKeyBeta are true, and operatorKeyFailsafe is false.
    // Disclaimer: You probably shouldn't use the CCRE to control a self-destruct.

### General Networking

If you want to use standard TCP Sockets, you'll probably run into the problem that you can't write one piece of code that works on all CCRE platforms. Well, now you can! (This is used internally by Cluck)

#### Opening a socket server

    ServerSocket sock = Network.connect(22);
    while (!cancelProcess()) {
        ClientSocket conn = sock.accept();
        InputStream istr = conn.openInputStream();
        OutputStream ostr = conn.openOutputStream();
        // Provide world-domination service.
        conn.close();
    }
    sock.close();

See the javadoc for more details. (Note: error handling not included in this example)

#### Opening a socket connection

    ClientSocket sock = Network.connect("10.15.40.2", 22);
    InputStream istr = sock.openInputStream();
    OutputStream ostr = sock.openOutputStream();
    // Achieve world domination using the connection.
    sock.close();

See the javadoc for more details. (Note: error handling not included in this example)

#### Listing local IPv4 addresses

    CCollection<String> addrs = Network.listIPv4Addresses();
    // Find servers using the local addresses.

For example, this might contain `["127.0.0.1", "10.15.40.2"]` on a robot.

See the javadoc for more details.

### PhidgetReader

The PhidgetReader is a small interface class that is used to access a Phidget control panel from a robot. Currently, the other end of the connection is provided by the Poultry Inspector. This uses Cluck.

Currently, this is hardcoded to be useful for only a specific number of inputs and outputs.

Available parts of the Phidget:

    BooleanOutput[8] digitalOutputs
    BooleanInput[8] digitalInputs
    FloatInput[8] analogInputs
    StringHolder[2] lcdLines

### Data storage

The CCRE has a generic system for saving key/value data in a system-specific manner, which allows for the same code on both Java SE and Squawk platforms.

Example usage:

    StoragSegment seg = StorageProvider.openStorage("test_storage")
    String name = seg.getStringForKey("robot name");
    if (name == null) {
        name = "Unknown";
    }
    Logger.config("Robot name: " + name);
    seg.setStringForKey("robot name", getNewRobotName());
    seg.close();

Here is the breakdown of the code:

    StorageProvider.openStorage("test_storage")

This opens up the storage segment delimited by the specified name.
Storage names must only contain alphanumeric characters, underscore (_), and the currency symbol ($).

    seg.getStringForKey("robot name")

This fetches the value associated with the specified key name. This is equivalent to `new String(seg.getBytesForKey("robot name"))` except that it also handles empty keys by returning null instead of throwing an error.

    seg.setStringForKey("robot name", getNewRobotName())

This is the opposite of the previous operation - it saves the specified string to the specified key. It is equivalent to `seg.setBytesForKey("robot name", getNewRobotName().getBytes())`.

    seg.close();

This closes the segment so that it can no longer be used, including flushing data to disk. Flushing data without closing the segment can be performed by `seg.flush();`.

**WARNINGS**:

If `seg.flush()` or `seg.close()` is not called, then the data will most likely not be saved!

`seg.getBytesForKey` and `seg.setBytesForKey` assume that the specified byte array will NOT be modified! Behavior is undefined if it is. If you are unsure, make a new copy of the array first!

If you use `seg.setDataOutputForKey`, make sure that you call `.close()` on it! Otherwise, the value will not be saved!

See the API reference for more information.

### Throwable printing

Java ME does not contain any way to log information about an error except to the standard output stream. If you want any more Throwable data than the message, you are SOL. The CCRE contains a general interface for printing that includes very special handling for Squawk (messing around with raw byte data), so that Throwable data can be sent anywhere, without caring about the platform.

How to use:

    try {
        ...
    } catch (Exception ex) {
        ThrowablePrinter.printThrowable(ex, myPrintStream);
    }

This will print the data for Exception to the specified output stream.

    try {
        ...
    } catch (Exception ex) {
        String errorOut = ThrowablePrinter.toStringThrowable(ex);
        ... // work with errorOut
    }

This allows for getting a string for a throwable, such that `myPrintStream.print(ThrowablePrinter.toStringThrowable(ex))` is equivalent to `ThrowablePrinter.printThrowable(ex, myPrintStream)`.

### Utils class

The CCRE contains a `Utils` class for miscellanious utilites that do not fit anywhere else.

    Utils.deadzone(value, deadzone)

This applies a deadzone to the specified value. If `-deadzone<=value<=deadzone`, then the result will be zero. Otherwise, it will be the value. This is used, for example, to handle joysticks where the value is never exactly zero.

    FloatInputPoll: Utils.currentTimeSeconds

This represents the difference, measured in seconds, between the current time and midnight, January 1, 1970 UTC. This may be accurate to the millisecond, depending on the system.

## Igneous classes

As you know, Igneous is the subsystem that provides access to a FRC robot.

See the Getting Started guide earlier in this document for how to create an Igneous project.

The main class for an Igneous application always extends IgneousCore directly or indirectly. Usually, it extends SimpleCore, which itself extends IgneousCore, and adds the four joysticks as objects.

Here is the list of functionality explicitly provided by Igneous:

### Joysticks

    IDispatchJoystick joystick1, joystick2, joystick3, joystick4

See the above section on generic CCRE joysticks for how to use these joysticks. These joysticks update their values exactly when the duringTeleop event is fired - see below.

If you are using IgneousCore directly, the following methods can be used to get the same joysticks:

    IDispatchJoystick makeDispatchJoystick(int id)

Get a dispatch joystick for the specified joystick ID (1-4).

    IDispatchJoystick makeDispatchJoystick(int id, EventSource when)

This is the same, but it updates when the specified event is fired instead of duringTeleop.

    ISimpleJoystick makeSimpleJoystick(int id)

Get a simple joystick for the specified joystick ID (1-4).

### Initialization Events

When any mode is entered - teleop, autonomous, disabled, testing - the corresponding init EventSource is produced:

    startedAutonomous
    startedTeleop
    robotDisabled
    startedTesting

### Periodic Events

While any mode is in progress, the corresponding periodic event will be produced every ~20 ms (more specifically, as fast as signals come from the driver station).

    duringAutonomous
    duringTeleop
    duringDisabled
    duringTesting

An additional periodic event is fired regardless of the mode:

    globalPeriodic

### Motors

The CCRE supports three kinds of Speed Controllers: Talons, Jaguars, and Victors.

The following methods are provided to get access to motors:

    FloatOutput makeTalonMotor(int id, MOTOR_FORWARD or MOTOR_REVERSE)
    FloatOutput makeJaguarMotor(int id, MOTOR_FORWARD or MOTOR_REVERSE)
    FloatOutput makeVictorMotor(int id, MOTOR_FORWARD or MOTOR_REVERSE)

These methods take a PWM port ID and whether or not to reverse the direction of the motor. They return a FloatOutput that can be written to in order to change the speed of the motor (-1.0f to 1.0f)

### Solenoids

Solenoids can be accessed using one method:

    BooleanOutput makeSolenoid(int id)

This gets a reference to the solenoid on the specified port, and the solenoid can be turned on and off by writing a value to the returned BooleanOutput.

### Analog Inputs

Analog Inputs can be accessed using one of two methods:

    FloatInputPoll makeAnalogInput(int id, int averageBits)
    FloatInputPoll makeAnalogInput_ValueBased(int id, int averageBit)

ID is the analog port to read. averageBits is the number of averaging bits for the FPGA to use. This should be higher if the value isn't accurate enough, and lower if the value updates too slowly. Team 1540 used a value of 9 for reading our arm's position via a potentiometer, and a value of 14 for reading the current tank pressure.

`makeAnalogInput` gives the current value as a voltage.

`makeAnalogInput_ValueBased` gives the current value as a 12-bit uncalibrated value (so this value will be 0.0 - 4095.0)

### Digital Inputs

Digital Inputs can be accessed using one method:

    BooleanInputPoll makeDigitalInput(int id)

This represents the current value of the specified GPIO line.

### Servos

Servos can be accessed using one method:

    FloatOutput makeServo(int id, float minInput, float maxInput)

ID represents the port number for the servo.

`minInput` and `maxInput` are the range for the values sent to the FloatOutput.

If the value is the same as minInput, the servo will be in its minimum position, and if the value is the same as maxInput, the servo will be in its maximum position.

### Driver station LCD

There are currently two methods for displaying information on the driver station LCD:

    FloatOutput makeDSFloatReadout(String prefix, int line)
    void makeDSFloatReadout(String prefix, int line, FloatInputPoll value, EventSource when)

The first method, when the FloatOutput has a value written to it, will write the prefix followed by ": " followed by the written value to the specified line (1-6) on the driver station.

The second method is similar, but updates the specified value when the specified EventSource is produced.

### Robot status

There are a handful of methods describing the current state:

    BooleanInputPoll getIsDisabled()
    BooleanInputPoll getIsAutonomous()

These represent whether or not the robot is disabled, or autonomous, respectively.

### Compressors

The CCRE has some easy-to-use systems for running compressors:

    useCompressor(int pressureSwitchChannel, int compressorRelayChannel)
    useCustomCompressor(BooleanInputPoll shouldDisable, int compressorRelayChannel)

The first method is for standard compressor setups. Call it with the channels for the pressure switch and compressor relay, and it will handle everything.

The second method is for when a custom pressure switching syste is used - for example if an analog pressure sensor is used, and calculations from it decide whether or not to run the compressor.

## Next steps

This completes all of the important reference information.

The javadoc contains the rest of the important information.

The rest of this document will be (TODO: make it be) more detailed information on how the CCRE works internally for anyone who wants to maintain it.

# System overview for maintainers

INCOMPLETE
