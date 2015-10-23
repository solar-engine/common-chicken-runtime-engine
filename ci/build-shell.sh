#!/bin/bash -e

DIRS="CommonChickenRuntimeEngine DeploymentEngine Emulator PoultryInspector roboRIO"
HERE=$(pwd)

for dir in $DIRS
do
	cd $(dirname $HERE)/$dir
	ant
done

cd $(dirname $HERE)/CommonChickenRuntimeEngine
rm -rf $HERE/junit-output
mkdir $HERE/junit-output
#if
ant test -Djunit.dir=$HERE -Djunit-output.dir=$HERE/junit-output
#then
#	echo "Success!"
#else
#	echo "Failure!"
#	exit 1
#fi

#	ls $HERE/junit-output
#	echo
#	for file in $HERE/junit-output/*
#	do
#		echo "====> $file"
#		cat $file
#	done


cd $HERE

echo "Done!"

