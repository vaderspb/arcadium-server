FROM ubuntu:latest as nesrepo

RUN apt-get -y update
RUN apt-get -y install git

WORKDIR /opt/halfnes
RUN git clone https://github.com/vaderspb/halfnes_reduced.git

WORKDIR /opt/halfnes/halfnes_reduced
RUN git checkout remove-javafx


FROM eclipse-temurin:17-jdk-jammy as builder

WORKDIR /opt/halfnes
COPY --from=nesrepo /opt/halfnes/halfnes_reduced/ halfnes_reduced

WORKDIR /opt/halfnes/halfnes_reduced
RUN ./mvnw clean install -DskipTests

WORKDIR /opt/arkadium
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY arcadium-api-server/ arcadium-api-server
COPY arcadium-catalog/ arcadium-catalog
COPY arcadium-session-server/ arcadium-session-server
COPY arcadium-transport/ arcadium-transport
COPY arcadium-worker/ arcadium-worker

RUN ./mvnw clean install -DskipTests


FROM eclipse-temurin:17-jdk-jammy

WORKDIR /opt/arkadium
COPY --from=builder /opt/arkadium/arcadium-worker/arcadium-nes-worker/target/arcadium-nes-worker-*.jar ./
COPY --from=builder /opt/arkadium/arcadium-worker/arcadium-nes-worker/target/lib/*.jar ./

COPY nes/btoads.nes ./

CMD ["java", "-cp", "*", "com.vaderspb.worker.nes.Application"]