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

@margin-note{Can you write helper code to make this easier? Yes, but it still gets messy.}

@vl-append[20
 (cell "define variable was_button_pressed")
 (cell "white true")
 (indent (cell "if red button is pressed AND not was_button_pressed") 40)
 (indent (cell "if key is turned") 80)
 (indent (cell "blow up the world") 120)
 (indent (cell "set was_button_pressed to whether or not the button is currently pressed") 40)
 (indent (cell "sleep for 20 milliseconds") 40)]

@margin-note{This is hard to scale for multiple reasons, including an inability to have @italic{inline state}. For each button, you need a variable defined in a completely different location in the file (which becomes more significant with larger files) and you have to reference multiple places to figure out what your code does.}

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
           Users to be notified when something happens.

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

@codeblock|{
  *****Input x = /* ... */;
  *****Output y = /* ... */;
  x.send(y); // connect the input to the output, so that y is updated with the current value of x both right now and whenever it changes.
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

@section{Review of advanced Java concepts}

In progress.

@subsection{Anonymous classes}

In progress.

@subsection{Lambdas}

In progress.

@section{Detailed guide to the CCRE}

In progress.

@subsection[#:tag "hardware-access"]{Hardware Access}

In progress.

@subsection[#:tag "remixing"]{Remixing}

In progress.

@subsection{Deployment}

In progress.

@subsection{The Cluck Pub/Sub System}

In progress.

@section{Versioning policies of the CCRE}

In progress.

@section{API reference}

In progress.

@section{Maintainer's guide}

In progress.
