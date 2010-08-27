#!/sbin/runscript
# Distributed under the terms of the GNU General Public License v2

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
        cd "${WEBLOUNGE_HOME}"
        start-stop-daemon --start --background --chuid ${WEBLOUNGE_USER}:${WEBLOUNGE_GROUP} --make-pidfile --pidfile "${PID_FILE}" --exec "${RUN_CMD}" -- ${RUN_OPTS} >> "${LOG_FILE}"
        eend $?
} 

stop()  {
        ebegin "Stopping Weblounge"
        start-stop-daemon --stop --quiet --pidfile ${PID_FILE} --name java
        if [ -e $PIDFILE ]; then
          rm ${PID_FILE}
        fi
        eend $?
}

restart() {
        svc_stop
        sleep 1
        svc_start
}
