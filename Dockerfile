FROM java:openjdk-8-jdk

RUN mkdir -p /usr/src/myapp
COPY . /usr/src/myapp
WORKDIR /usr/src/myapp

RUN ./gradlew stage
CMD ["java", "-jar", "/usr/src/myapp/app.jar"]
EXPOSE 8082
