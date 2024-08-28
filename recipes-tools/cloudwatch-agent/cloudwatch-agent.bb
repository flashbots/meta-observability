SUMMARY = "AWS CloudWatch Agent"
DESCRIPTION = "The CloudWatch Agent enables you to collect metrics and logs from Amazon EC2 instances and on-premises servers."
HOMEPAGE = "https://github.com/aws/amazon-cloudwatch-agent"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://src/${GO_WORKDIR}/LICENSE;md5=4dde6168ca1ce801034ffe20cabf2b37"

DEPENDS = "go-native"
inherit go-mod useradd

PV = "1.300043.0"
SRCREV = "v1.300043.0"

SRC_URI = "git://github.com/aws/amazon-cloudwatch-agent.git;protocol=https;nobranch=1 \
           file://cloudwatch-agent.init \
           file://cloudwatch-agent.json"

S = "${WORKDIR}/git"

GO_LINKSHARED = ""
do_compile[network] = "1"

USERADD_PACKAGES = "${PN}"
GROUPADD_PARAM:${PN} = "-r cwagent"
USERADD_PARAM:${PN} = "-r -g cwagent -d /var/lib/cwagent -s /sbin/nologin -c 'CloudWatch Agent' cwagent"

# reproducible builds
INHIBIT_PACKAGE_DEBUG_SPLIT = '1'
INHIBIT_PACKAGE_STRIP = '1'

# Set Go import path
GO_IMPORT = "github.com/aws/amazon-cloudwatch-agent"

do_compile() {
    bbwarn "Starting compile task"
    cd ${S}
    bbwarn "Current directory: $(pwd)"
    bbwarn "Directory contents: $(ls -la)"
    
    # Set GOPATH to include the source directory
    export GOPATH="${B}/gopath"
    mkdir -p ${GOPATH}/src/$(dirname ${GO_IMPORT})
    ln -sf ${S} ${GOPATH}/src/${GO_IMPORT}
    export GO111MODULE=on
    cd ${GOPATH}/src/${GO_IMPORT}
    bbwarn "Compiling in directory: $(pwd)"
    bbwarn "Directory contents: $(ls -la)"
    oe_runmake release
}

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${B}/gopath/src/${GO_IMPORT}/build/bin/linux/amd64/amazon-cloudwatch-agent ${D}${bindir}/amazon-cloudwatch-agent
    install -m 0755 ${B}/gopath/src/${GO_IMPORT}/build/bin/linux/amd64/amazon-cloudwatch-agent-ctl ${D}${bindir}/amazon-cloudwatch-agent-ctl
    install -m 0755 ${B}/gopath/src/${GO_IMPORT}/build/bin/linux/amd64/start-amazon-cloudwatch-agent ${D}${bindir}/start-amazon-cloudwatch-agent

    install -d ${D}${sysconfdir}/amazon/amazon-cloudwatch-agent
    install -m 0644 ${WORKDIR}/cloudwatch-agent.json ${D}${sysconfdir}/amazon/amazon-cloudwatch-agent/

    install -d ${D}${sysconfdir}/init.d
    install -m 0755 ${WORKDIR}/cloudwatch-agent.init ${D}${sysconfdir}/init.d/cloudwatch-agent

    # Create directory for CloudWatch Agent data
    install -d ${D}/var/lib/cwagent
    chown cwagent:cwagent ${D}/var/lib/cwagent

    # Set correct ownership for config directory
    chown -R cwagent:cwagent ${D}${sysconfdir}/amazon/amazon-cloudwatch-agent
}

pkg_postinst_ontarget:${PN}() {
    # Ensure the log file can be created by the cwagent user
    touch /tmp/amazon-cloudwatch-agent.log
    chown cwagent:cwagent /tmp/amazon-cloudwatch-agent.log
}

inherit update-rc.d

INITSCRIPT_NAME = "cloudwatch-agent"
INITSCRIPT_PARAMS = "defaults 95 15"

FILES:${PN} += "${sysconfdir}/amazon /var/lib/cwagent"

RDEPENDS:${PN} += "openssl"
