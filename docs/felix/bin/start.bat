SETLOCAL
REM ##
REM # Configure these variables to match your environment
REM # If you have system-wide variables for FELIX_HOME and M2_REPO then you
REM # should not have to make any changes to this file.
REM ##

REM # Make sure the following two path entries do *not* contain spaces
SET FELIX_HOME=C:\Libraries\felix-framework-2.0.0
SET M2_REPO=C:\Users\johndoe\.m2\repository

SET DEBUG_PORT=8000
SET DEBUG_SUSPEND=n

REM ##
REM # Only change the lines below if you know what you are doing
REM ##

SET FELIX_FILEINSTALL_OPTS=-Dfelix.fileinstall.dir=%FELIX_HOME%\load
SET PAX_CONFMAN_OPTS=-Dbundles.configuration.location=%FELIX_HOME%\conf
SET FELIX_CACHE=%FELIX_HOME%\felix-cache

REM # Clear felix cache dir
del /FQ %FELIX_CACHE%

REM # Create the debug config
SET DEBUG_OPTS=-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=%DEBUG_PORT%,server=y,suspend=%DEBUG_SUSPEND%

REM # Finally start felix
java %DEBUG_OPTS% -DM2_REPO=%M2_REPO% %FELIX_FILEINSTALL_OPTS% %PAX_CONFMAN_OPTS% -jar %FELIX_HOME%\bin\felix.jar %FELIX_CACHE%  
ENDLOCAL