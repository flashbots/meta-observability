SUMMARY = "Promu is the Prometheus Utility Tool"
DESCRIPTION = "Promu is used to build and release Prometheus projects"
HOMEPAGE = "https://github.com/prometheus/promu"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://src/${GO_WORKDIR}/LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

inherit go-mod
inherit native

GO_IMPORT = "github.com/prometheus/promu"

SRC_URI = "git://${GO_IMPORT};branch=master;protocol=https"
SRCREV = "v0.17.0"
PV = "0.17.0"

# Specify required Go version
GO_VERSION ?= "1.22%"

RDEPENDS:${PN}-dev += "bash"
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
    # Initialize Go module if go.mod doesn't exist
    if [ ! -f go.mod ]; then
        go mod init ${GO_IMPORT}
    fi
    # Ensure all dependencies are downloaded
    go mod tidy
    go mod download
    # Build promu
    GO111MODULE=on CGO_ENABLED=0 go build -o promu
}

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${S}/src/${GO_IMPORT}/promu ${D}${bindir}
}
