#!/bin/bash
dir=$(dirname ${BASH_SOURCE[0]})
java -cp "$dir/server/bin:$dir/external_libs/jerklib.jar:$dir/lib/bin" irc.server.Main
