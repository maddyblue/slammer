#!/bin/sh
cdrecord speed=10 dev=/dev/cd0c blank=fast
cdrecord speed=10 dev=/dev/cd0c -data install.iso
