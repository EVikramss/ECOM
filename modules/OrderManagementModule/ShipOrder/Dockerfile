FROM public.ecr.aws/amazoncorretto/amazoncorretto:22-headful
RUN yum update -y && yum install -y bind-utils
COPY ./target/ShipOrder.jar /tmp/ShipOrder.jar
WORKDIR /tmp
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "ShipOrder.jar"]
