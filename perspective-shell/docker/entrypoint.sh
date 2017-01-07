#!/bin/sh

JAVA_ARGS="$GC_OPTS $MEMORY_OPTS $NETWORK_OPTS $MISC_OPTS"

java ${JAVA_ARGS} -jar /usr/share/perspective/perspective-shell/${project.build.finalName}.jar 2>&1
