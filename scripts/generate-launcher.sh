#!/bin/bash

VERSION=0.0.1-SNAPSHOT
CACHE_VERSION=v1

coursier bootstrap \
  au.com.agiledigital:toolform_2.12:$VERSION \
  -f -o toolform \
  -M au.com.agiledigital.toolform.app.ToolFormApp \
  "$@"
