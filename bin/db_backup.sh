#!/bin/sh

BAK_HOME=/home/httpd/domains/bak_staging
mysqldump --opt -u |USER| -p|PASSWORD| |DB_NAME|> $BAK_HOME/db.sql
/bin/gzip $BAK_HOME/db.sql
echo '----- EOF -----'
