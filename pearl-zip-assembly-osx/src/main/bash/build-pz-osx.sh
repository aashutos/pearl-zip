#!/bin/bash

#
# Copyright © 2021 92AK
#

while read line; do
  if [ $(echo "$line" | grep = | wc -l) == 1 ]
  then
    echo "Setting environment variable: $line"
    key=$(echo $line | cut -d= -f1)
    value=$(echo $line | cut -d= -f2-)
    declare P_$key="$value"
  fi
done < ./src/main/resources/settings.properties

echo "Checking Maven and Java are installed"
JAVA_ROOT="${JAVA_HOME:+$JAVA_HOME/bin/}"

echo "Input parameters: $*"
VERSION=$1
LOCALE=$2
USE_BUILD_DIR=$3
if [ $(echo "$VERSION" | cut -d. -f1) -eq 0 ] || [ $(echo "$VERSION" | cut -d. -f1) -eq 1 ]
then # Mac versioning cannot start with major increment = 0
  MAC_APP_VERSION=1.$(echo "$VERSION" | cut -d. -f2).$(echo "$VERSION" | cut -d. -f3)
else
  MAC_APP_VERSION=$(echo "$VERSION" | cut -d. -f1).$(echo "$VERSION" | cut -d. -f2).$(echo "$VERSION" | cut -d. -f3)
fi
YT_VERSION=PA-$VERSION

if [ "$(which mvn | echo $?)" -ne 0 ]
then
  echo "Maven has not been installed. Exiting..."
  exit 1
fi

if [ $(${JAVA_ROOT}java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d. -f1) -lt 17 ]
then
  echo "Java >= 17 has not been installed. Exiting..."
  exit 1
fi

rootDir=$(pwd)
echo "Root directory: $(pwd)"

echo "Executing clean up script..."
rm -f $rootDir/deps.lst $rootDir/pz-class.lst $rootDir/pz-shared.jsa
rm -rf $rootDir/mods
rm -rf $rootDir/out
rm -rf $rootDir/work
rm -rf $rootDir/pz-runtime
rm -rf $rootDir/target/${LOCALE}/PearlZip.app
rm -rf $rootDir/build/${LOCALE}/PearlZip.app

echo "Generating Dependencies..."
mvn dependency:build-classpath -X -Dmdep.outputFile=$rootDir/deps.lst -f ../pearl-zip-ui/pom.xml

echo "Retrieving Dependencies..."
mkdir -p $rootDir/mods
cp $(cat $rootDir/deps.lst | sed -e 's/\:/ /g') $rootDir/mods/
cp ~/.m2/repository/com/ntak/pearl-zip-ui/$VERSION/pearl-zip-ui-$VERSION.jar $rootDir/mods/

mkdir $rootDir/work

echo "Identifying legacy modules..."
ls -1 $rootDir/mods/ | while read line
do
   echo "Archive: $rootDir/mods/$line"
   ${JAVA_ROOT}jdeps --module-path $rootDir/mods/ --ignore-missing-deps --multi-release 17 --generate-module-info out "mods/$line" > /dev/null
   if [ "$?" -eq 0 ]
   then
     echo "Get module name"
     moduleName=$(${JAVA_ROOT}jdeps --module-path $rootDir/mods/ --ignore-missing-deps --multi-release 17 --generate-module-info work "$rootDir/mods/$line" | grep "writing to" | cut -d" " -f3 | cut -d/ -f2)
     echo "Module name: $moduleName"

     echo "Generate module-info file..."
     ${JAVA_ROOT}jdeps --ignore-missing-deps --multi-release 17 --generate-module-info $rootDir/work "$rootDir/mods/$line"
     cp "work/$(ls -1 $rootDir/work)/versions/17/module-info.java" $rootDir/work/$(ls -1 $rootDir/work)/module-info.java

     echo "Building new jar..."
     mkdir $rootDir/classes
     cd $rootDir/classes
     jar xf $rootDir/mods/$line

     echo "Modularising legacy jars..."
     cd $rootDir
     ${JAVA_ROOT}javac -cp $rootDir/mods/ --module-path $rootDir/mods/ -d $rootDir/classes/ $rootDir/work/$moduleName/module-info.java
     jar uf $rootDir/mods/$line -C classes module-info.class

     echo "Clearing work directories..."
     rm -rf $rootDir/work
     rm -rf $rootDir/classes
   fi
done

echo "Manual override with pre-compiled modules..."
cp $rootDir/src/main/resources/lib/org.apache.logging.log4j.core/log4j-core-2.17.0.jar mods/
cp $rootDir/src/main/resources/lib/org.apache.commons.compress/commons-compress-1.21.jar mods/
cp $rootDir/src/main/resources/lib/com.sun.jna/jna-5.6.0.jar mods/

echo "Create shared archive..."
${JAVA_ROOT}java -Xshare:off -Djava.awt.headless=true -XX:+UseZGC -XX:DumpLoadedClassList=pz-class.lst --module-path="$(ls mods | xargs -I{} echo mods/{} | tr '\n' ':'):mods/pearl-zip-ui-$VERSION.jar" -m com.ntak.pearlzip.ui/com.ntak.pearlzip.ui.pub.ZipLauncher &
pid=$!
echo "Dump list process PID: $pid"
sleep 10
kill -9 $pid
# KILL executed, Check for end of process
while [ $(ps -p $pid > /dev/null) ]; do
  sleep 10
done

${JAVA_ROOT}java -Xshare:dump -Djava.awt.headless=true -XX:+UseZGC -XX:SharedClassListFile=pz-class.lst -XX:SharedArchiveFile=pz-shared.jsa --module-path="$(ls mods | xargs -I{} echo mods/{} | tr '\n' ':'):mods/pearl-zip-ui-$VERSION.jar" com.ntak.pearlzip.ui.pub.ZipLauncher -m com.ntak.pearlzip.ui/com.ntak.pearlzip.ui.pub.ZipLauncher > /dev/null 2>&1 &
pid=$!
echo "Create JSA process PID: $pid"
sleep 10
kill -2 $pid
#   SIGINT executed, Check for end of process
while [ $(ps -p $pid > /dev/null) ]; do
  sleep 10
done

echo "Create runtime..."
#cp $rootDir/src/main/resources/lib/javafx-jmods-15.0.1/* mods/
#find $rootDir/mods -name  "javafx*[^m][^a][^c].jar" -delete
#find $rootDir/mods/ -name  "javafx*mac.jar" -delete
${JAVA_ROOT}jlink --module-path=$rootDir/mods/ --add-modules=ALL-MODULE-PATH --output $rootDir/pz-runtime

echo "Generate App Image"
mkdir -p target/${LOCALE}

${JAVA_ROOT}jpackage --dest target/${LOCALE} --type app-image --app-version $MAC_APP_VERSION --copyright "© copyright 2021 92AK" --description "A JavaFX front-end wrapper for some common archive formats" --name PearlZip --vendor 92AK --verbose --java-options "-XX:SharedArchiveFile=pz-shared.jsa" --java-options "-XX:+UseZGC" --java-options "--add-opens nsmenufx/de.jangassen.platform.mac=com.ntak.pearlzip.ui" --java-options "--add-opens nsmenufx/de.jangassen.platform=com.ntak.pearlzip.ui" --java-options "-Dlog4j2.formatMsgNoLookups=true" --icon "$rootDir/src/main/resources/pz-icon.icns" --mac-package-identifier com.ntak.pearl-zip --mac-package-identifier PearlZip  --module-path $rootDir/mods/ -m com.ntak.pearlzip.ui/com.ntak.pearlzip.ui.pub.ZipLauncher --file-associations $rootDir/src/main/resources/file-associations/fa-xz.properties --file-associations $rootDir/src/main/resources/file-associations/fa-bz2.properties --file-associations $rootDir/src/main/resources/file-associations/fa-pzax.properties --file-associations $rootDir/src/main/resources/file-associations/fa-zip.properties --file-associations $rootDir/src/main/resources/file-associations/fa-gzip.properties --file-associations $rootDir/src/main/resources/file-associations/fa-tar.properties --file-associations $rootDir/src/main/resources/file-associations/fa-jar.properties --runtime-image $rootDir/pz-runtime
for f in $(ls $rootDir/src/main/resources/file-associations/Icons)
do
 echo "Adding icon $f..."
 cp "$rootDir/src/main/resources/file-associations/Icons/$f" "target/${LOCALE}/PearlZip.app/Contents/Resources/$f"
 ext=$(echo $f | cut -d- -f2)

 echo "Adding configuration for extension $ext..."
 printf "    <key>CFBundleTypeIconFile</key>\n    <string>pz-${ext}-icon.icns</string>\n			<key>CFBundleTypeExtensions</key>\n			<array>\n				<string>${ext}</string>\n			</array>\n" > ${ext}.xml
 cat ${ext}.xml
 $rootDir/src/main/bash/slip.sh "$rootDir/target/${LOCALE}/PearlZip.app/Contents/Info.plist" "$rootDir/target/${LOCALE}/PearlZip.app/Contents/tmp-${ext}.plist" "<string>PearlZip.${ext}" "${ext}.xml" 1
 mv "$rootDir/target/${LOCALE}/PearlZip.app/Contents/tmp-${ext}.plist" "$rootDir/target/${LOCALE}/PearlZip.app/Contents/Info.plist"
 rm ${ext}.xml

 $rootDir/src/main/bash/slip.sh "$rootDir/target/${LOCALE}/PearlZip.app/Contents/Info.plist" "$rootDir/target/${LOCALE}/PearlZip.app/Contents/tmp-${ext}.plist" "<string>PearlZip.${ext}" "$rootDir/src/main/resources/file-associations/fa-${ext}.cfg" 0
 mv "$rootDir/target/${LOCALE}/PearlZip.app/Contents/tmp-${ext}.plist" "$rootDir/target/${LOCALE}/PearlZip.app/Contents/Info.plist"
done
mkdir -p components
echo "Generating changelog"
# Retrieve Change Log for release
curl -X GET "${P_YOUTRACK_HOST}/api/issues?fields=summary&query=project:%20PearlZip%20%23Bug%20%23Feature%20%23Task%20Fix%20versions:%20${YT_VERSION}%20tag:%20release-tickets" -H 'Accept: application/json' -H "Authorization: Bearer ${P_YOUTRACK_AUTH}" -H 'Cache-Control: no-cache' -H 'Content-Type: application/json' | tr , "\n" | grep summary | cut -d'"' -f4 | sort | xargs -I{} echo "+ {}" > components/changelog
printf "PearlZip Version $VERSION Change Log\nCopyright © $(date +%Y) 92AK\n====================================\n\nThe following features are in scope for this release of PearlZip:\n\n$(cat components/changelog)" > components/changelog

echo "Building installation package..."
cp -R target/${LOCALE}/PearlZip.app ${P_LICENSE_DIRECTORY}/BSD-3-CLAUSE-LICENSE ${P_LICENSE_DIRECTORY}/CC-ATTR-4-LICENSE ${P_LICENSE_DIRECTORY}/CC-NC-ND-4-LICENSE components
pkgbuild --root components --component-plist ./src/main/resources/PearlZip.plist --install-location /Applications/PearlZip --identifier com.ntak.pearlzip --version ${MAC_APP_VERSION} --ownership recommended ./target/${LOCALE}/PearlZip-Installer-${LOCALE}-${VERSION}.pkg

if [ "$USE_BUILD_DIR" == 'true' ]
then
  echo "Keeping packages in build directory..."
  mkdir -p build
  cp -R target/* build
else
  rm -rf build
fi

echo "Clearing up working directories..."
rm -f pz-shared.jsa
rm -f pz-class.lst
rm -rf pz-runtime
rm -rf mods
rm -rf deps.lst
rm -rf out
rm -r  nohup.out
rm -rf components

exit 0
