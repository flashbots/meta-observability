# fluent-bit_git.bb

SUMMARY = "Fast and Lightweight Log processor and forwarder"
DESCRIPTION = "Fluent Bit is a Fast and Lightweight Log processor and forwarder for Linux, BSD and OSX"
HOMEPAGE = "https://fluentbit.io/"
BUGTRACKER = "https://github.com/fluent/fluent-bit/issues"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2ee41112a44fe7014dce33e26468ba93"

PV = "3.0.7"

SRC_URI = "git://github.com/fluent/fluent-bit.git;branch=3.0;protocol=https \
           file://fluent-bit.init \
           file://chunkio-static-lib-fts.patch \
           file://exclude-luajit.patch \
           "

SRCREV = "${AUTOREV}"

S = "${WORKDIR}/git"

DEPENDS = "bison-native flex-native openssl libyaml zlib"

# musl-fts-dev equivalent for musl-based systems
DEPENDS:append:libc-musl = " fts"

inherit cmake pkgconfig update-rc.d

EXTRA_OECMAKE += "\
    -DCMAKE_INSTALL_PREFIX=/usr \
    -DCMAKE_INSTALL_LIBDIR=lib \
    -DFLB_ALL=No \
    -DFLB_TLS=Yes \
    -DFLB_SHARED_LIB=Yes \
    -DFLB_EXAMPLES=No \
    -DFLB_HTTP_SERVER=Yes \
    -DFLB_IN_SYSTEMD=No \
    -DFLB_OUT_KAFKA=No \
    -DFLB_LUAJIT=No \
    -DFLB_WASM=No \
    -DFLB_STREAM_PROCESSOR=No \
    -DFLB_TESTS_RUNTIME=No \
    -DFLB_TESTS_INTERNAL=No \
    -DFLB_TESTS=No \
    -DFLB_IN_CPU=Yes \
    -DFLB_IN_MEM=Yes \
    -DFLB_IN_TAIL=Yes \
    -DFLB_OUT_STDOUT=Yes \
    -DFLB_OUT_FILE=Yes \
    -DFLB_OUT_HTTP=Yes \
    -DFLB_OUT_PROMETHEUS_EXPORTER=Yes \
    -DFLB_SQLDB=No \
    -DFLB_OUT_KAFKA_REST=No \
    -DFLB_IN_SERIAL=No \
    "

do_install:append() {
    install -d ${D}${sysconfdir}/init.d
    install -m 0755 ${WORKDIR}/fluent-bit.init ${D}${sysconfdir}/init.d/fluent-bit
    
    # Move configuration files to /etc
    install -d ${D}${sysconfdir}/fluent-bit
    if [ -d ${D}/usr/etc/fluent-bit ]; then
        mv ${D}/usr/etc/fluent-bit/* ${D}${sysconfdir}/fluent-bit/
        rm -rf ${D}/usr/etc
    fi
}

INITSCRIPT_NAME = "fluent-bit"
INITSCRIPT_PARAMS = "defaults"

FILES:${PN} += "${sysconfdir}"

RDEPENDS:${PN} += "openssl libyaml"

INSANE_SKIP:${PN} += "already-stripped"
