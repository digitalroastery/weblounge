##
# Configure these variables to match your environment
##

FELIX="/Applications/Weblounge"
M2_REPO="/Users/wunden/.m2/repository"
DEBUG_PORT="8000"
DEBUG_SUSPEND="n"

##
# Only change the line below if you want to customize the server
##

MAVEN_OPTS="-DM2_REPO=$M2_REPO"
FELIX_FILEINSTALL_OPTS="-Dfelix.fileinstall.dir=$FELIX/load"
PAX_CONFMAN_OPTS="-Dbundles.configuration.location=$FELIX/conf"

# Clear the felix cache directory
FELIX_CACHE="$FELIX/felix-cache"
rm -rf $FELIX_CACHE

# Create the debug config
DEBUG_OPTS="-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=$DEBUG_PORT,server=y,suspend=$DEBUG_SUSPEND"

# Finally start felix
cd $FELIX
java $DEBUG_OPTS $MAVEN_OPTS $FELIX_FILEINSTALL_OPTS $PAX_CONFMAN_OPTS -jar $FELIX/bin/felix.jar $FELIX_CACHE