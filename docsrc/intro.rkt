#lang scribble/manual

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

@margin-note{This code snippet assumes that you have a Talon speed controller on PWM port 0, which might not be correct for your actual robot. Ignore @code{FRC.MOTOR_FORWARD} for now.}

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

In progress.

@section{Review of advanced Java concepts}

In progress.

@section{Detailed guide to the CCRE}

In progress.

@section{Versioning policies of the CCRE}

In progress.

@section{API reference}

In progress.

@section{Maintainer's guide}

In progress.
