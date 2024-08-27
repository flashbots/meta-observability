SUMMARY = "Prometheus monitoring system and time series database"
DESCRIPTION = "Prometheus is an open-source systems monitoring and alerting toolkit"
HOMEPAGE = "https://prometheus.io/"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://src/${GO_WORKDIR}/LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

inherit go-mod
inherit update-rc.d

GO_IMPORT = "github.com/prometheus/prometheus"

SRC_URI = "git://${GO_IMPORT};branch=release-2.54;protocol=https \
           file://prometheus.init \
           file://prometheus.default"
SRCREV = "v2.54.0"
PV = "2.54.0"

DEPENDS += "promu-native"

# Specify required Go version
GO_VERSION ?= "1.22%"
COMPATIBLE_HOST:libc-musl:class-target = "null"
do_compile[network] = "1"

do_configure:prepend() {
    # Ensure we have the correct Go version
    go_version=$(go version | awk '{print $3}' | sed 's/go//')
    required_version=$(echo ${GO_VERSION} | sed 's/%//')
    if ! $(echo "${go_version} ${required_version}" | awk '{if ($1 >= $2) exit 0; else exit 1}'); then
        bbfatal "Go version must be ${GO_VERSION} or greater. Found ${go_version}."
    fi
}

do_compile() {
    cd ${S}/src/${GO_IMPORT}
    # Ensure promu is in PATH
    export PATH="${STAGING_DIR_NATIVE}${bindir_native}:$PATH"
    
    # Build Prometheus using promu
    promu build --prefix=${B}
}

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${B}/prometheus ${D}${bindir}
    install -m 0755 ${B}/promtool ${D}${bindir}

    install -d ${D}${sysconfdir}/prometheus
    install -m 0644 ${S}/src/${GO_IMPORT}/documentation/examples/prometheus.yml ${D}${sysconfdir}/prometheus/

    install -d ${D}${sysconfdir}/init.d
    install -m 0755 ${WORKDIR}/prometheus.init ${D}${sysconfdir}/init.d/prometheus

    install -d ${D}${sysconfdir}/default
    install -m 0644 ${WORKDIR}/prometheus.default ${D}${sysconfdir}/default/prometheus
}

INITSCRIPT_NAME = "prometheus"
INITSCRIPT_PARAMS = "defaults 85 15"

FILES:${PN} += "${sysconfdir}"
