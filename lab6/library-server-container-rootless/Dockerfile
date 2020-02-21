FROM openjdk:11.0.6-jre-buster
COPY library-server-container-rootless-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 9090
RUN addgroup --system --gid 1002 app && adduser --system --uid 1002 --gid 1002 appuser
USER 1002
ENTRYPOINT java -jar /app.jar
