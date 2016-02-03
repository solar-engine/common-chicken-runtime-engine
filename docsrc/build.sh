#!/bin/bash

set -e

# Scribble is a part of Racket.
scribble index.scrbl

BUILD=ccre-docs-dir

rm -rf $BUILD
mkdir $BUILD

cp *.png $BUILD
cp *.jpg $BUILD
cp *.html $BUILD
cp *.css $BUILD
cp *.js $BUILD

(cd $BUILD && tar -czf ../ccre-scr-doc.tgz .)

if [ "$1" = "upload" ]; then
	scp ccre-scr-doc.tgz cgscomwww.catlin.edu:ccre-scr-doc.tgz
	ssh cgscomwww.catlin.edu 'rm -r ccre-scr-doc && mkdir ccre-scr-doc && cd ccre-scr-doc && tar -xzf ../ccre-scr-doc.tgz && chmod 0755 . && chmod -R 0644 *'
	echo "Uploaded!"
else
	echo "See the '$BUILD' subdirectory."
fi
