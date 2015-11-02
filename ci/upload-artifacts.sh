#!/bin/bash -e

chmod 600 ssh-keys/travis-key
ssh-add ssh-keys/travis-key

git clone git@github.com:flamingchickens1540/ccre-dev-releases.git

cd ccre-dev-releases

./add-from-repo.sh $(dirname $HERE)
