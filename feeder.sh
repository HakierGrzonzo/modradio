#!/usr/bin/bash
mkdir /tmp/modfiles 2> /dev/null
bash -c "rm -rf /tmp/modfiles/*" 2> /dev/null
for zipped in $(find $@ -iname '*.zip' | shuf); do
    while [ $(find /tmp/modfiles -iname '*.aac' | wc -l) -gt "10" ]; do
        sleep 10
        echo "Waiting for modradio to pickup data" > /dev/stderr
    done
    unzip $zipped -d /tmp/modfiles/ > /dev/null
    file="/tmp/modfiles/$(unzip -l $zipped | awk 'FNR>3 {print $4}' | head -n 1)" 
    ffmpeg -y -i "$file" -b:a 320k "$file.aac" -loglevel error && \
        (echo "$file.aac"; echo "extracted $file" > /dev/stderr) || rm $file
    find /tmp/modfiles/ -type f ! -iname '*.aac' -exec rm {} \;
done
