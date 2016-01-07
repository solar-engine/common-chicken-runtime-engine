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

echo Testing...

rm -rf $HERE/junit-output
mkdir $HERE/junit-output

cd $(dirname $HERE)/CommonChickenRuntimeEngine
if ant test -Djunit.dir=$HERE -Djunit-output.dir=$HERE/junit-output
then
	echo "Success!"
else
	echo "Failure!"
	exit 1
fi

cd $(dirname $HERE)/PoultryInspector
if ant test -Djunit.dir=$HERE -Djunit-output.dir=$HERE/junit-output
then
	echo "Success!"
else
	echo "Failure!"
	exit 1
fi

cd $HERE

if [ "$TRAVIS_PULL_REQUEST" = "false" -a "$TRAVIS_BRANCH" = "devel-3.x.x" ]
then
	echo "Cloning uploader repo..."
	ssh-agent ./upload-artifacts.sh $(dirname $HERE)
	echo "Done uploading!"
else
	echo "Not uploading for pull request or non-devel branch."
fi
