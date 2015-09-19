#lang scribble/manual

@title{The Common Chicken Runtime Engine v3.0.0-pre1}

Welcome to the CCRE's documentation! This guide will introduce you to the basics of the CCRE.

You will find:

@itemlist[@item{Quickstart Guide}
          @item{Introduction to Dataflow}
          @item{Review of advanced Java concepts}
          @item{Conceptual Guide}]

@section{Quickstart Guide}

@subsection{Prerequisites}

@itemlist[@item{You have to know how to program in Java. (You don't have to know all of the ins-and-outs of Java, but you have to have a solid basic understanding.)}
          @item{You have to have the latest version of @hyperlink["http://www.eclipse.org/"]{Eclipse} installed.}
          @item{You need to have the Java 8 JDK (Java Development Kit) installed.}]

@subsection{Installing the CCRE}

Short version: import all of the projects from the CCRE repository into Eclipse and Build All.

Long version:

@itemlist[@item{Download @hyperlink["https://github.com/flamingchickens1540/Common-Chicken-Runtime-Engine/releases"]{the latest CCRE release}. Download "Source code (zip)" or "Source code (tar.gz)" depending on which one you can open. }
          @item{Extract that archive into a directory of your choice.}
          @item{Open Eclipse and select a new workspace.}
          @item{Go to File -> Import... and select Existing Projects into Workspace.}
          @item{Choose the directory where you extracted the archive to.}
          @item{Select all of the CCRE projects and press finish.}
          @item{Press Project -> Build All}]

@subsection{Creating a new project}

Creating a new project is easy.

Simply copy and paste the TemplateRobot project, and give it a new name like MyFirstRobot.

Open src/robot/RobotTemplate.java in your new project and replace 0000 with your team number.

Now press Project -> Build Project, and you're done once it finishes!

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

Now, let's test this code in the emulator. Press the dropdown next to the External Tools button:

@image["runas.png"]{run as}

and select "MyFirstRobot Emulate."

On the emulator window that pops up, you should see it say something like
@codeblock|{
    [INFO] (RobotTemplate.java:20) Hello, World!
}|

If so, congratulations! You appear to have installed the CCRE correctly and written a very simple program!

Of course, that's not particularly interesting, so let's move on to actual motors and joysticks!

@subsection{Single Joystick, Single Motor}

