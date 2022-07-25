#!/bin/bash
#
# Copyright © 2022 92AK
#

P_SETTINGS=$1

echo "Settings file: $P_SETTINGS"
while read line; do
  if [ $(echo "$line" | grep "=" | wc -l) == 1 ]
  then
    echo "Setting environment variable: $(echo $line | cut -d= -f1)..."
    key=$(echo $line | cut -d= -f1)
    value=$(echo $line | cut -d= -f2-)
    export P_$key="$value"
  fi
done < $P_SETTINGS

echo "Settings initialised."
