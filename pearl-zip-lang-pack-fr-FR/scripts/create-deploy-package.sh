#!/bin/sh

#
# Copyright © 2022 92AK
#

RELEASE=$1
PZ_RELEASE=$2
JAVA_HOME=${3:-$JAVA_HOME}
P_SETTINGS=${4:-./scripts/settings.properties}

source ../scripts/init-settings.sh "${P_SETTINGS}"

# 1. Copy unsigned jar from local repo
echo "Making build directory..."
[[ -d build ]] && rm -rf build
mkdir build
#echo "Copying raw plugin compiled source..."
#cp ~/.m2/repository/com/ntak/pearl-zip-lang-pack-fr-FR/${RELEASE}/pearl-zip-lang-pack-fr-FR-${RELEASE}.jar build/pearl-zip-lang-pack-fr-FR-${RELEASE}.jar

# 2. Sign jar using keystore
../scripts/sign-lib.sh "${RELEASE}" "${JAVA_HOME}"
cp target/${P_ARCHIVE_ROOT_NAME}-${RELEASE}.jar build/pearl-zip-lang-pack-fr-FR-${RELEASE}.jar

# Copy license file and deployment instructions
echo "Preparing deployment archive..."
echo "Preparing static resources..."
cp ../BSD-3-CLAUSE-LICENSE build/BSD-3-CLAUSE-LICENSE
cp scripts/MF build/MF

# Copy Dependencies
cd build
echo "Creating zip archive..."
shasum -a 256 pearl-zip-lang-pack-fr-FR-${RELEASE}.jar | cut -d" " -f1 > pearl-zip-lang-pack-fr-FR-${RELEASE}.sha256
zip -r pearl-zip-lang-pack-fr-FR-${RELEASE}.pzax *.jar *.sha256 BSD-3-CLAUSE-LICENSE MF
cd ..
