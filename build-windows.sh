#!/bin/sh
mvn install -Dversion=`cat VERSION` && (cd java; mvn assembly:single -Dversion=`cat ../VERSION`) && cp java/target/JSync-`cat VERSION`-jar-with-dependencies.jar java/target/JSync-jar-with-dependencies.jar && echo "DONE, NOW BUILD THE EXE WITH JSMOOTH USING jsync.jsmooth"
