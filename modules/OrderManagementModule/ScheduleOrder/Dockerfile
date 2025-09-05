FROM public.ecr.aws/amazoncorretto/amazoncorretto:22-headful
RUN yum update -y && yum install -y bind-utils
COPY ./target/ScheduleOrder.jar /tmp/ScheduleOrder.jar
WORKDIR /tmp
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "ScheduleOrder.jar"]
