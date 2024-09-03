SUMMARY = "Prometheus monitoring system and time series database"
DESCRIPTION = "Prometheus is an open-source systems monitoring and alerting toolkit"
HOMEPAGE = "https://prometheus.io/"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

inherit update-rc.d useradd

PV = "2.54.0"

SRC_URI = "https://github.com/prometheus/prometheus/releases/download/v${PV}/prometheus-${PV}.linux-amd64.tar.gz \
           file://prometheus.init \
           file://prometheus.default \
           file://prometheus.yml"

SRC_URI[sha256sum] = "465e1393a0cca9705598f6ffaf96ffa78d0347808ab21386b0c6aaec2cf7aa13"

S = "${WORKDIR}/prometheus-${PV}.linux-amd64"

USERADD_PACKAGES = "${PN}"
GROUPADD_PARAM:${PN} = "-r prometheus"
USERADD_PARAM:${PN} = "-r -g prometheus -d /var/lib/prometheus -s /sbin/nologin -c 'Prometheus monitoring system' prometheus"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${S}/prometheus ${D}${bindir}
    install -m 0755 ${S}/promtool ${D}${bindir}

    install -d ${D}${sysconfdir}/prometheus
    install -m 0644 ${WORKDIR}/prometheus.yml ${D}${sysconfdir}/prometheus/

    install -d ${D}${sysconfdir}/init.d
    install -m 0755 ${WORKDIR}/prometheus.init ${D}${sysconfdir}/init.d/prometheus

    install -d ${D}${sysconfdir}/default
    install -m 0644 ${WORKDIR}/prometheus.default ${D}${sysconfdir}/default/prometheus

    install -d ${D}${datadir}/prometheus
    cp -r ${S}/console_libraries ${D}${datadir}/prometheus/
    cp -r ${S}/consoles ${D}${datadir}/prometheus/

    # Create directory for Prometheus data
    install -d ${D}/var/lib/prometheus

    # Set correct ownership
    chown -R prometheus:prometheus ${D}/var/lib/prometheus
    chown -R prometheus:prometheus ${D}${sysconfdir}/prometheus
}

pkg_postinst_ontarget:${PN}() {
    # Ensure the log file can be created by the prometheus user
    touch /tmp/prometheus.log
    chown prometheus:prometheus /tmp/prometheus.log
}

INITSCRIPT_NAME = "prometheus"
INITSCRIPT_PARAMS = "defaults 85 15"

FILES:${PN} += "${sysconfdir} ${datadir}/prometheus /var/lib/prometheus"
