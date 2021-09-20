#
# Copyright © 2021 92AK
#

rm -rf work
rm -rf out
mkdir work out

jdeps --ignore-missing-deps --multi-release 17 --generate-module-info work $1
cp "work/$(ls -1 work)/versions/17/module-info.java" $(pwd)/work/$(ls -1 work)/module-info.java
ls -l work
