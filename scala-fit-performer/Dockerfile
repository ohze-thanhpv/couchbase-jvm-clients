# A Docker image for the Couchbase SDK performance test performer for Scala.
# Build from root with:
# docker build -f couchbase-jvm-clients/scala-fit-performer/Dockerfile .

FROM maven:3.8.1-openjdk-17
WORKDIR /app

COPY transactions-fit-performer transactions-fit-performer/
COPY couchbase-jvm-clients couchbase-jvm-clients/

WORKDIR /app/transactions-fit-performer
RUN mvn -T6 -q --projects gRPC/jvm --also-make install -Dcheckstyle.skip -Dmaven.test.skip -Dmaven.javadoc.skip

WORKDIR /app/couchbase-jvm-clients
RUN mvn -T6 -q -f protostellar/pom.xml clean install -Dcheckstyle.skip -Dmaven.test.skip -Dmaven.javadoc.skip
RUN mvn -T6 -q -f core-io-deps/pom.xml clean install -Dcheckstyle.skip -Dmaven.test.skip -Dmaven.javadoc.skip
RUN mvn -T6 package -q -pl scala-fit-performer -am -Dcheckstyle.skip -Dmaven.test.skip -Dmaven.javadoc.skip

# Multistage build to keep things small
FROM maven:3.8.1-openjdk-17

WORKDIR /app

COPY --from=0 /app/couchbase-jvm-clients/scala-fit-performer/target/fit-performer-scala-1.0-SNAPSHOT-jar-with-dependencies.jar .
ENTRYPOINT ["java", "-jar", "fit-performer-scala-1.0-SNAPSHOT-jar-with-dependencies.jar"]