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
P_SETTINGS=${2:-./scripts/settings.properties}
JAVA_HOME=${3:-$JAVA_HOME}

echo "JAVA_HOME: $JAVA_HOME"
source ../scripts/init-settings.sh "${P_SETTINGS}"

echo 'Configuring release name...'
P_RELEASE="${P_PREFIX_RELEASE:+$P_PREFIX_RELEASE-}$P_RELEASE"
echo "Release name has been set to: ${P_RELEASE}"

# 1. Sign jar using keystore
echo "Signing plugin archive..."
ARCHIVE=$(ls target/${P_ARCHIVE_ROOT_NAME}-${RELEASE}.jar)
echo "Archive to sign: $ARCHIVE"
echo $(cat ${P_PW_SIGNER_FILE:-/opt/.store/.pw-signer}) | ${JAVA_HOME}/bin/jarsigner -tsa ${P_TIMESTAMP_AUTHORITY_URL:-https://freetsa.org/tsr} -keystore ${P_KEYSTORE_FILE:-/opt/.store/.ks-signer} -storepass $(cat ${P_PW_KEYSTORE_FILE:-/opt/.store/.pw}) "$ARCHIVE" 92ak
