@ECHO OFF
SETLOCAL
REM ##
REM # Configure these variables to match your environment
REM # If you have system-wide variables for WEBLOUNGE_HOME and M2_REPO then you
REM # should not have to make any changes to this file.
REM ##

REM # Main Weblounge path configuration. Make sure that the user executing Weblounge
REM # has write access to WEBLOUNGE_WORKDIR.

REM # Weblounge home
SET WEBLOUNGE_HOME="C:\Program Files\Weblounge"
SET WEBLOUNGE_WORKDIR=%WEBLOUNGE_HOME%

REM # Memory settings
SET MEMORY_OPTS=-Xmx1024m

REM # Felix debug options
SET DEBUG_PORT=8000
SET DEBUG_SUSPEND=n

REM # Detail configuration for weblounge directories. Usually, it is fine to simply
REM # adjust the two paths above, namely $WEBLOUNGE_HOME and $WEBLOUNGE_WORKDIR.

SET WEBLOUNGE_LOGDIR=%WEBLOUNGE_WORKDIR%\logs
SET WEBLOUNGE_CACHEDIR=%WEBLOUNGE_WORKDIR%\cache
SET WEBLOUNGE_TEMPDIR=%WEBLOUNGE_WORKDIR%\work
SET WEBLOUNGE_SITESDIR=%WEBLOUNGE_WORKDIR%\sites
SET WEBLOUNGE_SITESDATADIR=%WEBLOUNGE_WORKDIR%\sites-data

REM ##
REM # Only change the lines below if you know what you are doing
REM ##

SET WEBLOUNGE_OPTS=-Dweblounge.sitesdir=%WEBLOUNGE_SITESDIR% -Dweblounge.sitesdatadir=%WEBLOUNGE_SITESDATADIR%
SET WEBLOUNGE_FILEINSTALL_OPTS=-Dfelix.fileinstall.dir=%WEBLOUNGE_HOME%\load
SET PAX_CONFMAN_OPTS=-Dbundles.configuration.location=%WEBLOUNGE_HOME%\conf -Dweblounge.logdir=%WEBLOUNGE_LOGDIR%
SET PAX_LOGGING_OPTS=-Dorg.ops4j.pax.logging.DefaultServiceLog.level=WARN
SET GRAPHICS_OPTS=-Djava.awt.headless=true -Dawt.toolkit=sun.awt.HeadlessToolkit
SET TEMPDIR_OPTS=-Djava.io.tmpdir=%WEBLOUNGE_TEMPDIR%

REM # Create the debug config
SET DEBUG_OPTS=-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=%DEBUG_PORT%,server=y,suspend=%DEBUG_SUSPEND%

REM # Create the java runtime options
RUNTIME_OPTS=%WEBLOUNGE_OPTS% %TEMPDIR_OPTS% %GRAPHICS_OPTS% %WEBLOUNGE_FILEINSTALL_OPTS% %PAX_CONFMAN_OPTS% %PAX_LOGGING_OPTS%

REM # Finally start Weblounge
java %MEMORY_OPTS% %RUNTIME_OPTS% %DEBUG_OPTS% -jar %WEBLOUNGE_HOME%\bin\felix.jar %WEBLOUNGE_CACHEDIR%

ENDLOCAL