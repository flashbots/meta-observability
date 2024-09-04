SUMMARY = "Process exporter for Prometheus"
DESCRIPTION = "Prometheus exporter that mines /proc to report on selected processes"
HOMEPAGE = "https://github.com/ncabatoff/process-exporter"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=8748f01be17d4b2ce4421ce67a12c479"

inherit update-rc.d useradd

PV = "0.8.3"

SRC_URI = "https://github.com/ncabatoff/process-exporter/releases/download/v${PV}/process-exporter-${PV}.linux-amd64.tar.gz \
           file://process-exporter.init \
           file://process-exporter.default \
           file://process-exporter.yaml"

SRC_URI[sha256sum] = "249db36771a4e66eaacca0ce31294de200df30eaf59a190c46639b98c5815969"

S = "${WORKDIR}/process-exporter-${PV}.linux-amd64"

USERADD_PACKAGES = "${PN}"
GROUPADD_PARAM:${PN} = "-r process-exporter"
USERADD_PARAM:${PN} = "-r -g process-exporter -d /var/lib/process-exporter -s /sbin/nologin -c 'Process Exporter' process-exporter"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${S}/process-exporter ${D}${bindir}

    install -d ${D}${sysconfdir}/process-exporter
    install -m 0644 ${WORKDIR}/process-exporter.yaml ${D}${sysconfdir}/process-exporter/

    install -d ${D}${sysconfdir}/init.d
    install -m 0755 ${WORKDIR}/process-exporter.init ${D}${sysconfdir}/init.d/process-exporter

    install -d ${D}${sysconfdir}/default
    install -m 0644 ${WORKDIR}/process-exporter.default ${D}${sysconfdir}/default/process-exporter

    # Create directory for Process Exporter data
    install -d ${D}/var/lib/process-exporter

    # Set correct ownership
    chown -R process-exporter:process-exporter ${D}/var/lib/process-exporter
    chown -R process-exporter:process-exporter ${D}${sysconfdir}/process-exporter
}

pkg_postinst_ontarget:${PN}() {
    # Ensure the log file can be created by the process-exporter user
    touch /tmp/process-exporter.log
    chown process-exporter:process-exporter /tmp/process-exporter.log
}

INITSCRIPT_NAME = "process-exporter"
INITSCRIPT_PARAMS = "defaults 92"

FILES:${PN} += "${sysconfdir} /var/lib/process-exporter"

# Override QA check for already-stripped binaries
INSANE_SKIP:${PN} += "already-stripped"
