# FROM java:8

FROM ubuntu:14.04
RUN apt-get update && apt-get -y upgrade && apt-get -y install software-properties-common && add-apt-repository ppa:webupd8team/java -y && apt-get update
RUN (echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections) && apt-get install -y oracle-java8-installer oracle-java8-set-default
ENV JAVA_HOME /usr/lib/jvm/java-8-oracle
ENV PATH $JAVA_HOME/bin:$PATH

# wget
RUN apt-get update && apt-get install -y wget

# alpr
RUN wget -O - http://deb.openalpr.com/openalpr.gpg.key | apt-key add -
RUN echo "deb http://deb.openalpr.com/master/ openalpr main" | tee /etc/apt/sources.list.d/openalpr.list
RUN apt-get update
RUN yes | apt-get install openalpr openalpr-daemon openalpr-utils libopenalpr-dev

ENV VERTICLE_FILE acceptor-1.0-SNAPSHOT-fat.jar

# Set the location of the verticles
ENV VERTICLE_HOME /usr/verticles

EXPOSE 8080

# Copy your fat jar to the container
COPY target/$VERTICLE_FILE $VERTICLE_HOME/
COPY src/main/resources/openalpr.conf $VERTICLE_HOME/

# Launch the verticle
WORKDIR $VERTICLE_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["java -jar $VERTICLE_FILE"]
