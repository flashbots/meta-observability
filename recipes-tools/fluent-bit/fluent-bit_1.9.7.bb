SUMMARY = "Fast Log processor and Forwarder"
DESCRIPTION = "Fluent Bit is a data collector, processor and  \
forwarder for Linux. It supports several input sources and \
backends (destinations) for your data. \
"

HOMEPAGE = "http://fluentbit.io"
BUGTRACKER = "https://github.com/fluent/fluent-bit/issues"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2ee41112a44fe7014dce33e26468ba93"
SECTION = "net"

SRC_URI = "https://releases.fluentbit.io/1.9/source-${PV}.tar.gz;subdir=fluent-bit-${PV};downloadfilename=${BPN}-${PV}.tar.gz \
           file://0001-CMakeLists.txt-Do-not-use-private-makefile-target.patch \
           file://0002-flb_info.h.in-Do-not-hardcode-compilation-directorie.patch \
           file://0003-mbedtls-Do-not-overwrite-CFLAGS.patch \
           file://0004-build-Make-systemd-init-systemd-detection-contingent.patch \
           file://0001-monkey-Define-_GNU_SOURCE-for-memmem-API-check.patch \
           file://0002-mbedtls-Remove-unused-variable.patch \
           file://0003-mbedtls-Disable-documentation-warning-as-error-with-.patch \
           file://0004-Use-correct-type-to-store-return-from-flb_kv_item_cr.patch \
           file://0005-stackdriver-Fix-return-type-mismatch.patch \
           file://0006-monkey-Fix-TLS-detection-testcase.patch \
           file://0007-cmake-Do-not-check-for-upstart-on-build-host.patch \
           file://0008-cri-parsers.patch \
           file://fluent-bit.init \
           file://td-agent-bit.conf.mustache \
           file://aws-auth.mustache \
           "
SRC_URI:remove:x86 = "file://0002-mbedtls-Remove-unused-variable.patch"
SRC_URI:append:libc-musl = "\
           file://0001-Use-posix-strerror_r-with-musl.patch \
           file://0002-chunkio-Link-with-fts-library-with-musl.patch \
           "
SRC_URI[sha256sum] = "8ca2ac081d7eee717483c06608adcb5e3d5373e182ad87dba21a23f8278c6540"
S = "${WORKDIR}/fluent-bit-${PV}"

DEPENDS = "zlib bison-native flex-native openssl"

PACKAGECONFIG[yaml] = "-DFLB_CONFIG_YAML=On,-DFLB_CONFIG_YAML=Off,libyaml"
PACKAGECONFIG[kafka] = "-DFLB_OUT_KAFKA=On,-DFLB_OUT_KAFKA=Off,librdkafka"
PACKAGECONFIG[examples] = "-DFLB_EXAMPLES=On,-DFLB_EXAMPLES=Off"
PACKAGECONFIG[jemalloc] = "-DFLB_JEMALLOC=On,-DFLB_JEMALLOC=Off,jemalloc"
#TODO add more fluentbit options to PACKAGECONFIG[]

DEPENDS:append:libc-musl = " fts "

# flex hardcodes the input file in #line directives leading to TMPDIR contamination of debug sources.
do_compile:append() {
    find ${B} -name '*.c' -or -name '*.h' | xargs sed -i -e 's|${TMPDIR}|${TARGET_DBGSRC_DIR}/|g'
}

PACKAGECONFIG ?= "yaml"

LTO = ""

# Use CMake 'Unix Makefiles' generator
OECMAKE_GENERATOR ?= "Unix Makefiles"

# Fluent Bit build options
# ========================

# Host related setup
EXTRA_OECMAKE += "-DGNU_HOST=${HOST_SYS} -DFLB_TD=1"

# Disable LuaJIT and filter_lua support
EXTRA_OECMAKE += "-DFLB_LUAJIT=Off -DFLB_FILTER_LUA=Off "

# Disable Library and examples
EXTRA_OECMAKE += "-DFLB_SHARED_LIB=Off"

# Enable release builds
EXTRA_OECMAKE += "-DFLB_RELEASE=On"

# musl needs these options
EXTRA_OECMAKE:append:libc-musl = ' -DFLB_JEMALLOC_OPTIONS="--with-jemalloc-prefix=je_ --with-lg-quantum=3" -DFLB_CORO_STACK_SIZE=24576'

EXTRA_OECMAKE:append:riscv64 = " -DCMAKE_C_STANDARD_LIBRARIES=-latomic"
EXTRA_OECMAKE:append:riscv32 = " -DCMAKE_C_STANDARD_LIBRARIES=-latomic"
EXTRA_OECMAKE:append:mips = " -DCMAKE_C_STANDARD_LIBRARIES=-latomic"
EXTRA_OECMAKE:append:powerpc = " -DCMAKE_C_STANDARD_LIBRARIES=-latomic"
EXTRA_OECMAKE:append:x86 = " -DCMAKE_C_STANDARD_LIBRARIES=-latomic"

CFLAGS:append:x86 = " -DMBEDTLS_HAVE_SSE2"

inherit cmake update-rc.d useradd pkgconfig

#USERADD_PACKAGES = "${PN}"
#GROUPADD_PARAM:${PN} = "-r fluentbit"
#USERADD_PARAM:${PN} = "-r -g fluentbit -s /sbin/nologin -d /nonexistent fluentbit"
USERADD_PACKAGES = "${PN}"
GROUPADD_PARAM:${PN} = "-r fluentbit"
USERADD_PARAM:${PN} = "-r -g fluentbit -d /var/lib/td-agent-bit -s /sbin/nologin -c 'Fluent Bit' fluentbit"

EXTRA_OECMAKE += "-DCMAKE_DEBUG_SRCDIR=${TARGET_DBGSRC_DIR}/"
TARGET_CC_ARCH += " ${SELECTED_OPTIMIZATION}"

do_install:append() {
    install -d ${D}${sysconfdir}/init.d
    install -m 0755 ${WORKDIR}/fluent-bit.init ${D}${sysconfdir}/init.d/td-agent-bit

    # Move configuration files to /etc
    install -d ${D}${sysconfdir}/td-agent-bit
    if [ -d ${D}/usr/etc/td-agent-bit ]; then
        mv ${D}/usr/etc/td-agent-bit/* ${D}${sysconfdir}/td-agent-bit/
        rm -rf ${D}/usr/etc
    fi
    install -m 0644 ${WORKDIR}/td-agent-bit.conf.mustache ${D}${sysconfdir}/td-agent-bit/
    install -m 0600 ${WORKDIR}/aws-auth.mustache ${D}${sysconfdir}/td-agent-bit/

    install -d ${D}/var/lib/td-agent-bit
    # Set correct ownership and permissions
    chown -R fluentbit:fluentbit ${D}/var/lib/td-agent-bit
    chown -R fluentbit:fluentbit ${D}${sysconfdir}/td-agent-bit
}

pkg_postinst_ontarget:${PN}() {
    # Ensure the log file can be created by the fluentbit user
    touch /tmp/td-agent-bit.log
    chown fluentbit:fluentbit /tmp/td-agent-bit.log
}

FILES:${PN} += "${sysconfdir}"

INITSCRIPT_NAME = "td-agent-bit"
INITSCRIPT_PARAMS = "defaults 92"
