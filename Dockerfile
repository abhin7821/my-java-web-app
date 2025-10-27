FROM tomcat:9.0

# Copy the pre-built WAR file into Tomcat
COPY ./target/*.war /usr/local/tomcat/webapps/myapp.war

# Expose Tomcat default port
EXPOSE 8080

# Start Tomcat automatically when container runs
CMD ["catalina.sh", "run"]

