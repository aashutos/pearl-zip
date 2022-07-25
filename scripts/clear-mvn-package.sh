#!/bin/bash
#
# Copyright © 2022 92AK
#

RELEASE=$1
P_SETTINGS=${2:-./scripts/settings.properties}

source ./init-settings.sh "$P_SETTINGS"

P_TOKEN_HEADER="Authorization: token ${P_GITHUB_API_TOKEN}"
PACKAGE_ID=$(curl --progress-bar -sH "${P_TOKEN_HEADER}" "${P_GITHUB_API}/user/packages/maven/${P_PACKAGE_NAME}/versions" | grep -B1 "\"name\": \"${RELEASE}\"" | grep id | tr ',' ' ' | cut -d: -f2 | xargs)
echo "Deleting maven package ${P_PACKAGE_NAME}:${PACKAGE_ID} from remote GitHub maven repository..."
RESP=$(curl --progress-bar -X DELETE -sH "${P_TOKEN_HEADER}" "${P_GITHUB_API}/user/packages/maven/${P_PACKAGE_NAME}/versions/${PACKAGE_ID}")

if [ $(echo $RESP | grep "You cannot delete the last version of a package. You must delete the package instead." | wc -l) -gt 0 ]
then
  echo "Deleting whole maven package ${P_PACKAGE_NAME} from remote GitHub maven repository..."
  RESP=$(curl --progress-bar -X DELETE -sH "${P_TOKEN_HEADER}" "${P_GITHUB_API}/user/packages/maven/${P_PACKAGE_NAME}")
fi
echo $RESP
