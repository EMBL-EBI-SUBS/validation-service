FROM openjdk:8-jdk-alpine

WORKDIR /opt

ENV MONGO_URI=mongodb://localhost:27017
ENV RABBIT_HOST=localhost
ENV RABBIT_PORT=5672

# copy in the gradlew, gradle credentials and src folder
ADD gradle ./gradle
ADD src ./src

COPY gradlew gradle.properties.enc build.gradle ./
# build the code
RUN ./gradlew assemble
# run tests, ignore mongoDB/rabbitmq dependent tests...
RUN ./gradlew externalCiTest

COPY build/libs/*.jar ./validator-coordinator.jar

CMD java -jar validator-coordinator.jar --spring.data.mongodb.uri=$MONGO_URI --spring.rabbitmq.host=$RABBIT_HOST --spring.rabbitmq.port=$RABBIT_PORT
