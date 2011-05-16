The log4j.properties file is used to configure logging output for unit tests at
build time (note: in order to configure logging for the runtime environment,
the configuration file is located in felix/conf/services and is named
org.ops4j.pax.logging.properties).

The file log4j.properties is being copied to each individual module during the
maven build and is therefore valid for all submodules.