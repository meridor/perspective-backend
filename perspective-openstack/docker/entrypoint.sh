#!/bin/sh

JAVA_ARGS="$GC_OPTS $MEMORY_OPTS $NETWORK_OPTS $LOGGING_OPTS $MISC_OPTS"
java $JAVA_ARGS -jar /usr/share/perspective/perspective-openstack/${project.build.finalName}.jar 2>&1
