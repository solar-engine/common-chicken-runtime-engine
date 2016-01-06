#lang scribble/manual
@require[pict]
@require["proc.rkt"]

@title{The Common Chicken Runtime Engine v3.0.0}

The CCRE solves the problem of writing elegant and maintainable robot software
by using a dataflow model and taking care of the infrastructure for your
project so that you can focus on the important parts of your code.

Here's an example of a robot piloted with Arcade Drive:

@jcode|{
    Drive.arcade(FRC.joystick1,
                 FRC.talon(1, FRC.MOTOR_FORWARD),
                 FRC.talon(2, FRC.MOTOR_REVERSE));
}|

Or, something more interesting: an example of a shifting drive train:

@jcode|{
    BooleanOutput shifter = FRC.makeSolenoid(2);
    shifter.setFalseWhen(FRC.startTele);
    shifter.setTrueWhen(FRC.joystick1.onPress(3));
    shifter.setFalseWhen(FRC.joystick1.onPress(1));
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
          @item{You need to have the Java 8 JDK (Java Development Kit) installed. Do not install the JRE! It won't have everything you need. Install the JDK.}]

@subsection{Installing the CCRE}

Short version: import all of the projects from the CCRE repository into Eclipse and Build All.

Long version:

@itemlist[@item{Download the latest CCRE release either as a @hyperlink["https://github.com/flamingchickens1540/Common-Chicken-Runtime-Engine/archive/ccre-v3.0.0.tar.gz"]{tar.gz} or a @hyperlink["https://github.com/flamingchickens1540/Common-Chicken-Runtime-Engine/archive/ccre-v3.0.0.zip"]{zip} depending on your system. You can also @link["https://github.com/flamingchickens1540/Common-Chicken-Runtime-Engine/releases"]{see all downloads here}.}
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

@margin-note{After the first time, your project will be rebuilt every time you deploy the code to the robot.}

@image["buildproject.png"]{Build Project}

@subsection{Basic code}

We'll start with something simple. You should see this code in your project:

@jcode|{
  @Override
  public void setupRobot() {
      // Robot setup code goes here.
  }
}|

Start by adding @jcode|{
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

@jcode|{
  FloatOutput motor = FRC.talon(0, FRC.MOTOR_FORWARD);
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
@jcode-inline{talon} with @jcode-inline{victor} or @jcode-inline{jaguar}. You may also need to replace the @jcode-inline{0}
with the correct port number.

Once you have the configuration correct, open the dropdown next to the External Tools button and select "MyFirstRobot Deploy."

@image["deploy.png"]{deploy}

Make sure that the output ends with @code{BUILD SUCCESSFUL}, and then you can connect your Driver Station to the robot, connect a Joystick, enable your robot, and move the motor by moving the Joystick!

@; TODO: troubleshooting guide.

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
 (cell "while true")
 (indent (cell "if red button is pressed") 40)
 (indent (cell "if key is turned") 80)
 (indent (cell "blow up the world") 120)
 (indent (cell "sleep for 20 milliseconds") 40)]

However, this technique is really easy to get wrong. That example has the bug where if the red button is pressed for very long - more than 20 milliseconds, which isn't very long - then the program will blow up the world multiple times!

Yes, that can be fixed, but the code gets more complicated:

@margin-note{This is hard to scale for multiple reasons, including an inability to have @italic{inline state}. For each button, you need a variable defined in a completely different location in the file (which becomes more significant with larger files) and you have to reference multiple places to figure out what your code does.}

@vl-append[20
 (cell "define variable was_button_pressed")
 (cell "while true")
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

@jcode|{
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

           @jcode{start_driving_forward.event(); // start the robot driving forward}
          }
          @item{@bold{BooleanOutput}: messages have an associated value of true or false.
           The message represents the request that the implementor change something to a state representable by a binary choice.

           @jcode{light_bulb.set(false); // turn off the light bulb}
           @jcode{light_bulb.set(true); // turn on the light bulb}
          }
          @item{@bold{FloatOutput}: messages have an associated real number value - often, but not always, in the range of -1.0 to 1.0.
           The message represents the request that the implementor change something to some potentially intermediate state.

           @jcode{motor.set(0.0f); // stop the motor}
           @jcode{motor.set(1.0f); // run the motor clockwise at full speed}
           @jcode{motor.set(-1.0f); // run the motor counterclockwise at full speed}
           @jcode{motor.set(0.6f); // run the motor clockwise at 60% speed}
           }]

With an Input, the users of the channel can request the present state of the channel from the implementation (if any), and ask the implementation to tell them when the value represented by the input changes.

@itemlist[@item{@bold{EventInput}: there is no associated value.
           Users to be notified when something happens - we call this the EventInput being either fired or produced.

           @margin-note{You would want to use an Instinct module for autonomous code like this.}
           @jcode|{
             // when the match starts, start the robot driving forward
             match_start.send(start_driving_forward);
             }|
           @margin-note{Don't use @jcode-inline{System.out.println(x)} in the CCRE. Logging, which you'll learn later, works better.}
           @jcode|{
              // when the match ends, say so
              match_end.send(() -> System.out.println("Match has ended!"));
              }|
          }
          @item{@bold{BooleanInput}: the associated value is a boolean.
           Users can ask if the current state is true or false, and ask to be told when it changes.

           @jcode|{
            // as long as the light switch is flipped, turn on the light bulb
            light_switch.send(light_bulb);
           }|
           @margin-note{Don't use @jcode-inline{System.out.println(x)} for this. Readouts for the user are best done through Cluck, which you'll learn later.}
           @jcode|{
            light_switch.send((is_flipped) ->
                System.out.println("The light switch is flipped: " + is_flipped));
            // whenever the light switch position is changed, say so
           }|
           @margin-note{You would probably want to use transforms for this.}
           @jcode|{
             // this runs once - you'd want it somewhere where it would run repeatedly.
             if (light_switch.get()) {
                 // We're wasting power!
             } else {
                 // It's too dark in here!
             }
               }|}
          @item{@bold{FloatInput}: the associated value is a real number value - often, but not always, in the range of -1.0 to 1.0.
           Users can ask for the current value, and ask to be told when it changes.

           @jcode|{
             // control a motor with a joystick axis
             joystick_axis.send(motor);
             }|
           @margin-note{See previous margin note about @jcode-inline{System.out.println(x)}}
           @jcode|{
            joystick_axis.send((current_position) ->
                System.out.println("The current Joystick position: " + current_position));
            // report the new position of the Joystick whenever someone moves it
           }|
           @margin-note{This could be done with something from the transformation toolset, which we'll see later.}
           @jcode|{
            // this runs once - you'd want it somewhere where it would run repeatedly.
            if (sewage_level.get() > 1000.0f) {
                // open drainage valve
            }
            }|}]

@subsection{Hardware}

The CCRE includes an API that provides access to an FRC robot's hardware via channels.

For example:

@jcode|{
            BooleanOutput led = FRC.makeDigitalOutput(7);
            FloatOutput test_motor = FRC.talon(3, FRC.MOTOR_FORWARD, 0.2f);
            EventInput start_match = FRC.startTele;
            BooleanInput button = FRC.joystick1.button(3);
            FloatInput axis = FRC.joystick6.axis(3);
}|

See @secref["hardware-access"] below for more info on robot hardware.

@subsection{Transforming channels}

It turns out that, often, you want to do similar things with your channels.
So, correspondingly, all channels have a variety of built-in methods to help you on your journey!

The simplest example is probably @jcode-inline{send}, which works for all three varieties of channels:

@jcode|{
  // this also works if you replace Boolean with Event or Float in both places.
  BooleanInput x = /* ... */;
  BooleanOutput y = /* ... */;
  x.send(y);
  }|

This connects the input to the output, so that @jcode-inline{y} is updated with the current value of @jcode-inline{x} both immediately and whenever @code{x} changes.
In the case of events, @jcode-inline{send} causes the EventOutput to be fired whenever the EventInput is produced.

Another simple example is @jcode-inline{onPress}:

@jcode|{
  BooleanInput bumper = FRC.makeDigitalInput(3);
  bumper.onPress().send(stop_motors);
  }|

This creates an EventInput that fires when the BooleanInput becomes true.

Also useful are @jcode-inline{setWhen} (along with @jcode-inline{setFalseWhen} and @jcode-inline{setTrueWhen}, and @jcode-inline{getSetEvent}):

@jcode|{
  driving_forward.setTrueWhen(FRC.joystick1.onPress(2));
  stop_motors = driving_forward.getSetEvent(false);
  }|

See @secref["transforms"] for more info.

@subsection{Cells}

Unlike in normal Java, you don't use variables to store state in CCRE dataflow code.
Instead, you use Cells, which are similar, but also act as Inputs and Outputs.

For example:

@jcode|{
  BooleanCell cell = new BooleanCell();
  // ...
  cell.get(); // is false
  // ...
  cell.set(true);
  // ...
  cell.get(); // is true
  // ...
  cell.set(false);
  // ...
  cell.get(); // is false
}|

You can use this with dataflow:

@jcode|{
  BooleanCell cell = new BooleanCell();
  some_boolean_input.send(cell);
  cell.onPress().send(do_something);
}|

Cells exist for Events, Booleans, and Floats. For Events, rather than store a value, they simply propagate events:

@jcode|{
  EventCell cell = new EventCell();
  cell.send(do_something);
  cell.send(do_something_else);
  x_happened.send(cell);
  y_happened.send(cell);
}|

This example would cause @jcode-inline{do_something} and @jcode-inline{do_something_else} to be fired whenever @jcode-inline{x_happened} is produced or @jcode-inline{y_happened} is produced.

Sometimes, you want to connect together methods that aren't easy to connect together. For example:

@jcode|{
  send_some_data_to_an_output(???);
  do_something_based_on_an_input(???);
  }|

How would you connect these? There's no implementation to provide either end. Luckily, we can use Cells!

@jcode|{
   // this also works for events and floats with EventCell and FloatCell.
   BooleanCell intermediate_channel = new BooleanCell();
   send_some_data_to_an_output(intermediate_channel);
   do_something_based_on_an_input(intermediate_channel);
   }|

You can also use them to connect far-away sections of your code.

@jcode|{
    BooleanCell some_shared_value = new BooleanCell();
    // ... somewhere ...
    some_input.send(some_shared_value);
    // ... somewhere else ...
    some_shared_value.send(some_output);
}|

@section{Review of advanced Java concepts}

There are a number of features of Java which are heavily used by the CCRE that you might not be familiar with. Below is a quick catalogue of them.

@subsection{Anonymous classes}

Sometimes you might want to implement an Output that does something unique, so you can't use anything built-in. You could put this somewhere else:

@jcode|{
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

@jcode|{
  EventOutput pet_fluffy = new CatPettingEventOutput(fluffy);
    }|

But then the class definition is far away from the actual use, and your code gets clogged up with all of your classes. Clearly, there has to be an easier way:

@jcode|{
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

@jcode|{
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

@jcode|{
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

Also, if you only have a single statement in the lambda, you can omit the @code|{{}}|:

@jcode|{
            EventOutput pet_fluffy = () -> {
              do_pet_fluffy();
            };
            }|

can become:

@jcode|{
            EventOutput pet_fluffy = () -> do_pet_fluffy();
            }|

Even nicer!

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

Note that the spacebar will cause an emergency stop regardless of whether or not the driver station is the current window. If you edit code on your driver station, make sure to disable the robot first. You don't want to emergency stop the robot whenever you type a space into your program.

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

Each Joystick has some number of @italic{axes} - each axis measures a value from -1.0 to 1.0. It also has some number of @italic{buttons} - each button can be either pressed or released.

Standard axes are the position of a Joystick on the forward-backward (Y) axis and the left-right (X) axis.

For example, a trigger on a Joystick is a button, and an altitude control wheel (as on the base of the first Joystick) is an axis.

The X axis on a Joystick is usually axis #1, and the Y axis is usually axis #2.

If you work with something with multiple XY sticks, this may vary.

On an xbox controller, for example, #1 & #2 are the axes on left stick, and for the right stick, the X axis is #5 and the Y axis is #6.
The trigger axes are #3 (left) and #4 (right.)

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

@itemlist[@item{Always wear @bold{safety glasses} when working on a robot. @bold{You probably like your eyes.}}
          @item{Before enabling a robot, always yell CLEAR and wait for people to step away from the robot. Yell CLEAR multiple times if necessary - but @bold{don't enable the robot if people could be hit by it.}}
          @item{When you first test an autonomous mode, @bold{the robot will probably move faster than you expect}. Set your speeds lower than you think you'll need to.}
          @item{Before enabling a robot, confirm that all of the Joysticks are free. Do not set any of them on seating surfaces.}
          @item{When enabling a robot, always hover your fingers over the enter (disable) key. This will work regardless of what application currently has focus.}
          @item{The first time you test a robot, or test @bold{any potentially dangerous behavior}, place the robot "on blocks" - put bricks/wood blocks/something under the drive frame so that the wheels don't touch anything and can spin freely. This prevents it from running into anyone.}]

Remember that following safety procedures are the difference between getting work done and being in the hospital.

@subsection{The roboRIO}

@image["roboRIO.jpg"]

@smaller{@smaller{image sourced from @url{http://khengineering.github.io/RoboRio/faq/roborio/}}}

The roboRIO is FRC's next-generation robot controller, from National Instruments. It supplants the previous cRIO, and runs Linux with PREEMPT_RT patches on an ARM processor.

It contains a set of ports for interfacing with PWM-controlled devices, CAN bus devices, I@superscript{2}C devices, SPI devices, RS232 (serial) devices, miscellaneous digital I/O devices, relays, analog inputs, USB devices, and networked devices over Ethernet.

You aren't allowed to control anything on your robot (except for nonfunctional components like LEDs) with any other controller than the roboRIO, so teams have to use it as their main controller.

See below in this document for some of the devices that attach to the roboRIO.

@subsubsection{The cRIO}

The cRIO was the previous platform used as a robot controller. It ran VxWorks instead of Linux, and the processor was PowerPC instead of ARM. This made it extremely hard to get any software for it. The only JVM we could use was the Squawk JVM, which only supported Java 1.3. (We're on Java 8 now.) The CCRE pioneered using retrotranslation technology to allow us to use some Java 5 features on the cRIO, but even with those the system was much harder to use than the modern roboRIO.

@subsection{Downloading code}

To download code, as we said before, you can easily deploy code to the robot:

@image["deploy.png"]

To see how this works behind the scenes, see @secref["deployment"].

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

@itemlist[@item{A PWM device can't send you any data.}
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

Sensors can be classified by how they connect to the roboRIO:

@subsubsection{Digital Inputs}

Digital Inputs are in the GPIO (general purpose IO) section of the roboRIO. These report either HIGH (true) or LOW (false) at any specific time. For obvious reasons, these correspond to BooleanInputs.

Touch sensors, light sensors, magnetic switches, and pressure switches are examples of simple digital inputs. In one state, they are pressed/activated, and in another state, they are released/deactivated.
@bold{IMPORTANT}: False is not necessary deactivated and true is not necessarily activated! Many (perhaps the majority) of sensors are true by default and only false when activated. Make sure to check your specific sensors to see how they function!

Encoders are bidirectional rotation sensors: every certain fraction of a rotation, they send a directioned tick back to the roboRIO (this is very simplified, of course), which tells the roboRIO that the encoder has spun more.
The FPGA totals these ticks and provides you with a sum, which you can access from your code. These are provided as FloatInputs even though they are discrete.
Unlike most sensors, these require @italic{two} digital input ports to function. There are specific APIs for working with encoders.

Gear tooth sensors are a simplified form of encoders that don't tell you direction - only that motion is occurring.
They only require a single digital input port and are easier to install, so they are sometimes used instead of encoders when you only care about measuring speed or distance without direction.

@subsubsection{Analog Inputs}

Analog inputs are in a dedicated section of the roboRIO. They provide a value in volts from the connected sensor. It is likely that you will need to scale this reading to something more useful to you.

Often, analog sensors are linear: they have a point at which they are @italic{zero} and a point at which they are @italic{one}. (The normalize method @;{ TODO add a link } is very useful for handling these.)

A pressure sensor is a good example that measures the pressure of a robot's pneumatic system. A certain base voltage is produced for a pressure of 1 atm by the pressure sensor, and a certain amount more is produced for each psi above that.

Gyros (gyroscopes) provide an analog value based on the rotation speed, which is integrated in the FPGA. There are specific APIs for working with gyros.

Other analog sensors include accelerometers and current sensors.

@subsubsection{Internal Sensors to the roboRIO}

The roboRIO also contains a set of internal accelerometers. It also contains sensors to measure voltages and currents on various internal rails, so you can check the health of the battery. These are FloatInputs.

@subsubsection{PDP Sensors}

The PDP, or Power Distribution Panel, provides data over the CAN bus to the roboRIO, which tells it the current battery voltage and the present current consumption on each port of the PDP. Useful!

@subsubsection{RS232 Sensors}

Sensors can also be connected over some of the other data buses. (These are: RS232, SPI, I@superscript{2}C, etc.) These usually require custom drivers to be written to use them.

Currently, the CCRE contains a driver for the UM7LT, which is a heading sensor from CH Robotics that tells us the current orientation of the robot. Note, however, that it requires a lot of calibration to use.

@; TODO: user guide to the UM7LT.

@subsection{WiFi and Networking}

Here's a brief overview of how the networking works on a robot when not on a competition field:

@pict-wrap[
 (defines (roboRIO wireless-ap laptop)
   (cell "roboRIO") (cell "Wireless Access Point") (cell "DS Laptop"))
 (harrows (ht-append 40 roboRIO wireless-ap laptop) (roboRIO wireless-ap laptop))]

Each element in this chain is a distinct device with an assigned IPv4 (internet protocol version 4) address, which is made up of four numbers from 0 to 255 (inclusive.)

Computers, in general, can only communicate via knowing each others' IP address - in the case of the internet, usually IP addresses are acquired via the Domain Name System (DNS). DNS lets your computer ask, for example, for an IP address for @code{team1540.org}, and might get back @code{65.39.205.61} or something else depending on changes to the IP address of the server.

On your local network, unlike on the internet, IP addresses are local. Since most IP addresses refer to specific machines on the wide internet, certain ranges are reserved for private IP addresses:

@codeblock|{
10.0.0.0 - 10.255.255.255 - 10.0.0.0/8
172.16.0.0 - 172.31.255.255 - 172.16.0.0/12
192.168.0.0 - 192.168.255.255 - 192.168.0.0/16
  }|

For example, the IP address @code{192.168.1.1} is a valid local IP address. In the FRC control system, IP addresses are assigned based on team number, in the form @code{10.TE.AM.0/24} (which means, for example, @code{10.15.40.0} through @code{10.15.40.255} for team 1540. For shorter team numbers, it might be @code{10.2.54.XX} or @code{10.0.11.XX}.)

Within that range, certain addresses are reserved: (for this example, we use team 1540 addresses.)

@codeblock|{
10.15.40.0
    Reserved, as it ends in 0. Don't use it.
10.15.40.1
    The robot's wireless access point.
    This is usually a D-Link DAP-1522 Rev B wireless radio.
10.15.40.2
    Previously reserved for the cRIO.
    It can be used by the roboRIO if you wish.
10.15.40.3 - 10.15.40.19
    Unused. Allocate static addresses from these.
10.15.40.20 - 10.15.40.199
    Assigned by the DHCP server on the roboRIO. (See below.)
10.15.40.200 - 10.15.40.254
    Unused. Allocate static addresses from these.
10.15.40.255
    Reserved, as it ends in 255. Don't use it.
  }|

There are two primary ways for a computer to get an IP address: a statically-allocated address or a DHCP-allocated address.

DHCP (Dynamic Host Configuration Protocol) lets your computer, after it has joined a network, ask others on the network "Please give me an address!" and a DHCP server on the network will tell it the address. Pretty much every normal wireless or wired network that you would plug your computer into uses DHCP.

A static address is set manually on the computer.

DHCP has advantages in terms of it being easier to configure on each computer... but it means that IP addresses can change over time, requires a running DHCP server, and other issues. A static address, by contrast, must be manually configured as to not conflict with any other address, but doesn't require as much infrastructure.

With the old 2014 cRIO control system, everything used static addresses. Today, with the 2015 roboRIO control system, everything but the wireless AP uses DHCP (as provided BY the wireless AP.) To solve the issue of the DHCP addresses changing, the roboRIO uses mDNS, which is a version of DNS that allows computers on a local network to find each other based on local names. The roboRIO is usually @code{roboRIO-NNNN.local}. (For example, @code{roboRIO-1540.local}.)

@subsubsection{The FMS and port filtering}

In a competition, instead of laptops connecting directly to wireless APs, the wireless radios are reprogrammed to act as wireless bridges, and then when they are on the field, the field generates access points for the robots in the current match, and the bridges connect to those APs. The laptops then connect over a wired network through the FMS to the robots.

This is mostly transparent, but has two major effects:

@itemlist[@item{Most TCP and UDP ports are blocked! This means that you must limit your traffic to a fixed set of ports. See below.}
          @item{The wireless radio is no longer in AP mode and doesn't host a DHCP server, which can cause problems connecting directly to the robot. @;{ (TODO: include solutions here.) }}]

A listing of available ports, up to date as of the 2015 challenge:

@itemlist[@item{TCP 1180}
          @item{TCP 1735}
          @item{UDP 1130 & 1140 (but used for the driver station, so you can't use these)}
          @item{TCP 80 & 443 (but these are below 1024, so you can't bind them on the roboRIO)}
          @item{UDP/TCP 554 (which has the same problem)}
          @item{UDP/TCP 5800-5810 (designated for team use)}]

The CCRE implementation for FRC hosts Cluck servers on 1540, 1735, 5800, and 5805 by default, and 5800 is used as the default port to connect to.

(See the next section for info on Cluck.)

@subsection{Cluck}

The CCRE includes a publish-subscribe networking system, designed to seamlessly integrate with the rest of the CCRE channel system.

The basic idea is that a group of computers connect to each other over Cluck transports, and can exchange messages over this. Specifically, one side can @italic{publish} channels and another side can then @italic{subscribe} to those channels.

For example, in the previous example of CCRE usage, you could put the blowing-up-the-world part onto its own computer/robot:

@let[((red (cell "red button"))
              (is (cell "becomes pressed"))
              (key (cell "key is turned"))
              (and (cell "and"))
              (pub (cell "publish over cluck"))
              (sub (cell "subscribe over cluck"))
              (blow (cell "blow up the world")))
     (harrows (ht-append 30
                         (frame #:segment 5
                                (cc-superimpose (blank 600 135)
                                                (harrows (vl-append 20
                                                                    (hc-append 30 red is and pub)
                                                                    (indent key 161))
                                                         (red is and pub)
                                                         (key and))))
                         (frame #:segment 5
                                (cc-superimpose (blank 400 75)
                                                (harrows (ht-append 30 sub blow)
                                                         (sub blow)))))
              (pub sub))
     ]

It's actually about this easy to connect things with actual code. See @secref["cluck"].

@subsubsection{The Poultry Inspector}

In usual use, you don't need to connect multiple robots or other pieces of code together with Cluck - often, the main use is to publish a bunch of channels and
@italic{inspect} them via the Poultry Inspector.

@image["poultry-inspector.png"]

Don't worry - it's not as complicated as the image may make it look. That's just with a lot of channels displayed.

See @secref["poultry-inspector"] below for more details on how to use it.

@subsubsection{Network Tables & Smart Dashboard}

WPILib, the "official" framework for writing robot code, has a similar (but, in our minds, insufficiently powerful) system called NetworkTables, and an equivalent to the Poultry Inspector called SmartDashboard.

We don't use these, due to feeling that the engineering behind them is not sufficiently robust for our purposes.

@section{Detailed guide to the CCRE}

This section is designed to provide a detailed guide on how to use just about everything in the CCRE!

It's organized into the following sections:

@; ***** TODO: talk about wireless network names ! *****

@table-of-contents{}

@subsection{Documentation format}

The following formats are used when describing constructors, methods, or fields:

@jnew[BooleanCell (boolean initial_value) "setup"]

This constructor could be used, for example, as:

@jcode|{
            BooleanCell cell = new BooleanCell(false);
}|

@jmethod[EventInput (BooleanInput onPress) "setup"]

This method could be used, for example, as:

@jcode|{
            BooleanInput button = /* ... some code goes here ... */;
            EventInput press = button.onPress();
}|

@jmethod[double static (Math sin) (double a) "setup"]

This method could be used, for example, as:

@jcode|{
            float x = 10.0f;
            double y = Math.sin(x);
}|

@jfield[EventOutput static (EventOutput ignored) "setup"]

This field could be used, for example, as:

@jcode|{
        EventOutput output = EventOutput.ignored;
        output.event();
}|

@subsection{Flow versus Setup}

There are two conditions in which CCRE code can run: flow mode and setup mode. When the robot first starts, it is running in setup mode while your code sets up its functionality. Once this finishes, the robot goes into flow mode and executes the control that has been set up.

Every method in the CCRE is tagged as either flow or setup. For example, the documentation might say:

@jmethod[void (BooleanOutput set) (boolean value) "flow"]

for flow mode or perhaps

@jmethod[EventInput (BooleanInput onPress) "setup"]

@margin-note{Flow mode versus setup mode is more about where you call a method from rather than exactly when it occurs, but in general you can understand the modes based on whether or not the robot is done setting up yet.}

for setup mode. Note the text that says "flow" or "setup".

Except in very specific cases, your code should never call a setup method from flow mode, or a flow method from setup mode. The main exception is that sometimes you want to preinitialize the value of something, and so may call a flow method from setup mode in such cases.

@subsection{Dataflow Channels}

As touched on before, a channel represents the ability to communicate in some fashion with an implementation. Channels can either be inputs or outputs, and carry values that are either booleans, floats, or events.

@subsubsection{Outputs}

Outputs are, at a fundamental level, something with a state that can be changed, but not necessarily queried.

@jmethod*[(void (BooleanOutput set) (boolean value))
          (void (FloatOutput set) (float value))
          (void (EventOutput event))
          "flow"]

You can call these methods to change the current state of the output. All control of outputs will at some level reduce to these methods.

@margin-note{The "flow block" annotation here specifies that the code in the block starting on that line is in flow mode.}
@jcode[#:box (list "setup" "flow block" #f "setup" "flow block" #f "setup" "flow block")]{
  $EventOutput eo = () -> {
      // do something
  };
  $BooleanOutput bo = (value) -> {
      // do something with value, a boolean
  };
  $FloatOutput eo = (value) -> {
      // do something with value, a float
  };
}

You can implement an output with the above code. You can replace @jcode-inline{// do something} with the flow mode code to execute when the output is controlled.

Please note that, most of the time, you shouldn't need to implement your own channels! Usually there is already an implementation for you. See @secref["transforms"].

@subsubsection{Inputs}

@jmethod*[(boolean (BooleanInput get))
          (float (FloatInput get))
          "flow"]

For many inputs, you can access the current value at any time with @jcode-inline{get()}. This calculates the current value at the time that you call @jcode-inline{get()}.

@jcode[#:box (list "setup" "flow block" #f "setup" "flow block" #f "setup" "flow block")]{
    event_input.send(() -> {
        // do something
    });
    boolean_input.send((value) -> {
        // do something with value, the new boolean value
    });
    float_input.send((value) -> {
        // do something with value, the new float value
    });
}

More importantly, you can listen for changes on an input. This works for all inputs. You may notice that these contain a part suspiciously similar to defining outputs: this is because you subscribe to a value by telling an input to send all values to an output, which you can implement yourself.

@jmethod*[(CancelOutput (EventInput send) (EventOutput target))
          (CancelOutput (BooleanInput send) (BooleanOutput target))
          (CancelOutput (FloatInput send) (FloatOutput target))
          "setup"]

The @jcode-inline{send} family of methods are all used to connect an input to an output with the same type of data.
So, whenever the value of the input changes (for a value-based input, like @jcode-inline{BooleanInput} or @jcode-inline{FloatInput}) or the input is produced (for @jcode-inline{EventInput}), then @jcode-inline{target} will received that value or event.

See below @;{ (TODO: ADD REF) } for @jcode-inline{CancelOutput}.

@jcode[#:box (list "setup" #f "flow block" #f #f "setup" #f "flow block" #f #f "setup" #f "flow block")]{
    EventInput new_input = new $DerivedEventInput(inputs) {
        protected boolean shouldProduce() {
            return /* true if an event should be produced right now */;
        }
    };
    BooleanInput new_input = new $DerivedBooleanInput(inputs) {
        protected boolean apply() {
            return /* value */;
        }
    };
    FloatInput new_input = new $DerivedFloatInput(inputs) {
        protected float apply() {
            return /* value */;
        }
    };
}

You can easily implement an input with the above code. You can replace @jcode-inline{/* value */} with the flow mode code to calculate the value of the input.

@jcode-inline{apply()} will be called once when the input is created, and then exactly once for each time that one of @jcode-inline{inputs} is updated.
The value provided by @jcode-inline{apply} will be returned by @jcode-inline{get}.

Since the value only updates when one of @jcode-inline{inputs} updates, make sure that you don't access anything not included in that list.
You can figure out most of what you need in that list by looking at the inputs you call @jcode-inline{get()} on.

Please note that, most of the time, you shouldn't need to implement your own channels! Usually there is already an implementation for you. See @secref["transforms"].

@jmethod*[(CancelOutput (EventInput onUpdate) (EventOutput target))
          (CancelOutput (BooleanInput onUpdate) (EventOutput target))
          (CancelOutput (FloatInput onUpdate) (EventOutput target))
          "setup"]

@margin-note{For EventInput, @jcode-inline{onUpdate} and @jcode-inline{send} are essentially identical.}
The @jcode-inline{onUpdate} family of methods are similar to the @jcode-inline{send} family of methods, but instead of sending a value in the case of a @jcode-inline{BooleanInput} or a @jcode-inline{FloatInput}, they just notify @jcode-inline{target} when the value changes.

See below @;{ (TODO: ADD REF) } for @jcode-inline{CancelOutput}.

@margin-note{Here, "setup block" means that the code in this block runs in setup mode.}
@jcode[#:box (list "setup" #f "setup block" #f #f #f "setup" #f "setup block" #f #f #f #f "flow block" #f #f "setup" #f "setup block" #f #f #f #f "flow block")]{
    EventInput input = new $EventInput() {
        public CancelOutput onUpdate(EventOutput target) {
            // set up to tell target when this input changes
            return /* a CancelOutput that cancels the connection */;
        }
    };
    BooleanInput input = new $BooleanInput() {
        public CancelOutput onUpdate(EventOutput target) {
            // set up to tell target when this input changes
            return /* a CancelOutput that cancels the connection */;
        }

        public boolean get() {
            return /* the current value */;
        }
    };
    FloatInput input = new $FloatInput() {
        public CancelOutput onUpdate(EventOutput target) {
            // set up to tell target when this input changes
            return /* a CancelOutput that cancels the connection */;
        }

        public float get() {
            return /* the current value */;
        }
    };
}

If you want more control over your inputs, you can implement them directly.
You implement @jcode-inline{onUpdate} to specify when the input changes, and implement @jcode-inline{get} to specify the value.

@bold{WARNING}: You probably don't want to use this! It's hard to implement, and you might make a mistake.
@jcode-inline{new DerivedEventInput} and friends, as detailed above @;{ (TODO: ADD REF) } are better for almost all implementations.

@subsubsection{CancelOutputs}

The @jcode-inline{send} and @jcode-inline{onUpdate} methods also return an @jcode-inline{CancelOutput}. This allows you to cancel a send after it is sent. For example:

@jcode{
       CancelOutput cancellator = an_input.send(an_output);
       // ... much later ...
       cancellator.cancel();
}

@jmethod[void (CancelOutput cancel) "setup"]

You call the @jcode-inline{cancel} method to cancel whatever the @jcode-inline{CancelOutput} represents.
Note that although this class is similar to @jcode-inline{EventOutput}, it is used in the setup mode, rather than flow mode.

@jcode[#:box (list "setup" "setup block")]{
    $CancelOutput cancellator = () -> {
        // ... cancel whatever it is ...
    };
}

You may need to implement a CancelOutput yourself, which can be done as shown here.
Note, of course, that the body is a setup block, not a flow block.

@jmethod[CancelOutput (CancelOutput combine) (CancelOutput other) "setup"]

This combines the CancelOutput you call it on with another CancelOutput, so that the resulting CancelOutput cancels both of these CancelOutputs.

@subsubsection{Channel Cells}

A Cell is the dataflow equivalent of a variable. In the case of a @jcode-inline{BooleanCell} or a @jcode-inline{FloatCell}, it simply holds a value that can be used as a BooleanOutput or FloatOutput (to modify the value) or as a BooleanInput or FloatInput (to read the value.) As a @jcode-inline{EventCell}, it simply propagates events through itself, and can be used as either an EventInput (to receive events) or EventOutput (to send events.)

The general idea of a channel that is both an Input and an Output is called an IO, for example @jcode-inline{BooleanIO} or @jcode-inline{EventIO}. All Cells are IOs.

@jmethod*[(EventOutput (EventIO asInput))
          (BooleanOutput (BooleanIO asInput))
          (FloatOutput (FloatIO asInput))
          (EventOutput (EventIO asOutput))
          (BooleanOutput (BooleanIO asOutput))
          (FloatOutput (FloatIO asOutput))
          "setup"]

The @jcode-inline{asInput()} and @jcode-inline{asOutput()} methods allow you to use an IO as just an Input or just an Output. Calling @jcode-inline{x.asInput()} will simply return @jcode-inline{x}, but in a variable of a different type, so this is equivalent to casting.

@jcode{
       BooleanIO x = new BooleanCell();
       x.asInput() == x == (BooleanInput) x;
}

However, sometimes you need to force which side you use the Cell or IO as, which is why this functionality is provided.
While you can cast, usage of @jcode-inline{asInput()} and @jcode-inline{asOutput()} is checked at compile time, so any misuses will be found before you try to run your code.
With casting, a mistake might not be found until you run code on your robot.

@jmethod*[(EventIO static (EventIO compose) (EventInput input) (EventOutput output))
          (BooleanIO static (BooleanIO compose) (BooleanInput input) (BooleanOutput output))
          (FloatIO static (FloatIO compose) (FloatInput input) (FloatOutput output))
          "setup"]

Sometimes, you want to combine an input and output for the same thing into a single IO. You can simply use @jcode-inline{compose(input, output)} to do this.

@jmethod[void (BooleanIO toggle) "flow"]

You can use @jcode-inline{toggle()} to toggle the value of a @jcode-inline{BooleanIO}.

@jcode{
    BooleanIO something = /* ... */;
    // ...
    something.toggle();
    // is equivalent to
    something.set(!something.get());
}

@jmethod[EventOutput (BooleanIO eventToggle) "setup"]

You can get an event that lets you toggle the value of a @jcode-inline{BooleanIO}, as if you had called @jcode-inline{toggle}.

@jcode{
    EventInput something_happened = /* ... */;
    BooleanIO something_controllable = /* ... */;
    something_happened.send(something_controllable.eventToggle();
}

This would toggle @jcode-inline{something_controllable} whenever @jcode-inline{something_happened}.

@jmethod[void (BooleanIO toggleWhen) (EventInput when) "setup"]

This tells the @jcode-inline{BooleanIO} to toggle itself whenever @jcode-inline{when} fires.

@jcode{
    EventInput something_happened = /* ... */;
    BooleanIO something_controllable = /* ... */;
    something_controllable.toggleWhen(something_happened);
}

@jnew[EventCell "setup"]

This lets you create a new anonymous EventIO that simply passes events through itself.

@jcode{
    EventIO passthrough_event = new EventCell();
    // ...
    passthrough_event.send(some_output);
    // ...
    some_input.send(passthrough_event);
}

@jcode[#:box "setup"]{
    new $EventCell(EventOutput... outputs)
}

This lets you create a new anonymous EventIO that simply passes events through itself.

This sends any events that occur to all of the outputs in the constructor arguments, in addition to any other places.

@jcode{
    EventIO passthrough_event = new EventCell(a, b, c);
    // is equivalent to
    EventIO passthrough_event = new EventCell();
    passthrough_event.send(a);
    passthrough_event.send(b);
    passthrough_event.send(c);
}

@jnew[BooleanCell "setup"]

This lets you create a new anonymous BooleanIO that simply stores a value. The initial value is @jcode-inline{false}.

@jcode{
    BooleanCell some_value = new BooleanIO();
    // ...
    some_value.send(some_output);
    // ...
    some_input.send(some_value);
}

@jnew[BooleanCell (boolean default) "setup"]

This lets you create a new anonymous BooleanIO that simply stores a value. The initial value is @jcode-inline{default}.

@jcode{
    BooleanCell some_value = new BooleanIO(true);
    // ...
    some_value.send(some_output);
    // ...
    some_input.send(some_value);
}

@jnew[BooleanCell (boolean default) "setup"]

This lets you create a new anonymous BooleanIO that simply stores a value. The initial value is @jcode-inline{default}.

@jcode{
    BooleanCell some_value = new BooleanCell(true);
    // ...
    some_value.send(some_output);
    // ...
    some_input.send(some_value);
}

@jcode[#:box "setup"]{
    new $BooleanCell(BooleanOutput... targets);
}

This lets you create a new anonymous BooleanIO that simply stores a value.

This sends the value to all of the outputs in the constructor arguments, in addition to any other places.

@jcode{
    BooleanIO passthrough_value = new BooleanCell(a, b, c);
    // is equivalent to
    BooleanIO passthrough_value = new BooleanCell();
    passthrough_value.send(a);
    passthrough_value.send(b);
    passthrough_value.send(c);
}

@jnew[FloatCell "setup"]

This lets you create a new anonymous FloatIO that simply stores a value. The initial value is @jcode-inline{0}.

@jcode{
    FloatCell some_value = new FloatIO();
    // ...
    some_value.send(some_output);
    // ...
    some_input.send(some_value);
}

@jnew[FloatCell (float default) "setup"]

This lets you create a new anonymous FloatIO that simply stores a value. The initial value is @jcode-inline{default}.

@jcode{
    FloatCell some_value = new FloatIO(3.2f);
    // ...
    some_value.send(some_output);
    // ...
    some_input.send(some_value);
}

@jnew[FloatCell (float default) "setup"]

This lets you create a new anonymous FloatIO that simply stores a value. The initial value is @jcode-inline{default}.

@jcode{
    FloatCell some_value = new FloatCell(0);
    // ...
    some_value.send(some_output);
    // ...
    some_input.send(some_value);
}

@jcode[#:box "setup"]{
    new $FloatCell(FloatOutput... targets);
}

This lets you create a new anonymous FloatIO that simply stores a value.

This sends the value to all of the outputs in the constructor arguments, in addition to any other places.

@jcode{
    FloatIO passthrough_value = new FloatCell(a, b, c);
    // is equivalent to
    FloatIO passthrough_value = new FloatCell();
    passthrough_value.send(a);
    passthrough_value.send(b);
    passthrough_value.send(c);
}

@subsection[#:tag "transforms"]{Dataflow Transformations}

In practice, most ways that you might want to transform a channel are ways that someone else has also wanted to. For example, one might convert a BooleanInput to an EventInput for when the BooleanInput becomes true. This is very useful for checking button or sensor presses.

Since these transformations appear repeatedly, the CCRE provides easy ways to perform these transformations.

However, don't let the term "transformation" confuse you: when you call @jcode-inline{output.negate()}, for example, nothing changes about @jcode-inline{output}.
Rather a new channel is returned by @jcode-inline{output.negate()}:

@jcode{
    FloatOutput negated_output = output.negate();
}

By "transformation", we usually mean a relationship between an existing channel and a newly-created channel, but sometimes we include things that aren't technically transformations but are similarly useful and related to channels.

This section will explain various ways to transform a channel.

@subsubsection{EventOutputs}

@jmethod[EventOutput (EventOutput combine) (EventOutput other) "setup"]

This combines two @jcode-inline{EventOutput}s into a single @jcode-inline{EventOutput}, so that firing the result would fire both of the outputs.

@jcode{
    EventOutput merged = a.combine(b);
    // then this would be equivalent:
    merged.event();
    // to this:
    a.event();
    b.event();
}

@jmethod*[(EventOutput (EventOutput filter) (BooleanInput allow))
          (EventOutput (EventOutput filterNot) (BooleanInput deny))
          "setup"]

@jcode-inline{filter(allow)} filters an @jcode-inline{EventOutput} so that events are only propagated further when @jcode-inline{allow.get() == true}.

@jcode-inline{filter(deny)} is similar, but events are only propagated when @jcode-inline{deny.get() == false}.

@jcode{
    EventOutput maybe_blow_up_world = blow_up_world.filter(has_authorization);
    // then this would be equivalent:
    maybe_blow_up_world.event();
    // to this:
    if (has_authorization.get()) {
        blow_up_world.event();
    }
}

@jmethod[EventOutput (EventOutput debounce) (long minimumMilliseconds) "setup"]

Debouncing limits how often an event can occur, such that if an event occurs a short time after another event, it will be ignored. The debouncing time is given by @jcode-inline{minimumMilliseconds}, which is the minimum number of milliseconds after one event before another event can pass through.

This is used to handle buttons, switches, or sensors that tend to "bounce" and trigger multiple times when you press them once.

The timeout is relative to the last event that was passed through; events that are ignored do not delay the timeout. This way, if an event occurs repeatedly at a period smaller than @jcode-inline{minimumMilliseconds}, there will be about one event every @jcode-inline{minimumMilliseconds}.

@jmethod[CancelOutput (EventOutput on) (EventInput when) "setup"]

This is the same as @jcode-inline{send}, but in the opposite direction.

@jcode{
    EventInput a;
    EventOutput b;
    // these are equivalent:
    a.send(b);
    b.on(a);
}

@jfield[EventOutput static (EventOutput ignored) "setup"]

This @jcode-inline{EventOutput} ignores all events sent to it.

@jcode{
    EventOutput.ignored.event();
    // is equivalent to doing absolutely nothing.
}

@subsubsection{EventInputs}

@jmethod[EventInput (EventInput or) (EventInput other) "setup"]

This is an @jcode-inline{EventInput} that is fired whenever either of the @jcode-inline{EventInput}s are fired.

@jcode{
    EventInput c = a.or(b);
    // this is equivalent:
    c.send(output);
    // to:
    a.send(output);
    b.send(output);
}

@jmethod*[(EventInput (EventInput and) (BooleanInput allow))
          (EventInput (EventInput andNot) (BooleanInput deny))
          "setup"]

This is an @jcode-inline{EventInput} limited to only firing when @jcode-inline{allow.get() == true} or @jcode-inline{deny.get() == false}, depending on which version you use.

So, each time that the original @jcode-inline{EventInput} fires, @jcode-inline{allow} or @jcode-inline{deny} is polled, and if the value is the expected value, the result @jcode-inline{EventInput} fires.

@jcode{
    EventInput c = a.and(b);
    // this is equivalent:
    c.send(output);
    // to:
    a.send(output.filter(b));
}

@jmethod[EventInput (EventInput debounced) (long minimumMilliseconds) "setup"]

Debouncing limits how often an event can occur, such that if an event occurs a short time after another event, it will be ignored. The debouncing time is given by @jcode-inline{minimumMilliseconds}, which is the minimum number of milliseconds after one event before another event can pass through.

This is used to handle buttons, switches, or sensors that tend to "bounce" and trigger multiple times when you press them once.

The timeout is relative to the last event that was passed through; events that are ignored do not delay the timeout. This way, if an event occurs repeatedly at a period smaller than @jcode-inline{minimumMilliseconds}, there will be about one event every @jcode-inline{minimumMilliseconds}.

@jfield[EventInput static (EventInput never) "setup"]

This is an EventInput that never occurs.

@jcode{
    EventInput.never.send(output);
    // is equivalent to doing absolutely nothing!
}

@subsubsection{BooleanOutputs}

@jmethod[BooleanOutput (BooleanOutput invert) "setup"]

This is an inverted version of the original @jcode-inline{BooleanOutput}.

@jcode{
    BooleanOutput inv = a.invert();
    // so then this is equivalent
    inv.set(false);
    // to this:
    a.set(true);
}

@jmethod[BooleanOutput (BooleanOutput combine) (BooleanOutput other) "setup"]

This combines two @jcode-inline{BooleanOutput}s into a single @jcode-inline{BooleanOutput}, so that setting the result to a value would set both of the original outputs to that value.

@jcode{
    BooleanOutput merged = a.combine(b);
    // then this would be equivalent:
    merged.set(true);
    // to this:
    a.set(true);
    b.set(true);
}

@jmethod[BooleanOutput (BooleanOutput limitUpdatesTo) (EventInput update) "setup"]

This limits a @jcode-inline{BooleanOutput} such that changes in its value propagate when, and only when, @jcode-inline{update} is fired.

@jcode{
    EventCell b = new EventCell();
    BooleanOutput c = a.limitUpdatesTo(b);
    // ...
    c.set(true); // does not modify a
    b.event(); // until this point!
}

@jmethod*[(EventOutput (BooleanOutput eventSet) (boolean value))
          (EventOutput (BooleanOutput eventSet) (BooleanInput value)) "setup"]

This provides an @jcode-inline{EventOutput} that sets this @jcode-inline{BooleanOutput} to some value, either a constant value or a value fetched in the moment from a @jcode-inline{BooleanInput}.

@jcode{
    EventOutput o = a.eventSet(true);
    // and then:
    o.event();
    // is equivalent to:
    a.set(true);
}

With a @jcode-inline{BooleanInput} parameter:

@jcode{
    EventOutput o = a.eventSet(input);
    // and then:
    o.event();
    // is equivalent to:
    a.set(input.get());
}

@jmethod*[(void (BooleanOutput setWhen) (boolean value) (EventInput when))
          (void (BooleanOutput setWhen) (BooleanInput value) (EventInput when))
          (void (BooleanOutput setTrueWhen) (EventInput when))
          (void (BooleanOutput setFalseWhen) (EventInput when))
          "setup"]

When @jcode-inline{when} fires, this @jcode-inline{BooleanOutput} will be set to @jcode-inline{value} or @jcode-inline{value.get()}, depending on type.

In the case of @jcode-inline{setTrueWhen(when)} or @jcode-inline{setFalseWhen(when)}, the boolean given in the name takes the place of @jcode-inline{value}.

@jcode{
    EventCell event = new EventCell();
    output.setWhen(true, event);
    // and then:
    event.event();
    // is equivalent to:
    output.set(true);
}

With a @jcode-inline{BooleanInput} parameter:

@jcode{
    EventCell event = new EventCell();
    output.setWhen(input, event);
    // and then:
    event.event();
    // is equivalent to:
    output.set(input.get());
}

With a constant name:

@jcode{
    EventCell event = new EventCell();
    output.setFalseWhen(event);
    // and then:
    event.event();
    // is equivalent to:
    output.set(false);
}

@jmethod[BooleanOutput static (BooleanOutput polarize) (EventOutput toFalse) (EventOutput toTrue) "setup"]

This combines two @jcode-inline{EventOutput}s into a @jcode-inline{BooleanOutput}.
When the value sent to the @jcode-inline{BooleanOutput} changes to @jcode-inline{true}, @jcode-inline{toTrue} will be fired, and when it changes to @jcode-inline{false}, @jcode-inline{toFalse} will be fired.

@jcode{
    BooleanOutput b = BooleanOutput.polarize(forFalse, forTrue);
    // and then:
    b.set(true);
    // would be equivalent to:
    forTrue.event();
    // IF b was last set to false.
}

@jcode{
    BooleanOutput b = BooleanOutput.polarize(forFalse, forTrue);
    // ...
    b.set(true);
    // and then a subsequent
    b.set(true);
    // would be equivalent to doing nothing.
}

@jmethod*[(BooleanOutput (BooleanOutput filter) (BooleanInput allow))
          (BooleanOutput (BooleanOutput filterNot) (BooleanInput deny)) "setup"]

@jcode-inline{filter} filters a @jcode-inline{BooleanOutput} so that it only changes when @jcode-inline{allow.get() == true}.

@jcode-inline{filterNot} filters a @jcode-inline{BooleanOutput} so that it only changes when @jcode-inline{deny.get() == false}.

As long as @jcode-inline{allow.get() == true}, each change to the returned @jcode-inline{BooleanOutput} will modify the original @jcode-inline{BooleanOutput}.

When @jcode-inline{allow} changes to @jcode-inline{false}, the current value is preserved.

As long as @jcode-inline{allow.get() == false}, each change to the returned @jcode-inline{BooleanOutput} will be remembered, but not propagated.

When @jcode-inline{allow} changes to @jcode-inline{true}, the last remembered value will be propagated.

@jcode{
    BooleanOutput filtered = original.filter(allow);
    // ...
    allow.set(true);
    filtered.set(true); // equivalent to original.set(true);
    filtered.set(false); // equivalent to original.set(false);
    filtered.set(true); // equivalent to original.set(true);
    allow.set(false);
    filtered.set(false); // does nothing
    filtered.set(true); // does nothing
    filtered.set(false); // does nothing
    allow.set(true); // now does original.set(false);
}

@jcode-inline{original.filterNot(deny)} is equivalent to @jcode-inline{original.filter(deny.not())}.

@jfield[BooleanOutput static (BooleanOutput ignored) "setup"]

This is a BooleanOutput that ignores all values sent to it.

@jcode{
    BooleanOutput.ignored.set(false);
    // is equivalent to absolutely nothing!
}

@subsubsection{BooleanInputs}

@jfield[BooleanInput static (BooleanInput alwaysTrue) "setup"]

A @jcode-inline{BooleanInput} that is always @jcode-inline{true} and never changes.

@jfield[BooleanInput static (BooleanInput alwaysFalse) "setup"]

A @jcode-inline{BooleanInput} that is always @jcode-inline{false} and never changes.

@jmethod[BooleanInput static (BooleanInput always) (boolean value) "setup"]

A @jcode-inline{BooleanInput} that is always @jcode-inline{value} and never changes.

@jmethod[BooleanInput (BooleanInput not) "setup"]

Provides a @jcode-inline{BooleanInput} that is always the inverse of the original @jcode-inline{BooleanInput}.

This means that @jcode-inline{input.not().get() == !input.get()}.

@jmethod[BooleanInput (BooleanInput and) (BooleanInput b) "setup"]

Provides a @jcode-inline{BooleanInput} that is @jcode-inline{true} if and only if both @jcode-inline{BooleanInput}s are @jcode-inline{true}.

This means that @jcode-inline{a.and(b).get() == (a.get() && b.get())}.

@jmethod[BooleanInput (BooleanInput andNot) (BooleanInput b) "setup"]

@jcode-inline{a.andNot(b)} is equivalent to @jcode-inline{a.and(b.not()}.

@jmethod[BooleanInput (BooleanInput xor) (BooleanInput b) "setup"]

Provides a @jcode-inline{BooleanInput} that is @jcode-inline{true} when one of the @jcode-inline{BooleanInput}s is @jcode-inline{true} and the other is @jcode-inline{false}.

This means that @jcode-inline{a.and(b).get() == (a.get() ^ b.get())}.

@jmethod[BooleanInput (BooleanInput or) (BooleanInput b) "setup"]

Provides a @jcode-inline{BooleanInput} that is @jcode-inline{true} if either of the @jcode-inline{BooleanInput}s are @jcode-inline{true}.

This means that @jcode-inline{a.and(b).get() == (a.get() || b.get())}.

@jmethod[BooleanInput (BooleanInput orNot) (BooleanInput b) "setup"]

@jcode-inline{a.orNot(b)} is equivalent to @jcode-inline{a.or(b.not())}.

@jmethod*[(EventInput (BooleanInput onPress))
          (EventInput (BooleanInput onRelease))
          (EventInput (BooleanInput onChange)) "setup"]

@jcode-inline{onPress} provides a @jcode-inline{EventInput} that is produced whenever this @jcode-inline{BooleanInput} changes from @jcode-inline{false} to @jcode-inline{true}.

@jcode-inline{onRelease} is the same, but for when @jcode-inline{true} changes to @jcode-inline{false}.

@jcode-inline{onChange} is similar, but happens whenever any change occurs.

@jcode{
    BooleanCell input = new BooleanCell(false);
    EventInput press = input.onPress();
    EventInput release = input.onRelease();
    EventInput change = input.onChange();
    // ...
    input.set(true);  // press fires; change fires
    input.set(false); // release fires; change fires
    input.set(false);    // nothing happens
    input.set(true);  // press fires; change fires
    input.set(true);    // nothing happens
    input.set(false); // release fires; change fires
    input.set(true);  // press fires; change fires
}

@jmethod*[(FloatInput (BooleanInput toFloat) (float off) (float on))
          (FloatInput (BooleanInput toFloat) (float off) (FloatInput on))
          (FloatInput (BooleanInput toFloat) (FloatInput off) (float on))
          (FloatInput (BooleanInput toFloat) (FloatInput off) (FloatInput on))
          "setup"]

@jcode-inline{toFloat} provides a @jcode-inline{FloatInput} with a value selected from one of two other @jcode-inline{float}s or @jcode-inline{FloatInput}s.

@jcode{
    BooleanCell input = new BooleanCell(false);
    FloatInput converted = input.toFloat(0.0f, 0.75f);
    // ...

    // converted is 0.0
    input.set(true);
    // converted is 0.75
    input.set(false);
    // converted is 0.0
}

@jmethod*[(BooleanInput (BooleanInput filterUpdates) (BooleanInput allow))
          (BooleanInput (BooleanInput filterUpdatesNot) (BooleanInput deny))
          "setup"]

@jcode-inline{filterUpdates} provides a @jcode-inline{BooleanInput} that follows the value of the original @jcode-inline{BooleanInput} while @jcode-inline{allow.get() == true}, and holds the last value while @jcode-inline{allow.get() == false}.

@jcode-inline{a.filterUpdatesNot(b)} is equivalent to @jcode-inline{a.filterUpdates(b.not())}.

@jcode{
    BooleanCell unlocked = new BooleanCell();
    BooleanInput lockable = original.filterUpdates(unlocked);
    // ...
    unlocked.set(true);

    original.set(true);  // lockable is now true
    original.set(false); // lockable is now false
    original.set(true);  // lockable is now true
    original.set(false); // lockable is now false
    original.set(true);  // lockable is now true

    unlocked.set(false); // lockable is still true

    original.set(false); // lockable is still true
    original.set(true);  // lockable is still true
    original.set(false); // lockable is still true

    unlocked.set(true);  // lockable is now false

    original.set(true);  // lockable is now true
    original.set(false); // lockable is now false

    unlocked.set(false); // lockable is still false
    original.set(true);  // lockable is still false
}

@subsubsection{FloatOutputs}

@jmethod[FloatOutput (FloatOutput negate) "setup"]

This is an negated version of the original @jcode-inline{FloatOutput}.

@jcode{
    FloatOutput inv = a.negate();
    // so then this is equivalent
    inv.set(1.0f);
    // to this:
    a.set(-1.0f);
}

@jmethod[FloatOutput (FloatOutput combine) (FloatOutput other) "setup"]

This combines two @jcode-inline{FloatOutput}s into a single @jcode-inline{FloatOutput}, so that setting the result to a value would set both of the original outputs to that value.

@jcode{
    FloatOutput merged = a.combine(b);
    // then this would be equivalent:
    merged.set(0.3f);
    // to this:
    a.set(0.3f);
    b.set(0.3f);
}

@jmethod*[(EventOutput (FloatOutput eventSet) (float value))
          (EventOutput (FloatOutput eventSet) (FloatInput value)) "setup"]

This provides an @jcode-inline{EventOutput} that sets this @jcode-inline{FloatOutput} to some value, either a constant value or a value fetched in the moment from a @jcode-inline{FloatInput}.

@jcode{
    EventOutput o = a.eventSet(0.7f);
    // and then:
    o.event();
    // is equivalent to:
    a.set(0.7f);
}

With a @jcode-inline{FloatInput} parameter:

@jcode{
    EventOutput o = a.eventSet(input);
    // and then:
    o.event();
    // is equivalent to:
    a.set(input.get());
}

@jmethod*[(void (FloatOutput setWhen) (float value) (EventInput when))
          (void (FloatOutput setWhen) (FloatInput value) (EventInput when))
          "setup"]

When @jcode-inline{when} fires, this @jcode-inline{FloatOutput} will be set to @jcode-inline{value} or @jcode-inline{value.get()}, depending on type.

@jcode{
    EventCell event = new EventCell();
    output.setWhen(0.3f, event);
    // and then:
    event.event();
    // is equivalent to:
    output.set(0.3f);
}

With a @jcode-inline{FloatInput} parameter:

@jcode{
    EventCell event = new EventCell();
    output.setWhen(input, event);
    // and then:
    event.event();
    // is equivalent to:
    output.set(input.get());
}

@jmethod[FloatOutput (FloatOutput outputDeadzone) (float deadzone) "setup"]

Provides a @jcode-inline{FloatOutput} whose values go through a deadzone filter before reaching the original @jcode-inline{FloatOutput}.

@margin-note{Usually, you want to apply deadzones on your inputs rather than outputs. See @jcode-inline{FloatInput.$deadzone} for the equivalent @jcode-inline{FloatInput} version of this method.}

The idea of a deadzone is that it chops out values close to zero.

Usually, a centered Joystick gives values of something like 0.047, not 0.000. You often want to interpret this as zero, because - for example - you usually might not want motors to move in this case.

By using a deadzone of 0.1, any values from around -0.09999999 to around 0.09999999 are converted to exactly zero.

@jmethod[FloatOutput (FloatOutput addRamping) (float limit) (EventInput updateWhen) "setup"]

Adds a ramping layer on top of a @jcode-inline{FloatOutput}.

The idea of ramping is that immediately changing from, for example, 0 feet per second to 16 feet per second can be very stressing on a robot's drivetrain - or other similar components. You usually want to "ramp up" to the desired speed, for example at four feet per second per second.

You configure ramping with a maximum delta and an event to update ramping. The idea is that whenever @jcode-inline{updateWhen} is fired, the actual output is moved closer to the most recent target value. The maximum amount by which it can change is @jcode-inline{limit}.

A recommended ramping setup, in absence of actual testing, is @jcode-inline{addRamping(0.1f, FRC.constantPeriodic)}. Since @jcode-inline{FRC.constantPeriodic} is a ten-millisecond loop, this means that it takes 100 milliseconds to go from stopped to full speed. You may need to tweak this lower or higher based on actual testing.

@jmethod[FloatOutput (FloatOutput viaDerivative) "setup"]

This provides a @jcode-inline{FloatOutput} such that the derivatives of the data sent to it are sent to the original @jcode-inline{FloatOutput}.

@margin-note{You probably want to use @jcode-inline{FloatInput.$derivative} instead. It usually makes more sense.}

For those of you who don't know calculus, this essentially converts a position to a speed. So you could send the total count of encoder ticks through @jcode-inline{viaDerivative} and you would get the encoder speed.

@jcode{
    FloatCell speed = new FloatCell();
    encoder.send(speed.viaDerivative());
}

@bold{WARNING}: There is a bug in the implementation that is very hard to solve. Usually, a sensor position "wiggles" slightly due to measurement noise. However, if this doesn't happen, the derivative doesn't know when to update the speed, and the speed will perpetually be the last measured speed.
It's hard to find a way to fix this, but we're working on it. There is a workaround:

@jcode{
    FloatCell speed = new FloatCell();
    speed.viaDerivative().setWhen(encoder, FRC.sensorPeriodic);
}

@jmethod*[(FloatOutput (FloatOutput filter) (FloatInput allow))
          (FloatOutput (FloatOutput filterNot) (FloatInput deny)) "setup"]

@jcode-inline{filter} filters a @jcode-inline{FloatOutput} so that it only changes when @jcode-inline{allow.get() == true}.

@jcode-inline{filterNot} filters a @jcode-inline{FloatOutput} so that it only changes when @jcode-inline{deny.get() == false}.

As long as @jcode-inline{allow.get() == true}, each change to the returned @jcode-inline{FloatOutput} will modify the original @jcode-inline{FloatOutput}.

When @jcode-inline{allow} changes to @jcode-inline{false}, the current value is preserved.

As long as @jcode-inline{allow.get() == false}, each change to the returned @jcode-inline{FloatOutput} will be remembered, but not propagated.

When @jcode-inline{allow} changes to @jcode-inline{true}, the last remembered value will be propagated.

@jcode{
    FloatOutput filtered = original.filter(allow);
    // ...
    allow.set(true);
    filtered.set(0.3f); // equivalent to original.set(0.3f);
    filtered.set(0.0f); // equivalent to original.set(0.0f);
    filtered.set(0.6f); // equivalent to original.set(0.6f);
    allow.set(false);
    filtered.set(0.3f); // does nothing
    filtered.set(0.0f); // does nothing
    filtered.set(0.5f); // does nothing
    allow.set(true); // now does original.set(0.5f);
}

@jcode-inline{original.filterNot(deny)} is equivalent to @jcode-inline{original.filter(deny.not())}.

@jmethod*[(BooleanOutput (FloatOutput fromBoolean) (float off) (float on))
          (BooleanOutput (FloatOutput fromBoolean) (float off) (FloatInput on))
          (BooleanOutput (FloatOutput fromBoolean) (FloatInput off) (float on))
          (BooleanOutput (FloatOutput fromBoolean) (FloatInput off) (FloatInput on))
          "setup"]

@jcode-inline{fromBoolean} provides a @jcode-inline{BooleanOutput} that controls a @jcode-inline{FloatOutput} with a value selected from one of two @jcode-inline{float}s or @jcode-inline{FloatInput}s.

@jcode{
    FloatCell output = new FloatCell(0.3f);
    BooleanOutput converted = output.fromBoolean(0.0f, 0.75f);
    // ...

    // converted is 0.3
    converted.set(true);
    // converted is 0.75
    converted.set(false);
    // converted is 0.0
    converted.set(true);
    // converted is 0.75
    converted.set(false);
    // converted is 0.0
}

@jfield[FloatOutput static (FloatOutput ignored) "setup"]

This is a FloatOutput that ignores all values sent to it.

@jcode{
    FloatOutput.ignored.set(1.0f);
    // is equivalent to absolutely nothing!
}

@subsubsection{FloatInputs}

@jmethod[FloatInput static (FloatInput always) (float value) "setup"]

Provides a @jcode-inline{FloatInput} that is always equal to @jcode-inline{value} and never changes.

@jcode{
    FloatInput i = FloatInput.always(17.0f);
    // ...
    i.get(); // 17.0f
}

@jfield[FloatInput static (FloatInput zero) "setup"]

A @jcode-inline{FloatInput} that is always equal to zero. Equivalent to @jcode-inline{FloatInput.always(0)}.

@jcode{
    FloatInput i = FloatInput.zero;
    // ...
    i.get(); // 0.0f
}

@jmethod*[(FloatInput (FloatInput plus) (FloatInput other))
          (FloatInput (FloatInput plus) (float other))
          (FloatInput (FloatInput minus) (FloatInput other))
          (FloatInput (FloatInput minus) (float other))
          (FloatInput (FloatInput minusRev) (FloatInput other))
          (FloatInput (FloatInput minusRev) (float other))
          (FloatInput (FloatInput multipliedBy) (FloatInput other))
          (FloatInput (FloatInput multipliedBy) (float other))
          (FloatInput (FloatInput dividedBy) (FloatInput other))
          (FloatInput (FloatInput dividedBy) (float other))
          (FloatInput (FloatInput dividedByRev) (FloatInput other))
          (FloatInput (FloatInput dividedByRev) (float other))
          "setup"]

These arithmetic methods allow you to perform arithmetic on the values of @jcode-inline{FloatInput}s.

For example, @jcode-inline{a.plus(b)} has the value of @jcode-inline{a.get() + b.get()} or @jcode-inline{a.get() + b} depending on which version you use.

The @jcode-inline{Rev} methods reverse the order of the operands. Since addition and multiplication are commutative, @jcode-inline{plusRev} and @jcode-inline{multipliedByRev} are unnecessary and do not exist.

@jcode{
    a.plus(b) // a + b
    a.minus(b) // a - b
    a.minusRev(b) // b - a
    a.multipliedBy(b) // a * b
    a.dividedBy(b) // a / b
    a.dividedByRev(b) // b / a
}

An example:

@jcode{
    FloatInput real_drive_speed =
        original_drive_speed.multipliedBy(is_kid_mode.toFloat(0.5f, 1.0f));
    // and then:
    real_drive_speed.get()
    // is either
    original_drive_speed.get() * 1.0f
    // or
    original_drive_speed.get() * 0.5f
}

@jmethod*[(BooleanInput (FloatInput atLeast) (float minimum))
          (BooleanInput (FloatInput atLeast) (FloatInput minimum))
          (BooleanInput (FloatInput atMost) (float maximum))
          (BooleanInput (FloatInput atMost) (FloatInput maximum))
          (BooleanInput (FloatInput outsideRange) (float minimum) (float maximum))
          (BooleanInput (FloatInput outsideRange) (float minimum) (FloatInput maximum))
          (BooleanInput (FloatInput outsideRange) (FloatInput minimum) (float maximum))
          (BooleanInput (FloatInput outsideRange) (FloatInput minimum) (FloatInput maximum))
          (BooleanInput (FloatInput inRange) (float minimum) (float maximum))
          (BooleanInput (FloatInput inRange) (float minimum) (FloatInput maximum))
          (BooleanInput (FloatInput inRange) (FloatInput minimum) (float maximum))
          (BooleanInput (FloatInput inRange) (FloatInput minimum) (FloatInput maximum))
          "setup"]

These comparison methods allow you to compare a channel against fixed or channel-based values.

@jcode-inline{a.atLeast(b)} has the value of @jcode-inline{a.get() >= b}.
@jcode-inline{a.atMost(b)} has the value of @jcode-inline{a.get() <= b}.
@jcode-inline{a.outsideRange(b,c)} has the value of @jcode-inline{a.get() < b || a.get() > c}.
@jcode-inline{a.inRange(b,c)} has the value of @jcode-inline{b <= a.get() && a.get() <= c}.

@jcode{
    BooleanInput low_on_pressure = pressure.atMost(40); // we want at least 40 psi of 120 max psi
    // and then
    low_on_pressure.get()
    // is equivalent to:
    pressure.get() <= 40
}

@jmethod[FloatInput (FloatInput negated) "setup"]

Provides a @jcode-inline{FloatInput} that is the negated version of this @jcode-inline{FloatInput}.

@jcode{
    FloatInput negated = original.negated();
    // and then:
    negated.get()
    // is equivalent to
    -original.get()
}

@jmethod[EventInput (FloatInput onChange) "setup"]

Provides an @jcode-inline{EventInput} that fires whenever this @jcode-inline{FloatInput} changes by any amount.

@jcode{
    FloatCell cell = new FloatCell(0);
    EventInput change = cell.onChange();
    // ...
    cell.set(3.2f); // causes change to be produced
    cell.set(3.2f); // nothing
    cell.set(0.0f); // causes change to be produced
}

@jmethod[EventInput (FloatInput onChangeBy) (float magnitude) "setup"]

Provides an @jcode-inline{EventInput} that fires whenever this @jcode-inline{FloatInput} changes by at least @jcode-inline{magnitude} from the last time that it changed.

@jcode{
    FloatCell cell = new FloatCell(0);
    EventInput change = cell.onChangeBy(1.0f);
    // ...
    cell.set(1.1f); // change fires once
    cell.set(30.0f); // change fires once
    cell.set(29.9f); // no fire
    cell.set(29.1f); // no fire
    cell.set(29.0f); // change fires once (because it is exactly one less than 30.0f)
    cell.set(29.9f); // no fire
    cell.set(28.1f); // no fire
    cell.set(27.9f); // change fires once
}

@jmethod[FloatInput (FloatInput deadzone) (float deadzone) "setup"]

Provides a @jcode-inline{FloatInput} with the value of the original @jcode-inline{FloatInput}, but with a deadzone applied.

The idea of a deadzone is that it chops out values close to zero.

Usually, a centered Joystick gives values of something like 0.047, not 0.000. You often want to interpret this as zero, because - for example - you usually might not want motors to move in this case.

By using a deadzone of 0.1, any values from around -0.09999999 to around 0.09999999 are converted to exactly zero.

@jcode{
    FloatInput deadzoned = original.deadzone(0.1f);
    // ...
    original.set(1.0f); // deadzoned is now 1.0f
    original.set(0.1f); // deadzoned is now 0.1f
    original.set(0.0999f); // deadzoned is now 0.0f
    original.set(0.05f); // deadzoned is still 0.0f
    original.set(0.0f); // deadzoned is still 0.0f
    original.set(-0.05f); // deadzoned is now 0.0f
    original.set(-0.0999f); // deadzoned is still 0.0f
    original.set(-0.1f); // deadzoned is now -0.1f
    original.set(-1.0f); // deadzoned is now -1.0f
}

@jmethod*[(FloatInput (FloatInput normalize) (float zeroV) (float oneV))
          (FloatInput (FloatInput normalize) (float zeroV) (FloatInput oneV))
          (FloatInput (FloatInput normalize) (FloatInput zeroV) (float oneV))
          (FloatInput (FloatInput normalize) (FloatInput zeroV) (FloatInput oneV))
          "setup"]

Linearly maps from two configurable values to the range of zero to one. @jcode-inline{zeroV} becomes @jcode-inline{0.0f}, and @jcode-inline{oneV} becomes @jcode-inline{1}. @jcode-inline{(zeroV + oneV) / 2} would become @jcode-inline{0.5f}. This linear map extends through all of the real numbers: it is converted into simply an addition and a multiplication.

You can specify two constant values, two values based on channels, or a combination of the two.

If you had a pressure sensor that outputted 1.1 Volts for an empty tank, and 4.6 Volts for a full tank, you can map these to the range 0 to 1, and use them as a fraction.

You could say:

@jcode{
    FloatInput sensor = /* ... */;
    FloatInput pressureFraction = sensor.normalize(1.1f, 4.6f);
    // pressureFraction would be 0 when the sensor's voltage is 1.1 volts,
    // and it would be 1 when the sensor's voltage is 4.6 volts.
    // if the sensor reported 4.8 volts, the fraction would be about 1.06.
}

@jmethod[FloatInput (FloatInput withRamping) (float limit) (EventInput updateWhen) "setup"]

Adds a ramping layer on top of a @jcode-inline{FloatInput}.

The idea of ramping is that immediately changing from, for example, 0 feet per second to 16 feet per second can be very stressing on a robot's drivetrain - or other similar components. You usually want to "ramp up" to the desired speed, for example at four feet per second per second.

You configure ramping with a maximum delta and an event to update ramping. The idea is that whenever @jcode-inline{updateWhen} is fired, the actual output is moved closer to the most recent target value. The maximum amount by which it can change is @jcode-inline{limit}.

A recommended ramping setup, in absence of actual testing, is @jcode-inline{withRamping(0.1f, FRC.constantPeriodic)}. Since @jcode-inline{FRC.constantPeriodic} is a ten-millisecond loop, this means that it takes 100 milliseconds to go from stopped to full speed. You may need to tweak this lower or higher based on actual testing.

@jmethod[EventOutput (FloatInput createRampingEvent) (float limit) (FloatOutput target) "setup"]

Provides an event that performs incremental ramping based on this @jcode-inline{FloatInput} and controlling @jcode-inline{target} as an output.

The idea of ramping is that immediately changing from, for example, 0 feet per second to 16 feet per second can be very stressing on a robot's drivetrain - or other similar components. You usually want to "ramp up" to the desired speed, for example at four feet per second per second.

You configure ramping with a maximum delta and an event to update ramping. The idea is that whenever the returned @jcode-inline{EventOutput} is fired, the actual output is moved closer to the most recent target value. The maximum amount by which it can change is @jcode-inline{limit}.

It's usually easier to use @jcode-inline{FloatInput.withRamping} or @jcode-inline{FloatOutput.addRamping}, which are easier to use.

@jmethod*[(FloatInput (FloatInput filterUpdates) (BooleanInput allow))
          (FloatInput (FloatInput filterUpdatesNot) (BooleanInput deny))
          "setup"]

@jcode-inline{filterUpdates} provides a @jcode-inline{FloatInput} that follows the value of the original @jcode-inline{FloatInput} while @jcode-inline{allow.get() == true}, and holds the last value while @jcode-inline{allow.get() == false}.

@jcode-inline{a.filterUpdatesNot(b)} is equivalent to @jcode-inline{a.filterUpdates(b.not())}.

@jcode{
    BooleanCell unlocked = new BooleanCell();
    FloatInput lockable = original.filterUpdates(unlocked);
    // ...
    unlocked.set(true);

    original.set(5.0f);  // lockable is now 5.0f
    original.set(2.0f);  // lockable is now 2.0f
    original.set(-1.2f); // lockable is now -1.2f

    unlocked.set(false); // lockable is still -1.2f

    original.set(-0.5f); // lockable is still -1.2f
    original.set(6.1f);  // lockable is still -1.2f
    original.set(3.1f);  // lockable is still -1.2f

    unlocked.set(true);  // lockable is now false

    original.set(0.3f);  // lockable is now 0.3f
    original.set(0.8f);  // lockable is now 0.8f

    unlocked.set(false); // lockable is still false
    original.set(1.0f);  // lockable is still 0.8f
}

@subsection{Drive Code Implementations}

Drive Code is the part of robot code that controls the drive base in response to the state of the driver's Joystick.

There are three main types of drive code:

@itemlist[@item{Tank Drive}
          @item{Extended Tank Drive}
          @item{Arcade Drive}
          @item{Mecanum Drive}]

@jmethod[void (Drive tank) (FloatInput leftIn) (FloatInput rightIn) (FloatOutput leftOut) (FloatOutput rightOut) "setup"]

This sets up tank drive code for the given axes and motors.

In Tank Drive, the driver has two Joysticks or axes, which can be moved forward and back to control the sides of the robot independently: the left axis controls the left side of the robot, and the right axis controls the right side of the robot.

@jcode{
    FloatInput left_axis = FRC.joystick1.axisY();
    FloatInput right_axis = FRC.joystick2.axisY();
    FloatOutput left_motor = FRC.talon(0, FRC.MOTOR_FORWARD);
    FloatOutput right_motor = FRC.talon(1, FRC.MOTOR_REVERSE);
    // and then the key:
    Drive.tank(left_axis, right_axis, left_motor, right_motor);
}

This method is very simple, and is equivalent to:

@jcode{
    left_axis.send(left_motor);
    right_axis.send(right_motor);
}

@jmethod[void (Drive extendedTank) (FloatInput leftIn) (FloatInput rightIn) (FloatInput forward) (FloatOutput leftOut) (FloatOutput rightOut) "setup"]

This is similar to @jcode-inline{tankDrive}, but takes an additional @jcode-inline{forward} parameter that is added to the two axes, so that it can be used for exact movement forward and backward.

It is equivalent to:

@jcode{
    left_axis.plus(forward).send(left_motor);
    right_axis.plus(forward).send(right_motor);
}

@jmethod*[(void (Drive arcade) (FloatInput sideways) (FloatInput forward) (FloatOutput leftOut) (FloatOutput rightOut))
          (void (Drive arcade) (Joystick joystick) (FloatOutput leftOut) (FloatOutput rightOut))
          "setup"]

This sets up two axes to control the robot with Arcade Drive.

Arcade Drive is also known as single-joystick drive: it allows the robot to be controlled by being pushed in any direction. Forward for forward, backward for backward, left to turn left, and right to turn right. This is done based on one axis for left-right, and one axis for forward-backward. For some, it is the most intuitive control scheme.

@jcode{
    FloatInput sideways_axis = FRC.joystick1.axisX();
    FloatInput forward_axis = FRC.joystick1.axisY();
    FloatOutput left_motor = FRC.talon(0, FRC.MOTOR_FORWARD);
    FloatOutput right_motor = FRC.talon(1, FRC.MOTOR_REVERSE);
    Drive.arcade(sideways_axis, forward_axis, left_motor, right_motor);
}

The second form of this method is the same as the first, but it uses the X and Y axis from a single Joystick.

@jcode{
    Drive.arcade(FRC.joystick1, left_motor, right_motor);
}

This piece of drive code is fairly simple, and is equivalent to:

@jcode{
    forward.plus(sideways).send(leftOut);
    forward.minus(sideways).send(rightOut);
}

@jmethod[void (Drive mecanum) (FloatInput forward) (FloatInput strafe) (FloatInput rotate)
         (FloatOutput leftFrontMotor) (FloatOutput leftBackMotor) (FloatOutput rightFrontMotor) (FloatOutput rightBackMotor) "setup"]

This sets up Mecanum Drive on four motors with channels to control forward-backward, left-right (strafe), and left-right (rotate.)

Mecanum drive works with very special wheels (mecanum wheels) to allow the robot to maneuver itself in any direction - not just forward, backward, and rotating. The code to do so is complicated, but it's all included in this method.

The @jcode-inline{forward} and @jcode-inline{strafe} channels control motion in the XY plane, and the @jcode-inline{rotate} channel controls rotation.

@jcode{
    FloatInput strafe_axis = FRC.joystick1.axisX();
    FloatInput forward_axis = FRC.joystick1.axisY();
    FloatInput rotate_axis = FRC.joystick2.axisX();
    // ... motors ...
    Drive.mecanum(forward_axis, strafe_axis, rotate_axis,
        left_front_motor, left_back_motor, right_front_motor, right_back_motor);
}

@subsection[#:tag "autonomous"]{Autonomous and Instinct Modules}

In autonomous mode, your code tends to need behavior somewhat different from teleoperated code.
While control of actuators usually still needs the same dataflow setup, the sequencing required for autonomous mode is not easy to implement with dataflow.

Therefore, the CCRE provides an imperative autonomous mode system, using units of imperative code called @jcode-inline{InstinctModule}s.

@margin-note{An "imperative block" is the same as a flow block, but can also use methods marked as "imperative" methods, which are methods that may pause execution.}

@jcode[#:box (list "setup" #f #f "imperative block")]|{
        FRC.registerAutonomous(new $InstinctModule() {
            @Override
            protected void autonomousMain() throws Throwable {
                // Autonomous code goes here.
            }
        });
}|

This is the basic structure of your autonomous code, for when you only need a single sequence. Your imperative code goes in the internal block.
The important difference about code here is that it is allowed to "block", or wait for an event to occur. Normal flow mode code cannot wait at all.
To allow this to work, InstinctModule bodies run in separate threads.

When autonomous mode ends, your code will get aborted by an @jcode-inline{AutonomousModeOverException} or an @jcode-inline{InterruptedException}. Do not attempt to handle these exceptions.

An example of an autonomous mode:

@jcode|{
        FRC.registerAutonomous(new InstinctModule() {
            @Override
            protected void autonomousMain() throws Throwable {
                leftMotor.set(0.5f);
                rightMotor.set(0.5f);
                waitForTime(5000); // milliseconds
                leftMotor.set(0.0f);
                rightMotor.set(0.0f);
            }
        });
}|

This example autonomous mode runs drive motors forward at half speed for five seconds.

@subsubsection{Autonomous methods}

There are a variety of methods available that let your code wait for various conditions. While these methods are waiting, the rest of your code (that is outside of this @jcode-inline{InstinctModule}) will continue normally.

These methods can only be used from within an InstinctModule.

@jmethod*[(void (waitForTime) (long milliseconds))
          (void (waitForTime) (FloatInput seconds))
          "imperative"]

This method waits for @jcode-inline{milliseconds} milliseconds to elapse before continuing. Note that most waits, especially short waits, will be overapproximated: they will in practice wait slightly longer than you expect.

The second form, which takes a @jcode-inline{FloatInput}, is approximately equivalent to @jcode-inline{waitForTime(seconds.get() * 1000)}. It exists entirely for convenience when using variable delays. Note that any changes to the value of the @jcode-inline{FloatInput} that occur while this method is waiting will be ignored.

@jcode{
    piston.set(true);
    waitForTime(1000); // 1000 milliseconds = one second
    piston.set(false);
}

This example autonomous mode will extend a piston for one second, and then retract it.

@jmethod*[(void (waitUntil) (BooleanInput waitFor))
          (boolean (waitUntil) (long timeout) (BooleanInput waitFor))
          (void (waitUntilNot) (BooleanInput waitFor))
          (boolean (waitUntilNot) (long timeout) (BooleanInput waitFor))
          "imperative"]

@jcode-inline{waitUntil} waits until @jcode-inline{BooleanInput}'s value is @jcode-inline{true}. It is not guaranteed to resume if the value changes to @jcode-inline{true} and very quick changes back to @jcode-inline{false}.

The second form of @jcode-inline{waitUntil} is similar, but will also stop waiting once @jcode-inline{timeout} seconds have ellapsed. It will return @jcode-inline{true} if the condition became true, and @jcode-inline{false} if it timed out instead.

@jcode-inline{waitUntilNot} is the inverse: it waits until a @jcode-inline{BooleanInput}'s value is @jcode-inline{false}.

@jcode{
    drive_motors.set(0.3f);
    waitUntil(bumper_sensor);
    drive_motors.set(0.0f);
}

Given that @jcode-inline{bumper_sensor} has been defined outside of the InstinctModule as a touch sensor on the robot, this example autonomous mode will drive forward until it hits something.

@jmethod[void (waitForEvent) (EventInput source) "imperative"]

This method waits for a specific event to fire. If the event fires before this method is called, it will be ignored.

@jcode{
    drive_motors.set(0.3f);
    waitForEvent(stop_button);
    drive_motors.set(0.0f);
}

@jmethod*[(int (waitUntilOneOf) (BooleanInput waitFor) #:vararg)
          (int (waitUntilOneOf) (long timeout) (BooleanInput waitFor) #:vararg)
          "imperative"]

This method waits until one of a list of @jcode-inline{BooleanInput}s becomes @jcode-inline{true}.
It also takes an optional timeout after which it will return regardless of the values of any of the @jcode-inline{BooleanInput}s.

It returns the index of the @jcode-inline{BooleanInput} that became @jcode-inline{true}, starting at zero, or @jcode-inline{-1} on a timeout.

@jcode{
    switch (waitUntilOneOf(5000, left_bumper, right_bumper)) {
    case -1: // timeout
        // ...
        break;
    case 0: // left_bumper
        // ...
        break;
    case 1: // right_bumper
        // ...
        break;
    }
}

@jmethod*[(void (waitUntilAtLeast) (FloatInput waitFor) (float minimum))
          (void (waitUntilAtMost) (FloatInput waitFor) (float maximum))
          "imperative"]

These methods wait for a @jcode-inline{FloatInput} to satisfy a particular comparison: either at least or at most some value.

@jcode{
    waitUntilAtLeast(air_pressure, 70.0f); // wait for at least 70 psi in tank
}

@subsubsection{A Dire Warning}

Note, very specifically, that you cannot use setup mode methods inside an @jcode-inline{InstinctModule}! This means that the following is NOT ALLOWED:

@jcode|{
        FRC.registerAutonomous(new InstinctModule() {
            @Override
            protected void autonomousMain() throws Throwable {
                // ...
                // THIS IS NOT ALLOWED. DO NOT EVER DO THIS.
                waitUntil(some_condition.not());
                // ...
            }
        });
}|

You will have major issues if you attempt to do this. Instead, try this:

@jcode|{
        BooleanInput not_some_condition = some_condition.not();
        FRC.registerAutonomous(new InstinctModule() {
            @Override
            protected void autonomousMain() throws Throwable {
                // ...
                // This is allowed.
                waitUntil(not_some_condition);
                // ...
            }
        });
}|

In this specific case, the following would be recommended instead:

@jcode|{
        FRC.registerAutonomous(new InstinctModule() {
            @Override
            protected void autonomousMain() throws Throwable {
                // ...
                // This is recommended.
                waitUntilNot(some_condition);
                // ...
            }
        });
}|

@subsubsection{Instinct Modules outside of Autonomous}

You can also use InstinctModules in other contexts besides autonomous modes, because they can be useful for semi-automatic control of parts of a robot.

@jcode[#:box (list #f "setup" #f #f "imperative block")]|{
        BooleanCell run_this_instinct_module = new BooleanCell(false);
        new $InstinctModule(run_this_instinct_module) {
            @Override
            protected void autonomousMain() throws Throwable {
                // Automatic code goes here.
            }
        });
}|

You can pass any @jcode-inline{BooleanInput} you want to the constructor, and the module will run while that @jcode-inline{BooleanInput} is @jcode-inline{true}.

@jmethod[void (InstinctModule setShouldBeRunning) (BooleanInput when) "setup"]

Equivalently to the previous example, you can construct an @jcode-inline{InstinctModule} without any parameters and then call @jcode-inline{setShouldBeRunning} later with the same value.

This means that @jcode-inline{FRC.registerAutonomous(module)} is actually just a call to @jcode-inline{module.setShouldBeRunning}!

@subsubsection{Instinct MultiModules}

In progress.

@subsection[#:tag "hardware-access"]{Hardware Access}

In progress.

@subsection{Logging Framework}

In progress.

@subsection{Concurrency Best Practices}

In progress.

@subsection{StateMachine}

In progress.

@subsection{PID controllers}

In progress.

@subsection{Time and Timing}

In progress. (Including Time, scheduling, Ticker, ExpirationTimer, PauseTimer.)

@subsection{Extended Motors and CAN}

In progress.

@subsection{Remote Configuration}

In progress.

@subsection{Error Handling}

In progress.

@subsection{Control Bindings}

In progress.

@subsection{Tunable Values}

In progress.

@subsection[#:tag "deployment"]{Deployment}

How does this work behind the scenes?

First, this goes through the DeploymentEngine, which dispatches to the @jcode-inline{deploy()} DepTask in the default Deployment class:

@jcode|{
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

Sometimes, the robot breaks and you need to figure out what's going on. I'm not going to go into all of the details of this @;{ (TODO: go into all the details on this) } but I'll overview how you start.

You can connect to the roboRIO over SSH (aka Secure SHell) - you simply point your SSH client at roboRIO-NNNN.local (where NNNN is your team number) and enter the correct username and password.

On Linux or Mac OS X, you can do this from the command line with pre-installed tools:

@margin-note{On Windows, you can use PuTTY.}
@codeblock|{
 $ ssh admin@roboRIO-1540.local
 Password:
 $ do stuff on the robot
}|

@margin-note{If you are familiar with nix-like systems, it may surprise you that the superuser account (uid 0) is named 'admin', not 'root'.}

The default password is the blank password, and the username is either 'admin' or 'lvuser'.

Once you're on the robot, you want to look in @code{/home/lvuser}, where you should find the user program and some related files, including the most recent log files.

@subsubsection{Deployment API}

In progress.

@subsection{Storage Framework}

In progress.

@subsection{Serial I/O}

In progress.

@subsection{Networking Framework}

In progress.

@subsection{Phidget control panels}

In progress.

@subsection{Special sensors}

(UM7LT.) In progress.

@subsection[#:tag "cluck"]{The Cluck Pub/Sub System}

In progress.

@subsubsection{Cluck with complexity}

In progress.

@subsection[#:tag "poultry-inspector"]{Detailed guide to the Poultry Inspector}

In progress.

@subsection{Detailed guide to the Emulator}

In progress.

@section{CCRE recipes}

In progress.

@section{Versioning policies of the CCRE}

In progress.

@section{Javadoc Link}

You can @link["http://cgscomwww.catlin.edu/~skeggsc/ccre3/"]{browse the Javadoc}!

@section{Maintainer's guide}

In progress.

@subsection{Hardware access}

The CCRE provides interfaces to the underlying hardware via WPILib's JNI layer, which attaches to the WPILib Hardware Abstraction Layer (in C++), which attaches to the NI ChipObject proprietary library, which attaches to the NiFPGA interface to the FPGA (field-programmable gate array) device that manages the communication between the higher-level code and the I/O ports.

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

@subsection{CCRE Philosophy}

In progress.
