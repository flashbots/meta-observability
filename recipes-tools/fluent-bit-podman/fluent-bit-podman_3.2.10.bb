SUMMARY = "Fast Log processor and Forwarder"
DESCRIPTION = "Fluent Bit is a data collector, processor and  \
forwarder for Linux. It supports several input sources and \
backends (destinations) for your data. \
"

HOMEPAGE = "http://fluentbit.io"
BUGTRACKER = "https://github.com/fluent/fluent-bit/issues"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

SECTION = "net"

FILESEXTRAPATHS:prepend := "${THISDIR}:"

SRC_URI += "file://init \
            file://aws-auth.mustache \
            file://td-agent-bit.conf.mustache \
            file://parser.conf"

INITSCRIPT_NAME = "td-agent-bit"
INITSCRIPT_PARAMS = "defaults 98"

inherit update-rc.d useradd

USERADD_PACKAGES = "${PN}"
GROUPADD_PARAM:${PN} = "-r fluentbit"
USERADD_PARAM:${PN} = "-r -g fluentbit -d /var/lib/td-agent-bit -s /sbin/nologin -c 'Fluent Bit' fluentbit"

do_install() {
    # Install service
    install -d ${D}${sysconfdir}/init.d
    install -m 0755 ${WORKDIR}/init ${D}${sysconfdir}/init.d/td-agent-bit

    # Install config files
    install -d -m 0755 ${D}${sysconfdir}/td-agent-bit
    install -m 0600 ${THISDIR}/aws-auth.mustache ${D}${sysconfdir}/td-agent-bit/aws-auth.mustache
    install -m 0600 ${THISDIR}/td-agent-bit.conf.mustache ${D}${sysconfdir}/td-agent-bit/td-agent-bit.conf.mustache
    install -m 0600 ${THISDIR}/parser.conf ${D}${sysconfdir}/td-agent-bit/parser.conf
    
    chown -R fluentbit:fluentbit ${D}${sysconfdir}/td-agent-bit/*
}

FILES:${PN} += "${sysconfdir}/init.d/${INITSCRIPT_NAME}"
FILES:${PN} += "${sysconfdir}/td-agent-bit/aws-auth.mustache"
FILES:${PN} += "${sysconfdir}/td-agent-bit/td-agent-bit.conf.mustache"
FILES:${PN} += "${sysconfdir}/td-agent-bit/parser.conf"
