#!/usr/bin/bash
mkdir /tmp/modfiles 2> /dev/null
bash -c "rm -r /tmp/modfiles/*" 2> /dev/null
for zipped in $(find $@ -iname '*.zip' | shuf); do
    unzip $zipped -d /tmp/modfiles/ > /dev/null
    file="/tmp/modfiles/$(ls /tmp/modfiles | head -n 1)" 
    ffmpeg -i "$file" -b:a 320k "$file.aac" 2>/dev/null && \
        echo "$file.aac" || rm $file
done
