##
# Configure these variables to match your environment
##

if [ -z "$WEBLOUNGE_HOME" ]; then
  echo "Please define WEBLOUNGE_HOME"
  exit 1
fi

# Main Weblounge path configuration. Make sure that the user executing Weblounge
# has write access to WEBLOUNGE_WORK_DIR.

WEBLOUNGE_WORK_DIR="$WEBLOUNGE_HOME"

# Memory settings
MEMORY_OPTS="-Xmx1024m"

# Debug options
DEBUG_PORT="8000"
DEBUG_SUSPEND="n"

# Detail configuration for weblounge directories. Usually, it is fine to simply
# adjust the two paths above, namely $WEBLOUNGE_HOME and $WEBLOUNGE_WORK_DIR.

WEBLOUNGE_LOG_DIR="$WEBLOUNGE_WORK_DIR/logs"
WEBLOUNGE_CACHE_DIR="$WEBLOUNGE_WORK_DIR/cache"
WEBLOUNGE_TEMP_DIR="$WEBLOUNGE_WORK_DIR/work"
WEBLOUNGE_SITES_DIR="$WEBLOUNGE_WORK_DIR/sites"
WEBLOUNGE_SITESDATA_DIR="$WEBLOUNGE_WORK_DIR/sites-data"

##
# Only change the line below if you want to customize the server
##

WEBLOUNGE_SITES_OPTS="-Dweblounge.sitesdir=$WEBLOUNGE_SITES_DIR"
WEBLOUNGE_SITES_DATA_OPTS="-Dweblounge.sitesdatadir=$WEBLOUNGE_SITESDATA_DIR"
WEBLOUNGE_FILEINSTALL_OPTS="-Dfelix.fileinstall.dir=$WEBLOUNGE_HOME/load"
PAX_CONFMAN_OPTS="-Dbundles.configuration.location=$WEBLOUNGE_HOME/conf"
PAX_LOGGING_OPTS="-Dorg.ops4j.pax.logging.DefaultServiceLog.level=WARN -Dweblounge.logdir=$WEBLOUNGE_LOG_DIR"
GRAPHICS_OPTS="-Djava.awt.headless=true -Dawt.toolkit=sun.awt.HeadlessToolkit"
TEMP_DIR_OPTS="-Djava.io.tmpdir=$WEBLOUNGE_TEMP_DIR"

# Create the debug config
DEBUG_OPTS="-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=$DEBUG_PORT,server=y,suspend=$DEBUG_SUSPEND"

# Create the directories
mkdir -p "$WEBLOUNGE_LOG_DIR"
mkdir -p "$WEBLOUNGE_CACHE_DIR"
mkdir -p "$WEBLOUNGE_TEMP_DIR"
mkdir -p "$WEBLOUNGE_SITES_DIR"
mkdir -p "$WEBLOUNGE_SITESDATA_DIR"

# Finally start Weblounge
java $MEMORY_OPTS $DEBUG_OPTS "$WEBLOUNGE_SITES_OPTS" "$WEBLOUNGE_SITES_DATA_OPTS" "$TEMP_DIR_OPTS" "$GRAPHICS_OPTS" "$WEBLOUNGE_FILEINSTALL_OPTS" "$PAX_CONFMAN_OPTS" "$PAX_LOGGING_OPTS" -jar "$WEBLOUNGE_HOME/bin/felix.jar" "$WEBLOUNGE_CACHE_DIR"