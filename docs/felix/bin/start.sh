##
# Configure these variables to match your environment
##

# Felix home
FELIX="/Applications/Weblounge"
FELIX_LOGDIR="$FELIX/logs"
FELIX_CACHEDIR="$FELIX/cache"
FELIX_TEMPDIR="$FELIX/work"

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
PAX_LOGGING_OPTS="-Dorg.ops4j.pax.logging.DefaultServiceLog.level=WARN -Dweblounge.logdir=$FELIX_LOGDIR"
GRAPHICS_OPTS="-Djava.awt.headless=true -Dawt.toolkit=sun.awt.HeadlessToolkit"
TEMPDIR_OPTS="-Djava.io.tmpdir=$FELIX_TEMPDIR"

# Create the debug config
DEBUG_OPTS="-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=$DEBUG_PORT,server=y,suspend=$DEBUG_SUSPEND"

# Make sure weblounge bundles are reloaded
for bundle in `find $FELIX_CACHEDIR -type f -name bundle.location | xargs grep --files-with-match -e "file:" | sed -e s/bundle.location// `; do
  rm -r $bundle
done

# Finally start felix
cd $FELIX
java $DEBUG_OPTS $TEMPDIR_OPTS $MAVEN_OPTS $GRAPHICS_OPTS $FELIX_FILEINSTALL_OPTS $PAX_CONFMAN_OPTS $PAX_LOGGING_OPTS -jar $FELIX/bin/felix.jar $FELIX_CACHEDIR