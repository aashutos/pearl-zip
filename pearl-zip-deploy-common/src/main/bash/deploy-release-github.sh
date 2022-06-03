#!/bin/bash
#
# Copyright © 2022 92AK
#

# Parameters
# $1 - Release e.g. PA-0.0.0.1
#

# Populate environment variables
P_RELEASE=$1
P_LOCALE=$2
P_CODENAME=$3

while read line; do
  if [ $(echo "$line" | grep "=" | wc -l) == 1 ]
  then
    echo "Setting environment variable: $(echo $line | cut -d= -f1)..."
    key=$(echo $line | cut -d= -f1)
    value=$(echo $line | cut -d= -f2-)
    declare P_$key="$value"
  fi
done < ./src/main/resources/settings.properties

# Get set environment variables
# ( set -o posix ; set )

echo 'Configuring release name...'
P_RELEASE="${P_PREFIX_RELEASE:+$P_PREFIX_RELEASE-}$P_RELEASE"
echo "Release name has been set to: ${P_RELEASE}"

# Dependencies check on environment
if [ "$(which 7z | echo $?)" -ne 0 ]
then
  echo "7z has not been installed. Exiting..."
  exit 1
fi

if [ "$(which git | echo $?)" -ne 0 ]
then
  echo "git has not been installed. Exiting..."
  exit 1
fi

if [ "$(which shasum | echo $?)" -ne 0 ]
then
  echo "shasum has not been installed. Exiting..."
  exit 1
fi

# Create git tag and hotfix branch
if [ $(git branch | grep '*' | grep master | wc -l) -eq 1 ]
then
  echo "On master branch. Creating release tag ${P_RELEASE}..."
  git tag "${P_RELEASE}"
  git push --tags

  echo "Creating hotfix branch releases/${P_RELEASE}..."
  git checkout -b "releases/${P_RELEASE}"
  git push --tags origin "releases/${P_RELEASE}"
else
  echo "Not on master branch. Exiting..."
  exit 2
fi

# Create release and upload
if [ ! -e "${P_BINARY_DIRECTORY}/${P_LOCALE}" ]
then
  echo "Release '${P_BINARY_DIRECTORY/${P_LOCALE}}' does not exists. exiting..."
  exit 2
fi

# Checking for deployment package...
INSTALLER=$(ls ${P_BINARY_DIRECTORY}/${P_LOCALE}/PearlZip-Installer-${LOCALE}*pkg)

if [ "${#INSTALLER}" -eq 0 ]
then
  echo "Package does not exists. exiting..."
  exit 3
fi

# Obtaining asset Id to link deployment package with a tag
P_TAGS_API="${P_GITHUB_API}/repos/${P_REPO_OWNER}/${P_REPOSITORY}/releases/tags/${P_RELEASE}"
P_TOKEN_HEADER="Authorization: token ${P_GITHUB_API_TOKEN}"

CHANGELOG=$(cat ../pearl-zip-assembly-osx/components/changelog | tr '\n' '\\n')

##########################################################################################
# UPLOADING INSTALLER TO GITHUB                                                          #
##########################################################################################
echo "Uploading asset ${INSTALLER} to ${P_REPOSITORY} for tag ${P_RELEASE}... "

# Get ID and remove whitespaces...
ID=$(curl -X GET -sH "${P_TOKEN_HEADER}" https://api.github.com/repos/aashutos/pearl-zip/releases | grep -A1 "\"html_url\": \".*${P_RELEASE}\"" | grep id | tr ',' ' ' | cut -d: -f2)
ID=${ID//[$'\t\r\n ']}

if [ ${ID:-0} -gt 0 ]
then
  ECHO "Existing release detected..."
else
  NEW_RELEASE_JSON=$(curl -X POST -sH "${P_TOKEN_HEADER}" -d "{\"name\":\"PearlZip Release ${P_RELEASE}${P_CODENAME:+" ($P_CODENAME)"}\",\"body\":\"PearlZip Release version ${P_RELEASE} as an Apple pkg installer.\",\"tag_name\":\"${P_RELEASE}\",\"draft\":${P_DRAFT_RELEASE}" https://api.github.com/repos/${P_REPO_OWNER}/${P_REPOSITORY}/releases)
  ID=$(echo "${NEW_RELEASE_JSON}" | grep -m 1 '\"id\"' | tr ',' ' ' | cut -d':' -f2 | xargs -I{} echo {})
fi

echo "Asset Id: $ID"
echo "Creating SHA-512 hash of ${INSTALLER}..."
INSTALLER_HASH=${INSTALLER}.sha512
shasum -a 512 "${INSTALLER}" | cut -d" " -f1 > "${INSTALLER_HASH}"

echo "Uploading asset ${INSTALLER}"
sleep 10
curl --progress-bar -sH "${P_TOKEN_HEADER}" --data-binary @"${INSTALLER}" -H "Content-Type: application/octet-stream" "${P_GITHUB_UPLOAD_API}/repos/${P_REPO_OWNER}/${P_REPOSITORY}/releases/$(echo $ID)/assets?name=$(basename "${INSTALLER}")"
sleep 5

echo "Uploading asset ${INSTALLER_HASH}"
sleep 10
curl --progress-bar -sH "${P_TOKEN_HEADER}" --data-binary @"${INSTALLER_HASH}" -H "Content-Type: application/octet-stream" "${P_GITHUB_UPLOAD_API}/repos/${P_REPO_OWNER}/${P_REPOSITORY}/releases/$(echo $ID)/assets?name=$(basename "${INSTALLER_HASH}")"

##########################################################################################
# UPLOADING fr-FR Language Pack                                                          #
##########################################################################################
fr_FR_LANG_PACK=$(ls ${P_fr_FR_LANG_PACK_DIRECTORY}/pearl-zip-lang-pack-fr-FR-*pzax)

echo "Creating SHA-512 hash of ${fr_FR_LANG_PACK}..."
fr_FR_LANG_PACK_HASH=${P_fr_FR_LANG_PACK_DIRECTORY}/pearl-zip-lang-pack-fr-FR-${P_RELEASE}.pzax.sha512
shasum -a 512 "${fr_FR_LANG_PACK}" | cut -d" " -f1 > "${fr_FR_LANG_PACK_HASH}"

echo "Uploading asset ${fr_FR_LANG_PACK} to ${P_REPOSITORY} for tag ${P_RELEASE}... "

echo "Uploading asset ${fr_FR_LANG_PACK}"
sleep 10
curl --progress-bar -sH "${P_TOKEN_HEADER}" --data-binary @"${fr_FR_LANG_PACK}" -H "Content-Type: application/octet-stream" "${P_GITHUB_UPLOAD_API}/repos/${P_REPO_OWNER}/${P_REPOSITORY}/releases/$(echo $ID)/assets?name=$(basename "${fr_FR_LANG_PACK}")"
sleep 5

echo "Uploading asset ${fr_FR_LANG_PACK_HASH}"
sleep 10
curl --progress-bar -sH "${P_TOKEN_HEADER}" --data-binary @"${fr_FR_LANG_PACK_HASH}" -H "Content-Type: application/octet-stream" "${P_GITHUB_UPLOAD_API}/repos/${P_REPO_OWNER}/${P_REPOSITORY}/releases/$(echo $ID)/assets?name=$(basename "${fr_FR_LANG_PACK_HASH}")"

echo 'resetting to master branch...'
git checkout master
