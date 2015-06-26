#!/bin/sh

FROM_HOME=$1
TO_HOME=/home/httpd/domains/bak_staging
FILE=$2

cd $FROM_HOME
/bin/tar -czvf $TO_HOME/$FILE *
echo '----- EOF -----'
