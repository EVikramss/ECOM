FROM public.ecr.aws/amazoncorretto/amazoncorretto:22-headful
RUN yum update -y && yum install -y bind-utils
COPY ScheduleOrder.jar /tmp/ScheduleOrder.jar
WORKDIR /tmp
ENTRYPOINT ["java", "-jar", "ScheduleOrder.jar"]
