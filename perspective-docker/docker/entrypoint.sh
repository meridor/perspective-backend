#!/bin/sh

JAVA_ARGS="$GC_OPTS $MEMORY_OPTS $NETWORK_OPTS $MISC_OPTS"

java $JAVA_ARGS -jar /usr/share/perspective/perspective-docker/${project.build.finalName}.jar $LOGGING_OPTS 2>&1
