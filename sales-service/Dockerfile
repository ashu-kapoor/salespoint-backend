FROM amazoncorretto:17.0.1-al2
ARG jarDir=build/libs
COPY $jarDir/inventory-service-1.0.jar service.jar

HEALTHCHECK --interval=3s --retries=20 CMD curl -f http://localhost:8080/actuator/health || exit 1
CMD java ${JAVA_OPTS:-"-Xmx64m"} -jar ${JAR:-"service.jar"}
