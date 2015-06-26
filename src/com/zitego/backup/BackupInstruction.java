package com.zitego.backup;

import java.util.Vector;

/**
 * Stores the properties of each instruction. The property string must include
 * remote_staged_file, prep_script, and args.<br>
 * Example:<br>
 * remote_staged_file=photos.tgz,\<br>
 * prep_script=/home/httpd/domains/penwrights.com/WEB-INF/bin/prep_backup_files.sh,\<br>
 * arg=/home/httpd/domains/penwrights.com/images/photos,\<br>
 * arg=photos.tgz
 *
 * @author John Glorioso
 * @version $Id: BackupInstruction.java,v 1.1.1.1 2008/02/20 15:12:44 jglorioso Exp $
 */
public class BackupInstruction
{
    /** The name of the staged file to retrieve. */
    public String remoteStagedFile;
    /** The prep script to run. */
    public String prepScript;
    /** The acript arguments. */
    public String[] args;

    /**
     * Creates a backup instruction with a remote staged file, a prep script (can
     * be null), and prep script arguments (can be null).
     *
     * @param file The remote file to retrieve.
     * @param script The prep script.
     * @param args The prep script args.
     */
    public BackupInstruction(String file, String script, String[] args)
    {
        this.remoteStagedFile = file;
        this.prepScript = script;
        this.args = args;
    }

    /**
     * Creates a new backup instruction by parsing the properties
     * String that comes in.
     *
     * @param props The properties.
     */
    public BackupInstruction(String props)
    {
        String tokens[] = props.split(",");
        if (tokens != null)
        {
            Vector tmp = new Vector();
            for (int i=0; i<tokens.length; i++)
            {
                String token[] = tokens[i].split("=");
                if ( token[0].equals("remote_staged_file") ) remoteStagedFile = token[1];
                else if (token[0].equals("prep_script") ) prepScript = token[1];
                else if (token[0].equals("arg") ) tmp.add(token[1]);
               }
               args = new String[tmp.size()];
               tmp.copyInto(args);
           }
    }

    public String toString()
    {
        StringBuffer ret = new StringBuffer()
            .append("[com.zitego.BackupInstruction:")
            .append( " remoteStagedFile=").append(remoteStagedFile)
            .append( " prepScript=").append(prepScript)
            .append( " args:");
        for (int i=0; i<args.length; i++)
        {
            ret.append(" ").append(args[i]);
        }
        ret.append("]");
        return ret.toString();
    }
}