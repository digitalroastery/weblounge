#!/sbin/runscript
# Copyright 1999-2004 Gentoo Foundation
# Distributed under the terms of the GNU General Public License v2

depend() {
        use net
}

start() {
        ebegin "Starting Weblounge $1"
        mkdir -p "${WEBLOUNGE_LOGDIR}" "${FELIX_BUNDLECACHE}"
        touch "${LOG_FILE}"
        chown -R ${WEBLOUNGE_USER}:${WEBLOUNGE_GROUP} "${WEBLOUNGE_LOGDIR}" "${FELIX_BUNDLECACHE}"
        start-stop-daemon --start --user ${WEBLOUNGE_USER} --background --make-pidfile --pidfile ${PID_FILE} --exec ${JAVA_HOME}/bin/java -- ${JAVA_CMD} >> "${LOG_FILE}" 2>&1
        eend $?
}

stop()  {
        ebegin "Stopping Weblounge $1"
        start-stop-daemon --stop --user ${WEBLOUNGE_USER} --pidfile ${PID_FILE}
        eend $?
}