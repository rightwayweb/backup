#!/bin/sh

JAVA=/usr/local/java/bin/java
HOME=/home/jglorioso/zitego/backup
CP=$HOME/dist/zitego_backup_1.1.jar:$HOME/lib/zitego_common_1.0.6.jar:$HOME/lib/zitego_filemanager_1.1.jar:$HOME/lib/zitego_markup_1.2.2.jar

$JAVA -classpath $CP com.zitego.backup.BackupManager -log_file $HOME/logs/backup.log -backup_list $1
