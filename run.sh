#!/bin/bash

if [ $# -lt 2 ]
  then
    echo "Not enough arguments supplied"
    exit 1
fi

project=$1
version=$2


echo 
echo "Analyse $project $version"

rm -r "outputs/$project@$version" > /dev/null 2>&1

mkdir -p outputs/$project@$version

echo " - resolve dependencies"
time java -DproxySet=true -DproxyHost=localhost -DproxyPort=3128 -cp target/maven-timemachine-1.0-SNAPSHOT-jar-with-dependencies.jar nl.wvdzwan.timemachine.Main  -k=ad19ce0d9ce33eac2bcdadb6ea73a388 -o "outputs/$project@$version" $project $version > outputs/$project@$version/resolve.log 2>&1
echo "   errors $(grep -e ERROR outputs/$project@$version/resolve.log | wc)"


echo " - generate callgraph"
time java -DproxySet=true -DproxyHost=localhost -DproxyPort=3128 -cp target/maven-timemachine-1.0-SNAPSHOT-jar-with-dependencies.jar nl.wvdzwan.timemachine.callgraph.CallGraphMain -o "outputs/$project@$version" @outputs/$project@$version/classpath.txt > outputs/$project@$version/callgraph.log 2>&1
echo "   errors$(grep -e ERROR outputs/$project@$version/callgraph.log | wc)"

echo
