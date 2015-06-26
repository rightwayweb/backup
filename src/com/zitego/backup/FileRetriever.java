package com.zitego.backup;

import com.zitego.logging.Logger;
import com.zitego.markup.xml.XmlTag;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * This is an abstract class that defines how to retrieve
 * files from a remote server. Properties are set specific to
 * the child classes. See documentation in those classes for
 * details.
 *
 * @author John Glorioso
 * @version $Id: FileRetriever.java,v 1.1.1.1 2008/02/20 15:12:44 jglorioso Exp $
 */
public abstract class FileRetriever
{
    /** The remote staging directory to retrieve files from. */
    protected String _remoteStagingDir;
    /** The local directory to backup to. */
    protected String _localBackupDir;
    /** The log writer. */
    protected Logger _logger;
    /** The dated file format. */
    protected SimpleDateFormat _fileFormat = new SimpleDateFormat("MMddyyyy");

    /**
     * Sets the properties for the file retriever.
     *
     * @param props A comma delimited properties string.
     */
    public abstract void setProperties(String props);

    /**
     * Sets the properties for the file retriever given an XmlTag object. The tag is expected to have
     * xml tag children for each of the allowable properties.
     *
     * @param xml The xml tag.
     */
    public abstract void setPropertiesFromXml(XmlTag tag);

    /**
     * Retrieves the file specified by the batch instruction.
     *
     * @param instr The instruction about the file.
     * @throws FileRetrievalException
     */
    public abstract void retrieveFile(BackupInstruction instr) throws FileRetrievalException;

    /**
     * Sets the log writer.
     *
     * @param logger The writer.
     */
    public void setLogger(Logger logger)
    {
        _logger = logger;
    }

    /**
     * Logs the specified message.
     *
     * @param msg The message.
     */
    public void log(String msg)
    {
        if (_logger != null) _logger.log(msg);
    }

    /**
     * Executes the command and arguments.
     *
     * @param args The command and arguments.
     * @throws FileRetrievalException
     */
    public void execute(String[] args) throws FileRetrievalException
    {
        FileRetrievalException fre = null;
        try
        {
            StringBuffer cmd = new StringBuffer();
            for (int i=0; i<args.length; i++)
            {
                cmd.append( (i>0?" ":"") ).append(args[i]);
            }
            log( cmd.toString() );
            Process proc = Runtime.getRuntime().exec(args);
            BufferedReader procOut = new BufferedReader( new InputStreamReader(proc.getInputStream()) );
            String line = null;
            StringBuffer msg = new StringBuffer();
            while ( (line=procOut.readLine()) != null )
            {
                if (line == "----- EOF -----") break;
            }
            BufferedReader err = new BufferedReader( new InputStreamReader(proc.getErrorStream()) );
            line = null;
            msg = new StringBuffer();
            while ( (line=err.readLine()) != null )
            {
                msg.append(line).append(" ");
            }
            if (msg.length() > 0)
            {
                log("*** ERROR *** "+msg);
                fre = new FileRetrievalException( msg.toString() );
            }
        }
        catch (Exception e)
        {
            fre = new FileRetrievalException(e);
        }
        if (fre != null) throw fre;
    }

    /**
     * Returns the dated file name based on the one passed in. If the file name ends in a wild
     * card (*) then an empty string is returned.
     *
     * @param f The filename.
     * @return String
     */
    public String getDatedFilename(String f)
    {
        if ( !f.endsWith("*") )
        {
            int index = f.indexOf(".");
            String name = f.substring(0, index);
            String ext = f.substring(index);
            StringBuffer ret = new StringBuffer()
                .append(name).append("_").append( _fileFormat.format(new Date()) ).append(ext);
            return ret.toString();
        }
        else
        {
            return "";
        }
    }

    /**
     * Sets the remote staging directory to retrieve files from.
     *
     * @param dir The remote staging directory.
     */
    public void setRemoteStagingDir(String dir)
    {
        _remoteStagingDir = dir;
    }

    /**
     * Returns the remote staging directory to retrieve files from.
     *
     * @return String
     */
    public String getRemoteStagingDir()
    {
        return _remoteStagingDir;
    }

    /**
     * Sets the local directory to backup to.
     *
     * @param dir The directory.
     */
    public void setLocalBackupDir(String dir)
    {
        _localBackupDir = dir;
    }

    /**
     * Returns the local directory to backup to.
     *
     * @return String
     */
    public String getLocalBackupDir()
    {
        return _localBackupDir;
    }
}