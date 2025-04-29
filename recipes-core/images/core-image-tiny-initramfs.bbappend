include ${@bb.utils.contains('DISTRO_FEATURES', 'observability', 'core-image-tiny-initramfs.inc', '', d)}
