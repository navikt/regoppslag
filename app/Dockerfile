FROM openjdk:8-jre-alpine
MAINTAINER Ketill Fenne <ketill.fenne@visma.no>

ADD "target/brevbestilling.jar" brevbestilling.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS \
  -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap \
  -Djava.security.egd=file:/dev/./urandom \
  -Dspring.profiles.active=remote \
  -jar /brevbestilling.jar"]
