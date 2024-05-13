FROM ghcr.io/navikt/baseimages/temurin:21

COPY app/target/app.jar /app/app.jar

ENV JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom \
               -Dspring.profiles.active=nais \
               -Xmx512m --add-opens java.xml/com.sun.org.apache.xerces.internal.jaxp.datatype=ALL-UNNAMED"