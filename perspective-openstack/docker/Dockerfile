FROM openjdk:8-jre-alpine

MAINTAINER Ivan Krutov <vania-pooh@meridor.org>

ENV LANG=en_US.UTF-8 \
    GC_OPTS="-XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled" \
    MEMORY_OPTS="-Xmx256m" \
    NETWORK_OPTS="" \
    LOGGING_OPTS="" \
    MISC_OPTS=""

COPY lib /usr/share/perspective/perspective-openstack/lib
COPY ${project.build.finalName}.jar usr/share/perspective/perspective-openstack

COPY entrypoint.sh /

ENTRYPOINT /entrypoint.sh