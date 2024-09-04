SUMMARY = "Prometheus exporter for hardware and OS metrics"
DESCRIPTION = "Prometheus exporter for hardware and OS metrics exposed by *NIX kernels, written in Go with pluggable metric collectors."
HOMEPAGE = "https://github.com/prometheus/node_exporter"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

inherit update-rc.d useradd

PV = "1.8.2"

SRC_URI = "https://github.com/prometheus/node_exporter/releases/download/v${PV}/node_exporter-${PV}.linux-amd64.tar.gz \
           file://node_exporter.init \
           file://node_exporter.default"

SRC_URI[sha256sum] = "6809dd0b3ec45fd6e992c19071d6b5253aed3ead7bf0686885a51d85c6643c66"

S = "${WORKDIR}/node_exporter-${PV}.linux-amd64"

USERADD_PACKAGES = "${PN}"
GROUPADD_PARAM:${PN} = "-r node_exporter"
USERADD_PARAM:${PN} = "-r -g node_exporter -d /var/lib/node_exporter -s /sbin/nologin -c 'Node Exporter' node_exporter"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${S}/node_exporter ${D}${bindir}

    install -d ${D}${sysconfdir}/init.d
    install -m 0755 ${WORKDIR}/node_exporter.init ${D}${sysconfdir}/init.d/node_exporter

    install -d ${D}${sysconfdir}/default
    install -m 0644 ${WORKDIR}/node_exporter.default ${D}${sysconfdir}/default/node_exporter

    # Create directory for Node Exporter data
    install -d ${D}/var/lib/node_exporter

    # Set correct ownership
    chown -R node_exporter:node_exporter ${D}/var/lib/node_exporter
}

pkg_postinst_ontarget:${PN}() {
    # Ensure the log file can be created by the node_exporter user
    touch /tmp/node_exporter.log
    chown node_exporter:node_exporter /tmp/node_exporter.log
}

INITSCRIPT_NAME = "node_exporter"
INITSCRIPT_PARAMS = "defaults 92"

FILES:${PN} += "${sysconfdir} /var/lib/node_exporter"
