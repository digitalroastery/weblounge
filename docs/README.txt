This is a proof-of-concept for a Distributed OSGI environment for developing Matterhorn.

To install and run this software:

1) Download the binary distribution of Felix 1.8.0.  TODO: Test on Equinox.
2) Build this software using maven (mvn -DskipTests install).  The first time you install, maven will download all of the dependencies and transitive dependencies for all of the Matterhorn code.  Don't be surprised if this takes a long time. You may need to increase your JVM memory settings.  I do this in bash with:

export MAVEN_OPTS="-Xms256m -Xmx512m -XX:PermSize=64m -XX:MaxPermSize=128m"

3) Copy docs/felix_config.properties to <felix_root>/conf/config.properties and edit the matterhorn.* properties at the bottom of the file to match your environment.
4) Start felix with cd <felix_root>; java -DM2_REPO=[your home directory]/.m2/repository/ -jar bin/felix.jar
5) Visit http://localhost:8080/samplews?wsdl to see the sample web service endpoint.  TODO: expose a URL describing all of the service endpoints, static resource aliases, etc.

Other URLs of interest:

Sample HTML form to upload binary data to the repository: http://localhost:8080/samplehtml/upload.html (warning: uploading returns HTTP 200, so "nothing happens" in the browser)
Get binary data from the repository: http://localhost:8080/rest/repository/data/[path] (warning: this returns a file with no filename or mime type)
(TODO) Get metadata from the repository: GET http://localhost:8080/rest/repository/metadata/[key]/[path]
(TODO) Put metadata into the repository: POST or PUT http://localhost:8080/rest/repository/metadata/[key]/[path]

Logging:

Logging configuration can be customized before server startup or at runtime.  Create a file named "org.ops4j.pax.logging.cfg" in the <felix_root>/load directory.  This file must contain log4j properties entries, such as:

log4j.rootLogger=INFO, stdout
log4j.appender.stdout=org.apache.log4j.ConsoleAppender
log4j.appender.stdout.layout=org.apache.log4j.PatternLayout
log4j.appender.stdout.layout.ConversionPattern=%5p [%t] (%C{3}:%L) - %m%n
log4j.logger.org.apache=WARN
log4j.logger.org.springframework=WARN
log4j.logger.org.opencastproject=DEBUG
