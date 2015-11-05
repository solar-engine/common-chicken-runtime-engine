The Common Chicken Runtime Engine
=================================

The CCRE solves the problem of writing elegant and maintainable robot software
by using a dataflow model and taking care of the infrastructure for your
project so that you can focus on the important parts of your code.

Here's an example of a robot piloted with Tank Drive:

    DriverImpls.arcadeDrive(FRC.joystick1,
                            FRC.talon(1, FRC.MOTOR_FORWARD),
                            FRC.talon(2, FRC.MOTOR_REVERSE));

Or, something more interesting: an example of a shifting drive train:

    BooleanOutput shifter = FRC.solenoid(2);
    shifter.setFalseWhen(FRC.startTele);
    shifter.setTrueWhen(FRC.joystick1.onPress(3));
    shifter.setFalseWhen(FRC.joystick1.onPress(1));

Features
--------

- Dataflow-based programming
- Modern framework for FRC robot code
- An emulator to test robot code without a robot
- A high-quality publish-subscribe networking system
- Robust error handling
- No dependency on WPILib plugins

Documentation and Quickstart Guide
-----------------------------------

Please read [our documentation and quickstart guide](http://cgscomwww.catlin.edu/~skeggsc/ccre-docs/)!

Contribute
----------

- Issue Tracker: github.com/flamingchickens1540/Common-Chicken-Runtime-Engine/issues
- Source Code: github.com/flamingchickens1540/Common-Chicken-Runtime-Engine

Support
-------

If you are having issues, please let us know. You can reach the primary
developer at: robotics [at] colbyskeggs [dot] com

License
-------

The project is licensed under the LGPL, which means that you can freely use it
as a dependency of your project without affecting the license that you use for
your project.

Since this project is publicly available, you can use it for FRC robot software
legally under the rules (as of 2015), as long as, if you make any changes
before Kickoff, you make your changes available publicly. That's because of the
FRC rules, not the license. The license does mean that if you release changes
to this framework itself publicly, you pretty much have to also make those
changes available under the LGPL.

