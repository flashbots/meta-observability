SUMMARY = "Fluent Bit: Fast and Lightweight Log Processor and Forwarder"
DESCRIPTION = "Fluent Bit is a fast Log Processor and Forwarder for Linux, BSD, OSX, and Windows."
HOMEPAGE = "https://fluentbit.io/"
BUGTRACKER = "https://github.com/fluent/fluent-bit/issues"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2ee41112a44fe7014dce33e26468ba93"
SECTION = "net"

SRCREV = "v3.1.6"
SRC_URI = "git://github.com/fluent/fluent-bit.git;branch=master;protocol=https"

PV = "3.1.6"
S = "${WORKDIR}/git"
INSANE_SKIP_${PN}-dev += "dev-elf"

# Use CMake 'Unix Makefiles' generator
OECMAKE_GENERATOR ?= "Unix Makefiles"

DEPENDS = "cmake libyaml openssl zlib bison-native flex-native pkgconfig-native"

inherit cmake

EXTRA_OECMAKE = " \
    -DGNU_HOST=${HOST_SYS} \
    -DFLB_DEBUG=Off \
    -DFLB_TRACE=Off \
    -DFLB_JEMALLOC=Off \
    -DFLB_TLS=On \
    -DFLB_SHARED_LIB=Off \
    -DFLB_EXAMPLES=Off \
    -DFLB_SQLDB=Off \
    -DFLB_HTTP_SERVER=On \
    -DFLB_RECORD_ACCESSOR=On \
    -DFLB_LUAJIT=Off \
    -DFLB_FILTER_LUA=Off \
    -DTEST_WRGSBASE_RESULT=0 \
"

EXTRA_OECMAKE += " --trace"


do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${B}/bin/fluent-bit ${D}${bindir}/fluent-bit

    install -d ${D}${sysconfdir}/fluent-bit/
    cp -r ${S}/conf/* ${D}${sysconfdir}/fluent-bit/

    install -d ${D}${docdir}/fluent-bit/
    cp -r ${S}/docs/* ${D}${docdir}/fluent-bit/
}
