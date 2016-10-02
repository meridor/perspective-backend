FROM openjdk:8-jre-alpine

MAINTAINER Ivan Krutov <vania-pooh@meridor.org>

ENV LANG en_US.UTF-8

COPY lib /usr/share/perspective/perspective-docker
COPY ${project.build.finalName}.jar usr/share/perspective/perspective-docker

COPY entrypoint.sh /

ENTRYPOINT /entrypoint.sh