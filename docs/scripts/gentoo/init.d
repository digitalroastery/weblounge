#!/sbin/runscript
#
# Distributed under the terms of the GNU General Public License v2
#
# Note that the gogo shell arguments (-Dgosh.args) are needed here rather
# than in the conf.d file because there is now other way than this to get
# the quotes right for the start-stop-daemon.
# The arguments are needed to convince gogo to run in non-interactive mode
# without shutting down the framework when the start-stop-daemon detaches.

depend() {
        use net
        after logger
}

start() {
        ebegin "Starting Weblounge"
        mkdir -p "${WEBLOUNGE_LOGDIR}" "${FELIX_BUNDLECACHE}"
        chown -R ${WEBLOUNGE_USER}:${WEBLOUNGE_GROUP} "${WEBLOUNGE_LOGDIR}" "${FELIX_BUNDLECACHE}"
        if [ ! -d "${WEBLOUNGE_HOME}" ]; then
            eerror "Weblounge home directory ${WEBLOUNGE_HOME} not found"
            return 1
        fi
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
