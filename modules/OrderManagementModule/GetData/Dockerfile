FROM public.ecr.aws/amazoncorretto/amazoncorretto:22-headful
RUN yum update -y && yum install -y bind-utils
COPY ./target/GetData.jar /tmp/GetData.jar
WORKDIR /tmp
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "GetData.jar"]
