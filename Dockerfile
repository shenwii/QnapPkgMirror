FROM openjdk:17-bullseye

WORKDIR /opt/QnapPkgMirror

COPY src /opt/QnapPkgMirror/src
COPY gradle /opt/QnapPkgMirror/gradle
COPY gradlew /opt/QnapPkgMirror/
COPY build.gradle /opt/QnapPkgMirror/
COPY settings.gradle /opt/QnapPkgMirror/

RUN sh gradlew build

ENTRYPOINT ["java","-jar","./build/libs/QnapPkgMirror-1.0.0.jar","--spring.config.location=./etc/application.yaml"]
