FROM navikt/java:8

COPY target/app.jar /app/app.jar

ENV JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom \
               -Dspring.profiles.active=nais"