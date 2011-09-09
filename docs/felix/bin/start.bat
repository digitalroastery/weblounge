@ECHO OFF
SETLOCAL ENABLEEXTENSIONS

REM # Main Weblounge path configuration.
REM #
REM # Make sure that the user executing Weblounge has write access to WEBLOUNGE_WORKDIR.
REM # Also note the following important rules on path configuration:
REM # - file paths must contain forward slashes only
REM # - file paths that contain spaces need to be enclosed in double quotes (")
REM # Therefore, a valid file path would be "C:/Program Files/Weblounge".

REM # Weblounge home
REM # SET WEBLOUNGE_HOME="C:/Program Files/Weblounge"
REM # SET WEBLOUNGE_WORKDIR=%WEBLOUNGE_HOME%

IF "%WEBLOUNGE_HOME%"=="" GOTO QUICKEND

REM # Memory settings
SET MEMORY_OPTS=-Xmx1024m -XX:MaxPermSize=256m

REM # Felix debug options
SET DEBUG_PORT=8000
SET DEBUG_SUSPEND=n

REM # Detail configuration for weblounge directories. Usually, it is fine to simply
REM # adjust the two paths above, namely WEBLOUNGE_HOME% and WEBLOUNGE_WORKDIR.

SET WEBLOUNGE_LOGDIR=%WEBLOUNGE_WORKDIR%/logs
SET WEBLOUNGE_CACHEDIR=%WEBLOUNGE_WORKDIR%/cache
SET WEBLOUNGE_TEMPDIR=%WEBLOUNGE_WORKDIR%/work
SET WEBLOUNGE_SITESDIR=%WEBLOUNGE_WORKDIR%/sites
SET WEBLOUNGE_SITESDATADIR=%WEBLOUNGE_WORKDIR%/sites-data
SET WEBLOUNGE_SERVICES=

REM #
REM # Only change the lines below if you know what you are doing
REM #

SET WEBLOUNGE_SITES_OPTS=-Dweblounge.sitesdir=%WEBLOUNGE_SITESDIR% 
SET WEBLOUNGE_SITES_DATA_OPTS=-Dweblounge.sitesdatadir=%WEBLOUNGE_SITESDATADIR%
SET WEBLOUNGE_FILEINSTALL_OPTS=-Dfelix.fileinstall.dir=%WEBLOUNGE_HOME%/load
SET PAX_CONFMAN_OPTS=-Dbundles.configuration.location=%WEBLOUNGE_HOME%/conf 
SET PAX_LOGGING_OPTS=-Dorg.ops4j.pax.logging.DefaultServiceLog.level=WARN
SET PAX_WEB_OPTS=-Dorg.ops4j.pax.web.config.file=%WEBLOUNGE_HOME%/conf/jetty.xml
SET WEBLOUNGE_LOGGING_OPTS=-Dweblounge.logdir=%WEBLOUNGE_LOGDIR%
SET GRAPHICS_OPTS=-Djava.awt.headless=true -Dawt.toolkit=sun.awt.HeadlessToolkit
SET TEMPDIR_OPTS=-Djava.io.tmpdir=%WEBLOUNGE_TEMPDIR%
SET LIB_DIR=-Dweblounge.libdir=%WEBLOUNGE_HOME%/lib

REM # Create the debug config

SET DEBUG_OPTS=-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=%DEBUG_PORT%,server=y,suspend=%DEBUG_SUSPEND%

REM # Create the java runtime options

SET RUNTIME_OPTS=%WEBLOUNGE_SITES_OPTS% %WEBLOUNGE_SITES_DATA_OPTS% %TEMPDIR_OPTS% %GRAPHICS_OPTS% %WEBLOUNGE_FILEINSTALL_OPTS% %PAX_CONFMAN_OPTS% %WEBLOUNGE_LOGGING_OPTS% %PAX_LOGGING_OPTS% %LIB_DIR%

REM # Create the directories
IF NOT EXIST %WEBLOUNGE_LOGDIR% mkdir "%WEBLOUNGE_LOGDIR%"
IF NOT EXIST %WEBLOUNGE_CACHEDIR% mkdir "%WEBLOUNGE_CACHEDIR%"
IF NOT EXIST %WEBLOUNGE_TEMPDIR% mkdir "%WEBLOUNGE_TEMPDIR%"
IF NOT EXIST %WEBLOUNGE_SITESDIR% mkdir "%WEBLOUNGE_SITESDIR%"
IF NOT EXIST %WEBLOUNGE_SITESDATADIR% mkdir "%WEBLOUNGE_SITESDATADIR%"

REM # Finally start Weblounge
java %MEMORY_OPTS% %RUNTIME_OPTS% %DEBUG_OPTS% -jar %WEBLOUNGE_HOME%/bin/felix.jar %WEBLOUNGE_CACHEDIR%

GOTO END

:QUICKEND
echo Please define WEBLOUNGE_HOME

:END
ENDLOCAL