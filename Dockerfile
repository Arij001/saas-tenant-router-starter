FROM registry-cbu.huawei.com/fuxi-build-plus/euler25_x86_jdk8:latest
RUN mkdir -p /home/huawei/microservice
COPY target/saas-tenant-router-starter-0.0.1-SNAPSHOT.jar /home/huawei/microservice/

ENTRYPOINT ["java","-jar","/home/huawei/microservice/saas-tenant-router-starter-0.0.1-SNAPSHOT.jar"]

EXPOSE 8080