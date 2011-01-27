##
# Configure these variables to match your environment
##

# Main Weblounge path configuration. Make sure that the user executing Weblounge
# has write access to WEBLOUNGE_WORKDIR.

WEBLOUNGE_HOME="/Applications/Weblounge"
WEBLOUNGE_WORKDIR=$WEBLOUNGE_HOME

# Memory settings
MEMORY_OPTS="-Xmx1024m"

# Debug options
DEBUG_PORT="8000"
DEBUG_SUSPEND="n"

# Detail configuration for weblounge directories. Usually, it is fine to simply
# adjust the two paths above, namely $WEBLOUNGE_HOME and $WEBLOUNGE_WORKDIR.

WEBLOUNGE_LOGDIR="$WEBLOUNGE_WORKDIR/logs"
WEBLOUNGE_CACHEDIR="$WEBLOUNGE_WORKDIR/cache"
WEBLOUNGE_TEMPDIR="$WEBLOUNGE_WORKDIR/work"
WEBLOUNGE_SITESDIR="$WEBLOUNGE_WORKDIR/sites"
WEBLOUNGE_SITESDATADIR="$WEBLOUNGE_WORKDIR/sites-data"

##
# Only change the line below if you want to customize the server
##

WEBLOUNGE_OPTS="-Dweblounge.sitesdir=$WEBLOUNGE_SITESDIR -Dweblounge.sitesdatadir=$WEBLOUNGE_SITESDATADIR"
WEBLOUNGE_FILEINSTALL_OPTS="-Dfelix.fileinstall.dir=$WEBLOUNGE_HOME/load"
PAX_CONFMAN_OPTS="-Dbundles.configuration.location=$WEBLOUNGE_HOME/conf"
PAX_LOGGING_OPTS="-Dorg.ops4j.pax.logging.DefaultServiceLog.level=WARN -Dweblounge.logdir=$WEBLOUNGE_LOGDIR"
GRAPHICS_OPTS="-Djava.awt.headless=true -Dawt.toolkit=sun.awt.HeadlessToolkit"
TEMPDIR_OPTS="-Djava.io.tmpdir=$WEBLOUNGE_TEMPDIR"

# Create the debug config
DEBUG_OPTS="-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=$DEBUG_PORT,server=y,suspend=$DEBUG_SUSPEND"

# Create the java runtime options
RUNTIME_OPTS="$WEBLOUNGE_OPTS $TEMPDIR_OPTS $GRAPHICS_OPTS $WEBLOUNGE_FILEINSTALL_OPTS $PAX_CONFMAN_OPTS $PAX_LOGGING_OPTS"

# Make sure weblounge bundles are reloaded
for bundle in `find $WEBLOUNGE_CACHEDIR -type f -name bundle.location | xargs grep --files-with-match -e "file:" | sed -e s/bundle.location// `; do
  rm -r $bundle
done

# Finally start Weblounge
java $MEMORY_OPTS $RUNTIME_OPTS $DEBUG_OPTS -jar $WEBLOUNGE_HOME/bin/felix.jar $WEBLOUNGE_CACHEDIR