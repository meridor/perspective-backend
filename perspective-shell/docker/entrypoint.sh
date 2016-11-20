#!/bin/sh

GC_OPTS=${GC_OPTS:-"-XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled"}
MEMORY_OPTS=${MEMORY_OPTS:-"-Xmx256m"}
NETWORK_OPTS=${NETWORK_OPTS:-"-Djava.net.preferIPv6Addresses=true"}
MISC_OPTS=${MISC_OPTS:-""}

JAVA_ARGS="$GC_OPTS $MEMORY_OPTS $NETWORK_OPTS $MISC_OPTS"

java ${JAVA_ARGS} -jar /usr/share/perspective/perspective-shell/${project.build.finalName}.jar 2>&1
