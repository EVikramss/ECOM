FROM public.ecr.aws/amazoncorretto/amazoncorretto:22-headful
RUN yum update -y && yum install -y bind-utils
COPY ./target/CreateOrder.jar /tmp/CreateOrder.jar
WORKDIR /tmp
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "CreateOrder.jar"]
