#!/bin/sh

GC_OPTS=${GC_OPTS:-"-XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled"}
MEMORY_OPTS=${MEMORY_OPTS:-"-Xmx1024m"}
NETWORK_OPTS=${NETWORK_OPTS:-"-Djava.net.preferIPv6Addresses=true"}
MISC_OPTS=${MISC_OPTS:-""}

JAVA_ARGS="$GC_OPTS $MEMORY_OPTS $NETWORK_OPTS $MISC_OPTS"

java $JAVA_ARGS -jar /usr/share/perspective/perspective-storage/${project.build.finalName}.jar >> /var/log/perspective-storage.log 2>&1
