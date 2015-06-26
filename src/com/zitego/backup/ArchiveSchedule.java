package com.zitego.backup;

import java.io.File;
import com.zitego.filemanager.util.WildcardFilter;
import com.zitego.logging.Logger;

/**
 * Handles archiving the retrieved files. The required properties that must
 * be specified are days_till_purge. This tells how old a file should be when
 * it is purged.<br>
 * Example:<br>
 * days_till_purge=5
 *
 * @author John Glorioso
 * @version $Id: ArchiveSchedule.java,v 1.1.1.1 2008/02/20 15:12:44 jglorioso Exp $
 */
public class ArchiveSchedule
{
    /** How old files can (in days) get before they are purged. */
    protected int _daysTillPurge = 0;
    /** The backup instructions. */
    protected BackupInstruction[] _instructions;
    /** To log what is being being purged. */
    protected Logger _logger;

    /**
     * Creates a new archive schedule with the specified properties and
     * a set of backup instructions.
     *
     * @param String The properties.
     * @param BackupInstruction[] The instructions.
     * @param Logger The logger.
     */
    public ArchiveSchedule(String props, BackupInstruction[] instr, Logger logger)
    {
        String tokens[] = props.split(",");
        if (tokens != null)
        {
            for (int i = 0; i < tokens.length; i++)
            {
                String token[] = tokens[i].split("=");
                if ( token[0].equals("days_till_purge") ) _daysTillPurge = Integer.parseInt(token[1]);
               }
           }
           _instructions = instr;
           _logger = logger;
           _logger.log("days_till_purge="+_daysTillPurge);
    }

    /**
     * Archives the files that were retrieved by the back up instructions
     * by seeing if any historical files are older then the specified number
     * of days till purging.
     *
     * @param String The local backup directory.
     */
    public void archive(String localBackupDir)
    {
        for (int i=0; i<_instructions.length; i++)
        {
            File f = new File(localBackupDir);
            String pattern = _instructions[i].remoteStagedFile.substring( 0, _instructions[i].remoteStagedFile.indexOf(".") );
            File[] files = f.listFiles
            (
                new WildcardFilter(pattern+"*", false)
            );
            long curTime = System.currentTimeMillis();
            for (int j=0; j<files.length; j++)
            {
                long fileAge = ( curTime-files[j].lastModified() )/24L/60L/60L/1000L;
                if (fileAge > _daysTillPurge)
                {
                    files[j].delete();
                    _logger.log("Purging "+files[j]);
                }
            }
        }
    }
}