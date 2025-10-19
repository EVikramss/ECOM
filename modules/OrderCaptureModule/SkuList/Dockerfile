FROM public.ecr.aws/amazoncorretto/amazoncorretto:22-headful
RUN yum update -y && yum install -y bind-utils
COPY ./target/SkuList.jar /tmp/SkuList.jar
WORKDIR /tmp
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "SkuList.jar"]
