echo
echo Enumerating files...
echo
GOOD=true
for x in `find -name '*.java'`
do
	if echo $x | grep -iE '(SampleRobot|cobertura|TemplateRobot|edu.wpi.first.wpilib.networktables.java|edu/wpi/first/wpilibj/hal|edu/wpi/first/wpilibj/communication|edu/wpi/first/wpilibj/can)' >/dev/null
	then
		true
	else
		if head -n 18 $x | grep -iE '(This file is part of the CCRE, the Common Chicken Runtime Engine[.]|Copyright \(c\) FIRST)' >/dev/null
		then
			true
		else
			echo $x BAD
			GOOD=false
		fi
	fi
done
echo
if $GOOD
then
	echo Done enumerating. All files pass inspection.
	echo
	exit 0
else
	echo Done enumerating. Not ready to commit. You forgot some license headers!
	echo
	exit 1
fi
