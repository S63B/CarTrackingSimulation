FROM frolvlad/alpine-oraclejdk8:slim
VOLUME /tmp
ADD simulation-0.0.1-SNAPSHOT.jar simulation.jar
ADD cartrackingsimulation.jar simulation.jar
RUN apt-get install sumo sumo-tools sumo-doc
RUN sh -c 'touch /simulation.jar'
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /simulation.jar" ]
