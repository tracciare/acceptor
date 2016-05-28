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

print_msg 'Building $name Vertx fat jar ...'
mvn clean package

print_msg 'Building $name Docker image ...'
docker-compose stop $name
yes | docker-compose rm
docker-compose build $name

print_msg "Running trace-repo and Mongo Docker containers: service will be available at http://$host:8080 ..."
docker-compose up -d

# print_msg 'Building trace-repo Docker image ...'
# docker build -t $tag .
# print_msg "Running trace-repo Docker container: service will be available at http://$host:8080 ..."
# docker run -t -i -p $port:$port $tag
