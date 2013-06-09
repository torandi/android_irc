#!/bin/bash
dir=$(dirname ${BASH_SOURCE[0]})
java -cp "$dir/bin:$dir/../external_libs/mysql-connector-java-5.1.25-bin.jar:$dir/../external_libs/JerkLib-.7r811/jerklib.jar:$dir/../lib/bin" com.torandi.irc.server.Main


