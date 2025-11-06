FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-25-dev AS builder
WORKDIR /build
COPY app/target/app.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --launcher --layers --destination extracted

FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-21
COPY --from=builder --chown=1069:1069 /build/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=1069:1069 /build/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=1069:1069 /build/extracted/dependencies/ ./
COPY --from=builder --chown=1069:1069 /build/extracted/application/ ./

ENV TZ="Europe/Oslo"
CMD ["-Dspring.profiles.active=nais", "-server", "-cp", ".", "org.springframework.boot.loader.launch.JarLauncher"]