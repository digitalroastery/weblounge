#!/sbin/runscript
#
# Distributed under the terms of the GNU General Public License v2
#
# Note that the gogo shell arguments (-Dgosh.args) are needed here rather
# than in the conf.d file because there is now other way than this to get
# the quotes right for the start-stop-daemon.
# The arguments are needed to convince gogo to run in non-interactive mode 
# without shutting down the framework when the start-stop-daemon detaches.

opts="${opts} reset update"

depend() {
        use net
        after logger
}

start() {
        ebegin "Starting Weblounge"
        if [ ! -d "${WEBLOUNGE_HOME}" ]; then
	        eerror "Weblounge home directory ${WEBLOUNGE_HOME} not found"
            return 1
	    elif [ -z "${WEBLOUNGE_SITES_DIR}" ]; then
		    eerror "Weblounge sites directory (WEBLOUNGE_SITES_DIR) not set"
	        return 1
	    elif [ -z "${WEBLOUNGE_SITES_DATA_DIR}" ]; then
		    eerror "Weblounge sites data directory (WEBLOUNGE_SITES_DATA_DIR) not set"
	        return 1
	    elif [ -z "${WEBLOUNGE_LOG_DIR}" ]; then
		    eerror "Weblounge log directory (WEBLOUNGE_LOG_DIR) not set"
	        return 1
	    elif [ -z "${FELIX_BUNDLECACHE_DIR}" ]; then
		    eerror "Felix bundle cache directory (FELIX_BUNDLECACHE_DIR) not set"
	        return 1
	    elif [ -z "${PID_FILE}" ]; then
		    eerror "Felix pid file (PID_FILE) not set"
	        return 1
	    elif [ -z "${LOG_FILE}" ]; then
		    eerror "Weblounge log file (LOG_FILE) not set"
	        return 1
	    elif [ -z "${RUN_CMD}" ]; then
		    eerror "Java commandline (RUN_CMD) not set"
	        return 1
	    elif [ -z "${RUN_OPTS}" ]; then
		    eerror "Java options (RUN_OPTS) not set"
	        return 1
	    fi
        mkdir -p "${WEBLOUNGE_LOG_DIR}" "${FELIX_BUNDLECACHE_DIR}" "${WEBLOUNGE_SITES_DIR}" "${WEBLOUNGE_SITES_DATA_DIR}"
        chown -R ${WEBLOUNGE_USER}:${WEBLOUNGE_GROUP} "${WEBLOUNGE_LOG_DIR}" "${FELIX_BUNDLECACHE_DIR}" "${WEBLOUNGE_SITES_DIR}" "${WEBLOUNGE_SITES_DATA_DIR}"
        start-stop-daemon --start \
            --background \
	        --user ${WEBLOUNGE_USER} \
            --group ${WEBLOUNGE_GROUP} \
	        --chdir ${WEBLOUNGE_HOME} \
            --make-pidfile \
            --pidfile "${PID_FILE}" \
            --stdout "${LOG_FILE}" \
            --stderr "${LOG_FILE}" \
            --exec "${RUN_CMD}" -- -Dgosh.args="--noshutdown -c noop=true" ${RUN_OPTS}
        eend $result
}

stop()  {
        ebegin "Stopping Weblounge"
        start-stop-daemon --stop \
            --pidfile "${PID_FILE}"
        if [ -e "${PID_FILE}" ]; then
          rm "${PID_FILE}"
        fi
        eend $?
}

reset() {
		if [ -z "${WEBLOUNGE_TEMP_DIR}" ]; then
		    eerror "Weblounge work directory (WEBLOUNGE_TEMP_DIR) not set"
		    return 1
		fi

	    STARTED=
		if [ -e "${PID_FILE}" ]; then
	    	STARTED="true"
		    stop
		fi
		
		ebegin "Removing weblounge work files"
		rm -rf ${WEBLOUNGE_TEMP_DIR}
		eend $?
		
		if [ ! -z "${STARTED}" ]; then
		    start
		fi
}

update() {
		if [ -z "${FELIX_BUNDLECACHE_DIR}" ]; then
		    eerror "Felix bundle cache directory (FELIX_BUNDLECACHE_DIR) not set"
		    return 1
		elif [ -z "${WEBLOUNGE_TEMP_DIR}" ]; then
		    eerror "Weblounge work directory (WEBLOUNGE_TEMP_DIR) not set"
		    return 1
		fi

	    STARTED=
		if [ -e "${PID_FILE}" ]; then
	    	STARTED="true"
		    stop
		fi
		
		ebegin "Cleaning felix bundle cache"
		rm -rf ${FELIX_BUNDLECACHE_DIR}
		eend $?
		
		ebegin "Removing weblounge work files"
		rm -rf ${WEBLOUNGE_TEMP_DIR}
		eend $?
		
		if [ ! -z "${STARTED}" ]; then
		    start
		fi
}
