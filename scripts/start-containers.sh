#!/bin/bash

command_exists () {
    type "$1" &> /dev/null ;
}

if ! command_exists docker-compose ; then
    echo "docker-compose not installed"
    exit 1
fi

read -p "Enter version: " version
version=${version:-latest}
VERSION=$version docker-compose up