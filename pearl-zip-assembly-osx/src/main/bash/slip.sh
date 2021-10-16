#!/bin/bash
#
# Copyright © 2021 92AK
#

# DESCRIPTION: Slips text from file at specified anchor point in the source file.
# By Aashutos Kakshepati

# Initialise
SRC_FILE=$1
NEW_FILE=$2
PATTERN=$3
EXTRA_FILE=$4
OFFSET=${5:-1}

echo "$SRC_FILE $NEW_FILE $PATTERN $EXTRA_FILE $OFFSET"

topLines=$(expr $(cat $SRC_FILE | grep "$PATTERN" -n | cut -d: -f1 | head -1) + $OFFSET)
total=$(cat $SRC_FILE | wc -l)
bottomLines=$(expr $total - $topLines)

echo "Top: $topLines; Bottom: $bottomLines"

# Add header files
head -n $topLines $SRC_FILE > $NEW_FILE
# Add extra lines
cat $EXTRA_FILE >> $NEW_FILE
# Add bottom lines
tail -n $bottomLines $SRC_FILE >> $NEW_FILE
