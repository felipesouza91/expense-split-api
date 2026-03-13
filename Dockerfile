FROM amazoncorretto:21-alpine3.19-jdk
WORKDIR /app
COPY ./target/*.jar app.jar
EXPOSE 8080
CMD ["java","-jar" ,"app.jar"]