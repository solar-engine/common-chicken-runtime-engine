#!/bin/bash -e

DIRS="CommonChickenRuntimeEngine Emulator DeploymentEngine PoultryInspector roboRIO"
HERE=$(pwd)

cd $(dirname $HERE)
echo Verifying headers...

./check-headers.sh
./check-headers-valid.sh --verify

echo Building...

for dir in $DIRS
do
	cd $(dirname $HERE)/$dir
	ant
done

dump_logs() {
	ls $HERE/junit-output
	echo
	for file in $HERE/junit-output/*
	do
		echo "====> $file"
		cat $file
	done
}

echo Testing...

cd $(dirname $HERE)/CommonChickenRuntimeEngine
rm -rf $HERE/junit-output
mkdir $HERE/junit-output
if ant test -Djunit.dir=$HERE -Djunit-output.dir=$HERE/junit-output
then
	dump_logs
	echo "Success!"
else
	dump_logs
	echo "Failure!"
	exit 1
fi

cd $HERE

echo "Done!"
