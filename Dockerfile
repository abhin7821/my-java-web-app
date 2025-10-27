FROM tomcat:9.0-jdk17

# Copy the WAR produced by Maven
COPY ./target/java-web-app.war /usr/local/tomcat/webapps/myapp.war

EXPOSE 8080
CMD ["catalina.sh", "run"]
