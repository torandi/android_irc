#!/bin/bash
dir=$(dirname ${BASH_SOURCE[0]})
java -cp "$dir/bin:$dir/../lib/bin" irc.client.Main
