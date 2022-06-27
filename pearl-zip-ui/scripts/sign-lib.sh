#!/bin/bash
#
# Copyright © 2022 92AK
#

# Parameters
# $1 - Release e.g. PA-0.0.0.1
#

# Populate environment variables
P_RELEASE=$1
RELEASE=$1
echo "JAVA_HOME: $JAVA_HOME"

echo 'Configuring release name...'
P_RELEASE="${P_PREFIX_RELEASE:+$P_PREFIX_RELEASE-}$P_RELEASE"
echo "Release name has been set to: ${P_RELEASE}"

# 1. Sign jar using keystore
echo "Signing plugin archive..."
ARCHIVE=$(ls target/pearl-zip-ui-${RELEASE}.jar)
echo "Archive to sign: $ARCHIVE"
echo $(cat /opt/.store/.pw-signer) | ${JAVA_HOME}/bin/jarsigner -tsa https://freetsa.org/tsr -keystore /opt/.store/.ks-signer -storepass $(cat /opt/.store/.pw) "$ARCHIVE" 92ak
