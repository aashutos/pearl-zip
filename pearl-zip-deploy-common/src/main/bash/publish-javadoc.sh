#!/bin/sh
#
# Copyright © 2022 92AK
#

# Reading Parameters
VERSION=${1}
ROOT_FOLDER=${2:-LATEST}
JAVA_ROOT="${JAVA_HOME:+$JAVA_HOME/bin/}"

echo "VERSION=${VERSION}; ROOT_FOLDER=${ROOT_FOLDER}"

# Setting correct relative directory...
cd $(dirname ${BASH_SOURCE[0]})
cd ../../..
echo "Current directory: $(pwd)"

# Setting up the properties file...
source ../scripts/init-settings.sh "./src/main/resources/settings.properties"

# Does rsync exist on the environment
if [ "$(which rsync | echo $?)" -ne 0 ]
then
  echo "rsync has not been installed. Exiting..."
  exit 1
fi

# Does java exist
if [ $(${JAVA_ROOT}java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d. -f1) -lt 17 ]
then
  echo "Java >= 17 has not been installed. Exiting..."
  exit 1
fi

# Has java docs been generated
if [ -d "target/site/LATEST" ]
then
  echo "Java docs have not been generated. Calling maven job..."
  mvn javadoc:aggregate -X -f pom.xml
fi

# Clear existing folder in target location
ssh $P_JAVADOC_HOST "chmod -R 777 public_html/pz-api/${ROOT_FOLDER}"
ssh $P_JAVADOC_HOST "rm -rf public_html/pz-api/${ROOT_FOLDER}"

# Upload new javadocs
rsync --progress -vr ../target/${VERSION}/ "${P_JAVADOC_HOST}:public_html/pz-api/${ROOT_FOLDER}"
ssh $P_JAVADOC_HOST "chmod -R 755 public_html/pz-api/${ROOT_FOLDER}"
exit $?
