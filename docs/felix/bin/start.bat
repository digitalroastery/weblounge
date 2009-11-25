SETLOCAL
REM ##
REM # Configure these variables to match your environment
REM # If you have system-wide variables for FELIX_HOME and M2_REPO then you
REM # should not have to make any changes to this file.
REM ##

REM # Make sure the following two path entries do *not* contain spaces

REM # Felix home
SET FELIX_HOME=C:\Libraries\felix-framework-2.0.0

REM # Maven home
SET M2_REPO=C:\Users\johndoe\.m2\repository

REM # Felix debug options
SET DEBUG_PORT=8000
SET DEBUG_SUSPEND=n

REM ##
REM # Only change the lines below if you know what you are doing
REM ##

SET MAVEN_OPTS=-DM2_REPO=%M2_REPO%
SET FELIX_FILEINSTALL_OPTS=-Dfelix.fileinstall.dir=%FELIX_HOME%\load
SET PAX_CONFMAN_OPTS=-Dbundles.configuration.location=%FELIX_HOME%\conf
SET PAX_LOGGING_OPTS=-Dorg.ops4j.pax.logging.DefaultServiceLog.level=WARN

REM # Clear felix cache dir
SET FELIX_CACHE=%FELIX_HOME%\felix-cache
del /FQ %FELIX_CACHE%

REM # Create the debug config
SET DEBUG_OPTS=-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=%DEBUG_PORT%,server=y,suspend=%DEBUG_SUSPEND%

REM # Finally start felix
java %DEBUG_OPTS% %MAVEN_OPTS% %FELIX_FILEINSTALL_OPTS% %PAX_CONFMAN_OPTS% %PAX_LOGGING_OPTS% -jar %FELIX_HOME%\bin\felix.jar %FELIX_CACHE%  
ENDLOCAL