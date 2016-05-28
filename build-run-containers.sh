#!/usr/bin/env bash
tag=tracciare/acceptor:1.0-SNAPSHOT
name=acceptor

host=$(docker-machine ip)
port=8080

function print_msg {
    delimiter='======================================================================================'
    echo $delimiter
    echo $1
    echo $delimiter
}

print_msg 'Building fat jar ...'
mvn clean package -Dmaven.test.skip=true

print_msg 'Building $name Docker image ...'
docker-compose stop $name
yes | docker-compose rm
docker-compose build $name

print_msg "Running acceptor service and Mongo Docker containers ..."
docker-compose up -d

# docker build -t $tag .
# docker run -t -i -p $port:$port $tag
