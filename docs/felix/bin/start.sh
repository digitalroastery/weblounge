##
# Configure these variables to match your environment
##

# Felix home
FELIX="/Applications/Weblounge"
FELIX_LOGDIR="$FELIX/logs"
FELIX_CACHEDIR="$FELIX/cache"

# Maven home
M2_REPO="/Users/johndoe/.m2/repository"

# Felix debug options
DEBUG_PORT="8000"
DEBUG_SUSPEND="n"

##
# Only change the line below if you want to customize the server
##

MAVEN_OPTS="-DM2_REPO=$M2_REPO"
FELIX_FILEINSTALL_OPTS="-Dfelix.fileinstall.dir=$FELIX/load"
PAX_CONFMAN_OPTS="-Dbundles.configuration.location=$FELIX/conf"
PAX_LOGGING_OPTS="-Dorg.ops4j.pax.logging.DefaultServiceLog.level=WARN  -Dweblounge.logdir=$FELIX_LOGDIR"

# Create the debug config
DEBUG_OPTS="-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=$DEBUG_PORT,server=y,suspend=$DEBUG_SUSPEND"

# Finally start felix
cd $FELIX
java $DEBUG_OPTS $MAVEN_OPTS $FELIX_FILEINSTALL_OPTS $PAX_CONFMAN_OPTS $PAX_LOGGING_OPTS -jar $FELIX/bin/felix.jar $FELIX_CACHEDIR