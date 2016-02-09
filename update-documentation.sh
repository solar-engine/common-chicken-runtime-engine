if grep -qF "ccre-version=ccre-v" version.properties
then
	echo "Release version!"
else
	echo "Not a release version! Update version.properties first."
	exit 1
fi

echo "This might take a bit."

./update-javadoc-site.sh || exit 1
./build.sh upload || exit 1

echo "Done!"
