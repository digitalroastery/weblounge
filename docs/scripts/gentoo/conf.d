#####
# Weblounge filesystem layout
##

# Where your web applications are located
WEBLOUNGE_HOME="/usr/share/weblounge"

# Configuration for weblounge
WEBLOUNGE_CONFDIR="/etc/weblounge"

# Where to put weblounge's log files
WEBLOUNGE_LOGDIR="/var/log/weblounge"

# Weblounge temporary files go here
WEBLOUNGE_TEMPDIR="/var/tmp/weblounge"


#####
# Weblounge configuration
##

# Where Felix will put the expanded bundles
FELIX_BUNDLECACHE="${WEBLOUNGE_TEMPDIR}/felix-cache"

# Where to have fileinstall check for relevant items
FELIX_FILEINSTALL_OPTS="-Dfelix.fileinstall.dir=${WEBLOUNGE_HOME}/load"

# Configuration admin's configuration location
PAX_CONFMAN_OPTS="-Dbundles.configuration.location=${WEBLOUNGE_CONFDIR}"

# Configuration of the logging facility
PAX_LOGGING_OPTS="-Dorg.ops4j.pax.logging.DefaultServiceLog.level=WARN -Dweblounge.logdir=${WEBLOUNGE_LOGDIR}"


#####
# Runtime environment setup
##

# The log file containing the init script's output
LOG_FILE="${WEBLOUNGE_LOGDIR}/weblounge.out"

# The pid
PID_FILE="/var/run/weblounge.pid"

# Weblounge's user
WEBLOUNGE_USER="felix"

# Weblounge's group
WEBLOUNGE_GROUP="felix"


#####
# JVM configuration
##

# Set's JAVA_HOME to current selected JRE
JAVA_HOME="$(java-config --jre-home)"

# JVM Memory configuration parameter
JVM_MEMORY_OPTS="-Xmx1024m -Xms512m"

# Path to additional jre libraries
JVM_LIBRARY_PATH="-Djava.library.path=$(java-config -i sun-jai-bin)"

# Options for the advanced windowing toolkit
JVM_AWT_OPTS="-Djava.awt.headless=true -Dawt.toolkit=sun.awt.HeadlessToolkit"

# Options to pass to the JVM on behalf of Jetty. Uncomment to disable debugging
#JVM_DEBUG_OPTS="-Xdebug -ea:ch -Xrunjdwp:transport=dt_socket,address=8001,server=y,suspend=n"

# Set the vm encoding
JVM_ENCODING_OPTS="-Dfile.encoding=utf-8"


####
# Java options and commandline
##

# The resulting weblounge-specific commandline options for java
WEBLOUNGE_OPTS="${FELIX_FILEINSTALL_OPTS} ${PAX_CONFMAN_OPTS} ${PAX_LOGGING_OPTS}"

# The resulting jvm-specific commandline options for java
JVM_OPTS="-server ${JVM_DEBUG_OPTS} ${JVM_MEMORY_OPTS} ${JVM_ENCODING_OPTS} ${JVM_LIBRARY_PATH} ${JVM_AWT_OPTS}"

# Final command
RUN_CMD="${JAVA_HOME}/bin/java"

# Final set of options to pass
RUN_OPTS="${JVM_OPTS} ${WEBLOUNGE_OPTS} -jar ${WEBLOUNGE_HOME}/bin/felix.jar ${FELIX_BUNDLECACHE}"
