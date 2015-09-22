#lang scribble/manual
@require[pict]

@title{The Common Chicken Runtime Engine v3.0.0-pre1}

The CCRE solves the problem of writing elegant and maintainable robot software
by using a dataflow model and taking care of the infrastructure for your
project so that you can focus on the important parts of your code.

Here's an example of a robot piloted with Tank Drive:

@codeblock|{
    DriverImpls.arcadeDrive(Igneous.joystick1,
                            Igneous.makeTalonMotor(1, MOTOR_FORWARD),
                            Igneous.makeTalonMotor(2, MOTOR_REVERSE));
}|

Or, something more interesting: an example of a shifting drive train:

@codeblock|{
    BooleanOutput shifter = Igneous.makeSolenoid(2);
    shifter.setFalseWhen(Igneous.startTele);
    shifter.setTrueWhen(Igneous.joystick1.onPress(3));
    shifter.setFalseWhen(Igneous.joystick1.onPress(1));
}|

@author{Colby Skeggs <robotics [at] colbyskeggs [dot] com>}

Features:

@itemlist[@item{Dataflow-based programming}
          @item{Modern framework for FRC robot code}
          @item{An emulator to test robot code without a robot}
          @item{A high-quality publish-subscribe networking system}
          @item{Robust error handling}
          @item{No dependency on WPILib plugins}]

Here's what you'll find in this document:

@table-of-contents{}

@section{Quickstart Guide}

@subsection{Prerequisites}

@itemlist[@item{You have to know how to program in Java. (You don't have to know all of the ins-and-outs of Java, but you have to have a solid basic understanding.)}
          @item{You have to have the latest version of @hyperlink["http://www.eclipse.org/"]{Eclipse} installed.}
          @item{You need to have the Java 8 JDK (Java Development Kit) installed.}]

@subsection{Installing the CCRE}

Short version: import all of the projects from the CCRE repository into Eclipse and Build All.

Long version:

@itemlist[@item{Download the latest CCRE release either as a @hyperlink["https://github.com/flamingchickens1540/Common-Chicken-Runtime-Engine/archive/ccre-v3.0.0-pre1.tar.gz"]{tar.gz} or a @hyperlink["https://github.com/flamingchickens1540/Common-Chicken-Runtime-Engine/archive/ccre-v3.0.0-pre1.zip"]{zip} depending on your system. You can also @link["https://github.com/flamingchickens1540/Common-Chicken-Runtime-Engine/releases"]{see all downloads here}.}
          @item{Extract that archive into a directory of your choice.}
          @item{Open Eclipse and select a new workspace. (Selecting a new workspace is optional if this is your first installation of the CCRE.) @image["workspace.png"]{new workspace}}
          @item{Go to File -> Import... and select Existing Projects into Workspace. @image["import.png"]{importing}}
          @item{Choose the directory where you extracted the archive to. @image["importprojects.png"]{projects}}
          @item{Select all of the CCRE projects and press finish. @image["importexisting2.png"]{finish}}
          @item{Press Project -> Build All.

                @image["buildall.png"]{build all}}]

@subsection{Creating a new project}

Creating a new project is easy.

Simply copy and paste the TemplateRobot project, and give it a new name like MyFirstRobot.

@image["copy-template.png"]{copy}

@image["paste-template.png"]{paste}

@image["paste-myfirstrobot.png"]{name}

Open src/robot/RobotTemplate.java in your new project.

@image["robottemplate-java.png"]{RobotTemplate.java}

Replace 0000 with your team number.

@image["robottemplate-teamnum.png"]{Team Number}

Now press Project -> Build Project, and once it finishes, you're done!

@margin-note{After the first time, rebuilding the project is taken care of for you.}

@image["buildproject.png"]{Build Project}

@subsection{Basic code}

We'll start with something simple. You should see this code in your project:

@codeblock|{
  @Override
  public void setupRobot() {
      // Robot setup code goes here.
  }
}|

Start by adding @codeblock|{
    Logger.info("Hello, World!");
}| to this method.

Now, let's test this code in the emulator. Open the dropdown next to the External Tools button and select "MyFirstRobot Emulate."

@image["emulate.png"]{emulate}

If the emulator doesn't appear properly, close it and rebuild the entire CCRE by pressing Project -> Clean... -> Clean All Projects -> OK and then Project -> Build All.

On the emulator window that pops up, you should see it say something like
@codeblock|{
    [INFO] (RobotTemplate.java:20) Hello, World!
}|

Example:

@image["helloworld.png"]{hello world}

If so, congratulations! You appear to have installed the CCRE correctly and written a very simple program!

Of course, that's not particularly interesting, so let's move on to actual motors and joysticks.

@subsection{Single Joystick, Single Motor}

Let's set up a Talon speed controller and control it with the Y axis of a Joystick.

@codeblock|{
  FloatOutput motor = FRC.makeTalonMotor(0, FRC.MOTOR_FORWARD);
  FloatInput yAxis = FRC.joystick1.axisY();
  yAxis.send(motor);
}|

You will need to import all of the classes referenced by this piece of code. Eclipse has an easy way to do this:

@image["hover-error.png"]{hover error}

@margin-note{You can also press Control+1 while your (keyboard) cursor is over the error to pop up the window faster.}

@image["fix-error.png"]{fix error}

Import the rest of the missing classes and then you can run your program in the emulator!

@image["emulator-disabled.png"]{disabled emulator}

@margin-note{X = 1, Y = 2.} Now, let's try your program. Click on DISABLED to toggle it to ENABLED and then drag the bar under Axis 2 to
change the Y axis in your program.

You could try to drag this while the robot is DISABLED, but the motor wouldn't be able to change.

@image["emulator-enabled.png"]{enabled emulator}

@subsection{Running your code on a real robot}

(If you don't want to run your code on a real robot yet, skip this section and come back later.)

@margin-note{The kinds of Speed Controllers are: @itemlist[@item{Talons (latest)}
                                                           @item{Jaguars}
                                                           @item{Victors (oldest)}]}

For the current example, the motor port number was chosen arbitrarily: a Talon on PWM port 0.
However, your real robot might vary from this. You should figure out which motor you want to run on your robot,
and figure out its port number and which kind of speed controller it uses.

@margin-note{It's possible that your robot uses CAN speed controllers. Try to choose motors that aren't connected with CAN, as
CAN motors are harder to deal with. Don't worry, we'll get to CAN motors later.}

Once you know which kind of speed controller you're using (Talon, Victor, Jaguar), you may need to replace
@code{makeTalonMotor} with @code{makeVictorMotor} or @code{makeJaguarMotor}. You may also need to replace the @code{0}
with the correct port number.

Once you have the configuration correct, open the dropdown next to the External Tools button and select "MyFirstRobot Deploy."

@image["deploy.png"]{deploy}

Make sure that the output ends with @code{BUILD SUCCESSFUL}, and then you can connect your Driver Station to the robot, connect a Joystick, enable your robot, and move the motor by moving the Joystick!

TODO: troubleshooting guide.

Congratulations! You now know how to use the basics of the CCRE!

@section{Introduction to Dataflow programming}

When thinking about how a robot should react to stimuli, often the mental model that you generate is about the flow of data:

@define-syntax-rule[(pict-wrap xes ...) (let () xes ...)]
@define-syntax-rule[(defines (name ...) value ...)
                    (define-values (name ...)
                      (values value ...))]
@define[(cell txt) (let ((gen-text (text txt)))
                     (cc-superimpose (rectangle (+ (pict-width gen-text) 40) 40) gen-text))]
@define[(harrows-i base pict-pairs) (if (null? (cdr pict-pairs)) base
                                       (pin-arrow-line 10 (harrows-i base (cdr pict-pairs))
                                                       (car pict-pairs) rc-find
                                                       (cadr pict-pairs) lc-find
                                                       #:line-width 2))]
@define-syntax-rule[(harrows base (picts ...) ...) (let loop ((cur base) (rest (list (list picts ...) ...)))
                                                     (if (null? rest)
                                                         cur
                                                         (loop (harrows-i cur (car rest)) (cdr rest))))]
@define[(varrows-i base pict-pairs) (if (null? (cdr pict-pairs)) base
                                        (pin-arrow-line 10 (varrows-i base (cdr pict-pairs))
                                                        (car pict-pairs) cb-find
                                                        (cadr pict-pairs) ct-find
                                                        #:line-width 2))]
@define-syntax-rule[(varrows base (picts ...) ...) (let loop ((cur base) (rest (list (list picts ...) ...)))
                                                     (if (null? rest)
                                                         cur
                                                         (loop (varrows-i cur (car rest)) (cdr rest))))]
@define[(indent x n) (hc-append (blank n 40) x)]

@define[ccre-intro-model
        (let ((pict-a (cell "When the red button is pressed"))
              (pict-b (cell "and the key is turned"))
              (pict-c (cell "blow up the world")))
          (harrows (hc-append 75 pict-a pict-b pict-c)
                   (pict-a pict-b pict-c)))]

@ccre-intro-model

With traditional methods, you might think to implement that control system like this:

@vl-append[20
 (cell "white true")
 (indent (cell "if red button is pressed") 40)
 (indent (cell "if key is turned") 80)
 (indent (cell "blow up the world") 120)
 (indent (cell "sleep for 20 milliseconds") 40)]

However, this technique is really easy to get wrong. That example has the bug where if the red button is pressed for very long - more than 20 milliseconds, which isn't very long - then the program will blow up the world multiple times!

Yes, that can be fixed, but the code gets more complicated:

@margin-note{This is hard to scale for multiple reasons, including an inability to have @italic{inline state}. For each button, you need a variable defined in a completely different location in the file (which becomes more significant with larger files) and you have to reference multiple places to figure out what your code does.}

@vl-append[20
 (cell "define variable was_button_pressed")
 (cell "white true")
 (indent (cell "if red button is pressed AND not was_button_pressed") 40)
 (indent (cell "if key is turned") 80)
 (indent (cell "blow up the world") 120)
 (indent (cell "set was_button_pressed to whether or not the button is currently pressed") 40)
 (indent (cell "sleep for 20 milliseconds") 40)]

With the number of things you have to think about in a practical system, this quickly becomes an ineffective strategy: you can do it, but you will probably have a hard time keeping it understandable.

The CCRE solves this by aligning the code more closely with the original model. Recall what we had earlier:

@ccre-intro-model

The CCRE would express this as:

@margin-note{This is slightly simplified, but not by much.}

@let[((red (cell "red button"))
              (is (cell "becomes pressed"))
              (key (cell "key is turned"))
              (and (cell "and"))
              (blow (cell "blow up the world")))
     (harrows (vl-append 40
                         (hc-append 75 red is and blow)
                         (indent key 206))
              (red is and blow)
              (key and))]

Or, in practice:

@codeblock|{
            red_button.onPress().and(key_input).send(blow_up_world);
      }|

And that's the reason that we built the CCRE.

@subsection{Channels}

The CCRE is built around the concept of "channels."
A channel has an implementation that handles one side of the channel,
and any number of users that talk to the implementation over the channel.
Neither side has to know much about the details of the other side.

There are six kinds of channels, along two axes.

@tabular[(list (list "" "Event" "Boolean" "Float")
               (list "Input" (bold "EventInput") (bold "BooleanInput") (bold "FloatInput"))
               (list "Output" (bold "EventOutput") (bold "BooleanOutput") (bold "FloatOutput")))
         #:style 'boxed
         #:column-properties '((left-border right-border) right-border right-border right-border)
         #:row-properties '(bottom-border bottom-border bottom-border)]

With an Output, the users of the channel can send messages over the channel to the implementation behind it.

@itemlist[@item{@bold{EventOutput}: messages have no associated data.
           They simply represent the request that the implementor should cause a defined thing to happen.
           We call this "firing" the EventOutput.

           @codeblock{start_driving_forward.event(); // start the robot driving forward}
          }
          @item{@bold{BooleanOutput}: messages have an associated value of true or false.
           The message represents the request that the implementor change something to a state representable by a binary choice.

           @codeblock{light_bulb.set(false); // turn off the light bulb}
           @codeblock{light_bulb.set(true); // turn on the light bulb}
          }
          @item{@bold{FloatOutput}: messages have an associated real number value - often, but not always, in the range of -1.0 to 1.0.
           The message represents the request that the implementor change something to some potentially intermediate state.

           @codeblock{motor.set(0.0f); // stop the motor}
           @codeblock{motor.set(1.0f); // run the motor clockwise at full speed}
           @codeblock{motor.set(-1.0f); // run the motor counterclockwise at full speed}
           @codeblock{motor.set(0.6f); // run the motor clockwise at 60% speed}
           }]

With an Input, the users of the channel can request the present state of the channel from the implementation (if any), and ask the implementation to tell them when the value represented by the input changes.

@itemlist[@item{@bold{EventInput}: there is no associated value.
           Users to be notified when something happens - we call this the EventInput being either fired or produced.

           @codeblock|{
             // when the match starts, start the robot driving forward
             match_start.send(start_driving_forward);
             }|
           @margin-note{Don't actually use @code{System.out.println} for any of these. You'll learn about Logging later, which is a better way to do this.}
           @codeblock|{
              // when the match ends, say so
              match_end.send(() -> System.out.println("Match has ended!"));
              }|
          }
          @item{@bold{BooleanInput}: the associated value is a boolean.
           Users can ask if the current state is true or false, and ask to be told when it changes.

           @codeblock|{
            // as long as the light switch is flipped, turn on the light bulb
            light_switch.send(light_bulb);
           }|
           @codeblock|{
            light_switch.send((is_flipped) ->
                System.out.println("The light switch is flipped: " + is_flipped));
            // whenever the light switch position is changed, say so
           }|
           @codeblock|{
            if (light_switch.get()) {
                // We're wasting power!
            } else {
                // It's too dark in here!
            }
               }|}
          @item{@bold{FloatInput}: the associated value is a real number value - often, but not always, in the range of -1.0 to 1.0.
           Users can ask for the current value, and ask to be told when it changes.

           @codeblock|{
             // control a motor with a joystick axis
             joystick_axis.send(motor);
             }|
           @codeblock|{
            joystick_axis.send((current_position) ->
                System.out.println("The current Joystick position: " + current_position));
            // report the new position of the Joystick whenever someone moves it
           }|
           @margin-note{A number of these examples aren't the right way to do things. Keep reading.}
           @codeblock|{
            if (sewage_level.get() > 1000.0f) {
                // open drainage valve
            }
            }|}]

@subsection{Hardware}

The CCRE contains an interface layer that lets you work with an FRC robot's hardware with these channels.

For example:

@codeblock|{
            BooleanOutput led = FRC.makeDigitalOutput(7);
            FloatOutput test_motor = FRC.makeTalonMotor(3, FRC.MOTOR_FORWARD, 0.2f);
            EventInput start_match = FRC.startTele;
            BooleanInput button = FRC.joystick1.button(3);
            FloatInput axis = FRC.joystick6.axis(3);
}|

See @secref["hardware-access"] below for more info on robot hardware.

@subsection{Remixing channels}

It turns out that, often, you want to do similar things with your channels.
So, correspondingly, all channels have a variety of built-in methods to help you on your journey!

The simplest example is probably @code{send}, which works for all three varieties of channels:

This connects the input to the output, so that @code{y} is updated with the current value of @code{x} both immediately and whenever @code{x} changes.
In the case of events, @code{send} causes the EventOutput to be fired whenever the EventInput is produced.

@codeblock|{
  // this also works if you replace Boolean with Event or Float in both places.
  BooleanInput x = /* ... */;
  BooleanOutput y = /* ... */;
  x.send(y);
  }|

Another simple example is @code{onPress}, which converts a BooleanInput into an EventInput (for when the BooleanInput changes to true):

@codeblock|{
  BooleanInput bumper = FRC.makeDigitalInput(3);
  bumper.onPress().send(stop_motors);
  }|

Also useful is @code{setWhen} (along with @code{setFalseWhen} and @code{setTrueWhen}, and the mirrors @code{getSet*Event}):

@codeblock|{
  driving_forward.setTrueWhen(FRC.joystick1.onPress(2));
  stop_motors = driving_forward.getSetEvent(false);
  }|

See @secref["remixing"] for more info.

@subsection{Status cells}

Sometimes, you want to connect together methods that aren't easy to connect together. For example, you might have:

@codeblock|{
  send_some_data_to_an_output(???);
  do_something_based_on_an_input(???);
  }|

How would you connect these? There's no implementation to provide either end. Luckily, the solution is easy: statuses!

@codeblock|{
   // this also works for EventStatus and FloatStatus, depending on what you're connecting.
   BooleanStatus intermediate_channels = new BooleanStatus();
   send_some_data_to_an_output(intermediate_channels);
   do_something_based_on_an_input(intermediate_channels);
   }|

How is this works is: they have an internal state (at least for BooleanStatuses and FloatStatuses) that can be modified when they are used as an output, and can be monitored and read when they are used as an input. In the case of EventStatuses, when the output side is fired, the input side is produced.

Because they have a persistent value, which is preserved over time, you can have persistent states that you can change.

You can also use them as a named place to exchange data and control between different parts of a program.

@section{Review of advanced Java concepts}

There are a number of features of Java which are heavily used by the CCRE that you might not be familiar with. Below is a quick catalogue of them.

@subsection{Anonymous classes}

Sometimes you might want to implement an Output that does something unique, so you can't use anything built-in. You could put this somewhere else:

@codeblock|{
  public class CatPettingEventOutput implements EventOutput {
    private final Cat cat_to_pet;

    public CatPettingEventOutput(Cat cat_to_pet) {
      this.cat_to_pet = cat_to_pet;
    }

    @Override
    public void event() {
      // pet a cat
    }
  }
  }|

and later:

@codeblock|{
  EventOutput pet_fluffy = new CatPettingEventOutput(fluffy);
    }|

But then the class definition is far away from the actual use, and your code gets clogged up with all of your classes. Clearly, there has to be an easier way:

@codeblock|{
     EventOutput pet_fluffy = new EventOutput() {
       public void event() {
         // pet fluffy
       }
     };
     }|

Useful!

@subsection{Lambdas}

Let's go one step further. In Java 8, there's a new feature called Lambdas which can replace some simple anonymous classes with even shorter code.

Instead of these:

@codeblock|{
     EventOutput pet_fluffy = new EventOutput() {
       public void event() {
         // pet fluffy
       }
     };
     BooleanOutput fluffy_cage_light = new BooleanOutput() {
       public void set(boolean light_on) {
         // turn on or off the light in fluffy's cage
       }
     };
     FloatOutput fluffy_heater_temp = new FloatOutput() {
       public void set(float temperature) {
         // change the thermostat on fluffy's cage
       }
     };
     }|

You can now simply say:

@codeblock|{
     EventOutput pet_fluffy = () -> {
       // pet fluffy
     };
     BooleanOutput fluffy_cage_light = (light_on) -> {
       // turn on or off the light in fluffy's cage
     };
     FloatOutput fluffy_heater_temp = (temperature) -> {
       // change the thermostat on fluffy's cage
     };
     }|

(Also, if you only have a single statement in the lambda, you can omit the @code|{{}}| and the semicolon around the statement.)

@section{The software environment}

There are a number of components that you will be working with.

@table-of-contents{}

@subsection{The Driver Station}

@margin-note{Unfortunately, the DS software only runs on Microsoft Windows.}
The Driver Station is two different things: a piece of software, and the Windows laptop that runs that software.

The Driver Station software connects over the robot's wireless network to the roboRIO (the brain of the robot) and tells it:

@itemlist[@item{If it should be @italic{enabled}}
          @item{What @italic{mode} it should be in}
          @item{The current positions of all @italic{Joysticks}}]

It also talks to the Field Management System (FMS) if you're playing in a real competition.

The software looks like this:

@image["driver-station.png"]

The laptop might looks something like this: (depending on your team)

@image["driver-station-laptop.jpg"]

@margin-note{I'm only showing the left half of the DS - the right half is relatively unimportant.}

Let's go over the tabs available on the DS:

@image["driver-station-annotated.png"]

@itemlist[@item{On the first page, you can select the @italic{mode} of the robot, as well as whether or not it's enabled.}
          @item{On the third page, you can change the team number, which needs to be correct for you to be able to connect properly.}
          @item{On the fourth page, you can see the current plugged-in Joysticks, change their ordering (by dragging them), and see the current inputs on any Joystick by selecting it.}
          @item{And much more, but that's the most important stuff.}]

@subsubsection{Modes}

There are many different conceptualizations of what a "mode" is. The core three are Autonomous Mode, Teleoperated Mode, and Test Mode.

From the robot's perspective, there's also disabled mode. (You can also think of it as Enabled versus Disabled and the three fundamental modes.)

When in disabled mode, nothing on the robot should move. Safe!

From the driver station's perspective, there is also Practice mode, which is useful in theory but not much in practice. (heh.) This mode simply sequences through the other modes in the standard order.

There's also the emergency stop mode, which is entered by pressing the spacebar (in practice) or the physical e-stop button (on the real field.) Once the robot enters emergency stop mode, it is disabled until the robot is physically turned off and on again.

@subsubsection{Keyboard shortcuts}

There are a few important keyboard shortcuts:

@itemlist[@item{The spacebar is the @italic{emergency-stop} button. Once you press it, the robot stops running until you reboot it physically.}
          @item{The enter key is the disable key. It is the same as pressing the disable button on the first tab of the driver station.}
          @item{If you press the keysequence @code|{[]\ }|, the robot will enable. This is the same as pressing the enable button on the first tab of the driver station.}]

Always keep your hand near the disable key when enabling the robot.

@subsubsection{Joysticks}

The driver station can have up to six Joysticks attached to it. Each Joystick is an individual physical device. Examples of Joysticks:

This is a Joystick:

@image["joystick-normal.jpg"]

This is ALSO a Joystick:

@image["joystick-flight.jpg"]

This? Another Joystick. Not two Joysticks - just one.

@image["joystick-xbox.jpg"]

Each Joystick has some number of @italic{axes} - each axis measures a value from -1.0 to 1.0. It also has some number of @italic{buttons} - each button can be either pressed or unpressed.

Standard axes are the position of a Joystick on the forward-backward (Y) axis and the left-right (X) axis.

For example, a trigger on a Joystick is a button, and an altitude control wheel (as on the base of the first Joystick) is an axis.

The X axis on a Joystick is usually axis #1, and the Y axis is usually axis #2.

If you work with something with multiple XY sticks, this may vary. On an xbox controller, for example, this is the left stick, but for the right stick, the X axis is #5 and the Y axis is #6. (And the triggers are #3 (left) and #4 (right.))

@subsection{The match sequence}

Here's how a match goes in normal FRC competitions:

@itemlist[@item{All six teams from both alliances place their robots on the field, powered up.}
          @item{They connect their driver stations to the FMS (field management system.)}
          @item{The FMS takes control of the driver stations, preventing the user from controlling the robot mode.}
          @item{When the match starts, after all robots have powered up and connected properly (or are bypassed if not) and the field personel start the match, the FMS enables each of the robots in autonomous mode.}
          @item{Fifteen seconds later (depending on the game), the robots momentarily change into disabled mode.}
          @item{Either momentarily afterward, or perhaps a few seconds afterward depending on the game, the robots are enabled in teleoperated mode.}
          @item{Around two minutes later, the robots are disabled and the match ends.}]

There is no way for a team to disable a robot during this time except to emergency-stop the robot. The robot can be forcibly disabled by the referees if it displays unsafe behaviour.

Autonomous mode requires some different programming techniques to write well. See @secref["autonomous"] below.

@subsection{Safety}

There are some important guidelines that you need to follow, even if you're working on software:

@itemlist[@item{Always wear safety glasses when working with a robot. You probably like having eyes.}
          @item{Before enabling a robot, always yell CLEAR and wait for people to step away from the robot. Yell CLEAR multiple times if necessary - but don't enable the robot if people could be hit by it.}
          @item{When you first test an autonomous mode, the robot will probably move faster than you expect. Set you speeds low.}
          @item{Before enabling a robot, confirm that all of the Joysticks are free. Do not set any of them on seating surfaces.}
          @item{When enabling a robot, always hover your fingers over the enter (disable) key. This will work regardless of what application currently has focus.}
          @item{The first time you test a robot, or test any potentially dangerous behavior, place the robot "on blocks" - put bricks/wood blocks/something under the drive frame so that the wheels don't touch anything and can spin freely. This prevents it from running into anyone.}]

Remember that following safety procedures are the difference between getting work done and being in the hospital.

@subsection{The roboRIO}

@image["roboRIO.jpg"]

@smaller{@smaller{image sourced from @url{http://khengineering.github.io/RoboRio/faq/roborio/}}}

The roboRIO is FRC's next-generation robot controller, from National Instruments. It supplants the previous cRIO, and runs Linux with PREEMPT_RT patches on an ARM processor.

It contains a set of ports for interfacing with PWM-controlled devices, CAN bus devices, I@superscript{2}C devices, SPI devices, RS232 (serial) devices, miscellaneous digital I/O devices, relays, analog inputs, USB devices, and networked devices over Ethernet.

You aren't allowed to control anything on your robot (except for nonfunctional components like LEDs) with any other controller than the roboRIO, so teams have to use it as their main controller.

See below in this document for some of the devices that attach to the roboRIO.

@subsubsection{Hardware access}

The CCRE provides interfaces to the underlying hardware via WPILib's JNI layer, which attaches to the WPILib Hardware Abstraction Layer (in C++), which attaches to the NI ChipObject propriatary library, which attaches to the NiFPGA interface to the FPGA (field-programmable gate array) device that manages the communication between the higher-level code and the I/O ports.

In other words, here's how hardware access works (CCRE at the left - other systems also shown):

@define[(cellw txt width [height 40]) (let ((gen-text (text txt)))
                            (cc-superimpose (rectangle width height) gen-text))]

@vl-append[10
 (ht-append 10
            (vl-append 10
                       (ht-append 10 (vl-append 10
                                                (ht-append 10 (cellw "CCRE Hardware I/O" 150) (cellw "WPILibJ" 150))
                                                (cellw "WPILib JNI Layer" 310))
                                  (cellw "WPILibC++" 150 90)
                                  (cellw "pyfrc" 150 90))
                       (cellw "Hardware Abstraction Layer (HAL)" 630 40)
                       (cellw "Ni ChipObject .so" 630 40)
                       (cellw "NiFPGA interface" 630 40))
            (cellw "FRC LabVIEW" 150 240))
 (cellw "FPGA Hardware" 790 40)]

See @secref["hardware-access"] for details on how to access hardware.

@subsubsection{The cRIO}

The cRIO was the previous platform used as a robot controller. It ran VxWorks instead of Linux, and the processor was PowerPC instead of ARM. This made it extremely hard to get any software for it. The only JVM we could use was the Squawk JVM, which only supported Java 1.3. (We're on Java 8 now.) The CCRE pioneered using retrotranslation technology to allow us to use some Java 5 features on the cRIO, but even with those the system was much harder to use than the modern roboRIO.

@subsection{Downloading code}

To download code, as we said before, you can easily deploy code to the robot:

@image["deploy.png"]

But how does this work behind the scenes?

First, this goes through the DeploymentEngine, which dispatches to the @code{deploy()} DepTask in the default Deployment class:

@codeblock|{
    @DepTask
    public static void deploy() throws Exception {
        Artifact result = DepRoboRIO.buildProject(robotMain);

        // code slightly abbreviated for clarity.

        DepRoboRIO.RIOShell rshell = DepRoboRIO.discoverAndVerify(robot.RobotTemplate.TEAM_NUMBER);

        rshell.archiveLogsTo(DepProject.root());

        rshell.downloadAndStart(result);
    }
    }|

@margin-note{To find the roboRIO, it checks @code{roboRIO-NNNN.local} (where NNNN is your team number), @code{172.22.11.2}, and @code{10.XX.YY.2} (where XXYY is your team number.)}
This builds your robot code, discovers the roboRIO based on your team number, grabs any old logfiles from the robot, deploys the new robot code to the robot, and restarts the running code.

To be able to talk to your robot to change the code, your laptop needs to be on the same network as the robot, and you need to be able to connect to the robot. (Try running @code{ping roboRIO-NNNN.local} to see if it can be reached, if you're having any problems.)

@subsubsection{SSH}

Sometimes, the robot breaks and you need to figure out what's going on. I'm not going to go into all of the details of this (TODO: go into all the details on this) but I'll overview how you start.

You can connect to the roboRIO over SSH (aka Secure SHell) - you simply point your SSH client at roboRIO-NNNN.local (where NNNN is your team number) and enter the correct username and password.

On Linux or Mac OS X, you can do this from the command line with pre-installed tools:

@margin-note{On Windows, you can use PuTTY.}
@codeblock|{
 $ ssh admin@roboRIO-1540.local
 Password:
 $ do stuff on the robot
}|

The default password is the blank password, and the default username (instead of 'root') is 'admin'.

Once you're on the robot, you want to look in @code{/home/lvuser}, where you should find the user program and some related files, including the most recent log files.

@subsection{Speed controllers}

A speed controller stands between the Power Distribution Panel (PDP) and individual motors, and varies the speed of the motor based on a signal from the roboRIO. There are two primary ways to control a motor: via PWM or via CAN.

@subsubsection{PWM (Pulse-width modulation)}

With PWM, you can drive Talon SRs, Talon SRXes, Victors, and Jaguars.

A Victor:

@image["motor-victor.jpg"]

A Jaguar (also controllable over CAN):

@image["motor-jaguar.jpg"]

A Talon:

@image["motor-talon-sr.jpg"]

A Talon SRX (also controllable over CAN):

@image["motor-talon-srx.jpg"]

For a discussion of how PWM works, see @hyperlink["https://en.wikipedia.org/wiki/Pulse-width_modulation"]{the Wikipedia article}. The important attributes for us are:

@itemlist[@item{PWM as a protocol is unidirectional. You can't get any information from a PWM device.}
          @item{PWM ranges vary by kind of motor, so if you set up your output for a Talon, you can't run a Victor on that output.}
          @item{PWM as a control channel is bidirectional. You can run the motor in either direction.}
          @item{PWM is simple. It is usually easy to get to work and fixing a broken PWM connection is as easy as replacing a cable.}]

See @secref["hardware-access"] for information on how to communicate with PWM speed controllers.

@subsubsection{CAN (Control area network)}

With CAN, you can drive Talon SRXes and CAN Jaguars.

A Jaguar (also controllable over PWM):

@image["motor-jaguar.jpg"]

A Talon SRX (also controllable over PWM):

@image["motor-talon-srx.jpg"]

For a discussion of how CAN works, see @hyperlink["https://en.wikipedia.org/wiki/CAN_bus"]{the Wikipedia article}. The relevant attributes:

@itemlist[@item{CAN as a protocol is bidirectional. You can get information from a CAN device about current voltage, current, faults, and more.}
          @item{CAN needs to be specialized to the specific kind of speed controller - you have to know whether the CAN device is a CAN Jaguar or a Talon SRX, and a setup for one won't work for the other.}
          @item{CAN can be controlled in terms of absolute voltage, fractional voltage, current, and other control modes. Fractional voltage is the easiest because it's simply -1.0 to 1.0 just like other motors.}
          @item{Legacy CAN cables, such as those used for CAN Jaguars, are flaky and unreliable. Luckily, the newer CAN cables are much more reliable.}
          @item{CAN is more complicated and requires more testing and configuration, but should provide more diagnostics in practice.}]

See @secref["hardware-access"] for information on how to communicate with CAN speed controllers.

@subsection{Sensors}

Sensors can be classified by how they connect to the roboRIO.

@subsubsection{Digital Inputs}

  Infrared, touch, encoders, gear tooth, light sensors, magnetic switches, pressure switches

@subsubsection{Analog Inputs}

  Gyros, accelerometers, current sensors, pressure sensors

@subsubsection{Internal Sensors to the roboRIO}

  Internal accelerometers, current sensors

@subsubsection{RS232 Sensors}

  UM7LT

@subsection{WiFi and Networking}

@subsubsection{The FMS and port filtering}

@subsection{Cluck & The Poultry Inspector}

@subsubsection{Network Tables & Smart Dashboard}

@section{Detailed guide to the CCRE}

In progress.

@subsection[#:tag "hardware-access"]{Hardware Access}

In progress.

@subsection[#:tag "remixing"]{Remixing}

In progress.

@subsection[#:tag "autonomous"]{Autonomous}

In progress.

@subsection{Deployment}

In progress.

@subsection{The Cluck Pub/Sub System}

In progress.

@section{CCRE recipes}

In progress.

@section{Versioning policies of the CCRE}

In progress.

@section{API reference}

In progress.

@section{Maintainer's guide}

In progress.
