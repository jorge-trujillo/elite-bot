FROM adoptopenjdk/openjdk11-openj9:jdk-11.28
MAINTAINER Jorge Trujillo <jorge.a.trujillo@target.com>

ENV HOME /root
ENV TIMEZONE=America/Chicago

# Prereqs
RUN apt-get update
RUN apt-get install -y --no-install-recommends curl vim lsof jq dnsutils net-tools \
  && rm -rf /var/lib/apt/lists/*

# Set up entrypoint scripts
RUN mkdir -p /apps/install/core
COPY files/* /apps/install/core/
RUN chmod a+x /apps/install/core/docker_startpoint.sh

# App properties and certificates
ENV CERTS_PATH /apps/install/certs
ENV APP_PROPERTIES_PATH /apps/install/app_properties
ENV APP_PROPERTIES_FILE application-override.properties

# Copy JAR
ENV JAR_FILE=/apps/install/elite-bot.jar
COPY elite-bot.jar $JAR_FILE

EXPOSE 8080
ENTRYPOINT ["/apps/install/core/docker_startpoint.sh"]

CMD java $GC_FLAGS $JAVA_OPTS -jar $JAR_FILE $SPRING_OPTS $EXTERNAL_CONFIG
