#!/bin/sh

JAVA_ARGS="$GC_OPTS $MEMORY_OPTS $NETWORK_OPTS $MISC_OPTS"
if [ -n "$LOGFILE" ]; then
    LOGGING_OPTS=" >> $LOGFILE"
fi
java $JAVA_ARGS -jar /usr/share/perspective/perspective-openstack/${project.build.finalName}.jar $LOGGING_OPTS 2>&1
