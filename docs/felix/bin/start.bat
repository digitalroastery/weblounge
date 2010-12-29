SETLOCAL
ECHO OFF
REM ##
REM # Configure these variables to match your environment
REM # If you have system-wide variables for FELIX_HOME and M2_REPO then you
REM # should not have to make any changes to this file.
REM ##

REM # Make sure the following two path entries do *not* contain spaces
REM # or are escaped by double quotes.
REM # Also, make sure that the user executing Weblounge has write access
REM # to FELIX_WORKDIR.

REM # Felix home
SET FELIX_HOME="C:\Program Files\Weblounge"
SET FELIX_WORKDIR="C:\Users\johndoe\AppData\Local\Weblounge"
SET FELIX_LOGDIR=%FELIX_WORKDIR%\logs
SET FELIX_CACHEDIR=%FELIX_WORKDIR%\cache
SET FELIX_TEMPDIR=%FELIX_WORKDIR%\work

REM # Maven home. This variable needs to be set if certain bundles are
REM # being referenced from the local maven repository in FELIX_HOME/conf
REM # 
SET M2_REPO=C:\Users\johndoe\.m2\repository

REM # Felix debug options
SET DEBUG_PORT=8000
SET DEBUG_SUSPEND=n

REM ##
REM # Only change the lines below if you know what you are doing
REM ##

SET MAVEN_OPTS=-DM2_REPO=%M2_REPO%
SET FELIX_FILEINSTALL_OPTS=-Dfelix.fileinstall.dir=%FELIX_HOME%\load
SET PAX_CONFMAN_OPTS=-Dbundles.configuration.location=%FELIX_HOME%\conf -Dweblounge.logdir=%FELIX_LOGDIR%
SET PAX_LOGGING_OPTS=-Dorg.ops4j.pax.logging.DefaultServiceLog.level=WARN
SET GRAPHICS_OPTS=-Djava.awt.headless=true -Dawt.toolkit=sun.awt.HeadlessToolkit
SET TEMPDIR_OPTS=-Djava.io.tmpdir=$FELIX_TEMPDIR
SET MEMORY_OPTS=-Xmx2048m

REM # Create the debug config
SET DEBUG_OPTS=-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=%DEBUG_PORT%,server=y,suspend=%DEBUG_SUSPEND%

REM # Remove the weblounge bundles from felix cache

REM # Finally start felix
java %MEMORY_OPTS% %DEBUG_OPTS% %TEMPDIR_OPTS% %MAVEN_OPTS% %GRAPHICS_OPTS% %FELIX_FILEINSTALL_OPTS% %PAX_CONFMAN_OPTS% %PAX_LOGGING_OPTS% -jar %FELIX_HOME%\bin\felix.jar %FELIX_CACHEDIR%
ENDLOCAL