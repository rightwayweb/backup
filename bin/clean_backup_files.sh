#!/bin/sh

DIR=/home/httpd/domains/bak_staging/
FILE=$1

cd $DIR
rm -f $DIR/$FILE
echo '----- EOF -----'
