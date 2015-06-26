package com.zitego.backup;

import com.zitego.util.getopts.*;
import com.zitego.logging.Logger;
import java.util.*;
import java.io.*;

/**
 * This class will handles backing-up specified files on remote server(s).
 * It is a stand alone application that is run with the following options:
 * backup_list - The path of the file that contains a list of property files.<br>
 * properties - The path of a properties file.<br>
 * log_file - The file to log to. If this is left out, then output will go to System.out.<br>
 * <p>
 * The properties file must contain backup instructions, a file retriever,and an archive schedule.
 * See the api documentation for those classes for details on specific options.
 * </p>
 * instruction_[n] - This property specifies an instruction to backup a remote directory. [n]
 *                   Is the number of the instruction. Ex: instruction_3<br>
 * file_retriever - This is the java class that will actually retrieve the files.<br>
 * archive_schedule - This tells the backup manager what to archive when.<br>
 * <br>
 * Example:<br>
 * instruction_0=remote_staging_file=files.tgz,\<br>
 *                 prep_script=/home/httpd/domains/penwrights.com/WEB-INF/bin/prep_backup_files.sh,\<br>
 *                 arg=/home/httpd/domains/penwrights.com/images/files,\<br>
 *                 arg=files.tgz<br>
 * <br>
 * instruction_1=remote_staged_file=photos.tgz,\<br>
 *                 prep_script=/home/httpd/domains/penwrights.com/WEB-INF/bin/prep_backup_files.sh,\<br>
 *                 arg=/home/httpd/domains/penwrights.com/images/photos,\<br>
 *                 arg=photos.tgz<br>
 * <br>
 * instruction_2=remote_staged_file=logs.tgz,\<br>
 *                 prep_script=/home/httpd/domains/penwrights.com/WEB-INF/bin/prep_backup_files.sh,\<br>
 *                 arg=/home/httpd/domains/penwrights.com/images/logs,\<br>
 *                 arg=logs.tgz<br>
 * <br>
 * instruction_3=remote_staged_file=db.sql.gz,\<br>
 *                 prep_script=/home/httpd/domains/penwrights.com/WEB-INF/bin/db_backup.sh<br>
 * <br>
 * file_retriever=class=com.zitego.backup.SSHFileRetriever,\<br>
 *               ssh_cmd=/usr/bin/ssh,\<br>
 *               scp_cmd=/usr/bin/scp,\<br>
 *               prep_script=/home/httpd/domains/penwrights.com/WEB-INF/bin/prep_backup_files.sh,\<br>
 *               user=jglorioso,\<br>
 *               remote_server=zitego.com,\<br>
 *               clean_script=/home/httpd/domains/penwrights.com/WEB-INF/bin/clean_backup_files.sh,\<br>
 *               remote_staging_dir=/home/httpd/domains/bak_staging,\<br>
 *               local_backup_dir=/home/jglorioso/backups<br>
 * <br>
 * database_backup=remote_dump_script=/home/httpd/domains/penwrights.com/WEB-INF/bin/db_dump.sh,\

 * archive_schedule=days_till_purge=5
 *
 * @see FileRetriever
 * @see BackupInstruction
 * @see ArchiveSchedule
 * @author John Glorioso
 * @version $Id: BackupManager.java,v 1.1.1.1 2008/02/20 15:12:44 jglorioso Exp $
 */
public class BackupManager
{
    /** The properties of the backup manager. */
    protected String[] _props;
    /** The instructions for what to backup. */
    protected BackupInstruction[] _instructions;
    /** The archive schedule. */
    protected ArchiveSchedule _schedule;
    /** To handle retrieving files. */
    protected FileRetriever _retriever;
    /** To handle logging progress. */
    protected Logger _logger;

    /**
     * To be called from the command line. A properties file is specified
     * containing backup instructions.
     *
     * @param String[] The arguments.
     */
    public static void main(String[] args)
    {
        String backupPath = null;
        String propsPath = null;
        String logFile = null;
        try
        {
            GetOpts opts = new GetOpts(new String[] { "backup_list::", "properties::", "log_file::" }, args, GetOpts.OPTION_CASE_INSENSITIVE);
            int index;
            while ( (index=opts.getOptions()) != -1 )
            {
                String arg = opts.getOptionString(index);
                String value = opts.getOptarg();
                if ( "log_file".equals(arg) )
                {
                    logFile = value;
                }
                else if ( "backup_list".equals(arg) )
                {
                    backupPath = value;
                }
                else if ( "properties".equals(arg) )
                {
                    propsPath = value;
                }
                else
                {
                    System.out.println("*** WARNING *** Ignoring invalid argument: " + arg);
                }
            }
        }
        catch(Throwable t)
        {
            System.out.println("*** ERROR *** Could not read properties: " + t);
            System.exit(1);
        }

        if (backupPath == null && propsPath == null)
        {
            System.out.println("Usage: java com.zitego.backup.BackupManager [-log_file <log>] [-backup_list <backup list path>] [-properties <properties file>]");
            System.exit(1);
        }

        try
        {
            Vector tmp = new Vector();
            if (propsPath != null) tmp.add(propsPath);

            if (backupPath != null)
            {
                BufferedReader in = new BufferedReader( new FileReader(backupPath) );
                String line = null;
                while ( (line=in.readLine()) != null )
                {
                    tmp.add(line);
                }
            }
            String[] propsFiles = new String[tmp.size()];
            tmp.copyInto(propsFiles);

            BackupManager mgr = new BackupManager(propsFiles);
            mgr.setLogFile(logFile);
            mgr.run();
        }
        catch (Throwable t)
        {
            StringWriter err = new StringWriter();
            t.printStackTrace( new PrintWriter(err) );
            System.out.println("*** ERROR *** Could not execute backup manager:\r\n"+err);
            System.exit(1);
        }
    }

    /**
     * Creates a new backup manager with a properties file path.
     *
     * @param String The properties file path.
     * @throws IOException
     */
    public BackupManager(String propsPath) throws IOException
    {
        this( new String[] { propsPath } );
    }

    /**
     * Creates a new backup manager with an array of properties file paths.
     *
     * @param String[] The properties file paths.
     * @throws IOException
     */
    public BackupManager(String[] propsPaths) throws IOException
    {
        _props = propsPaths;
    }

    /**
     * Sets the log file. If it is null, then it sets to standard out.
     *
     * @param String The path to the log file.
     * @throws IOException if the path is invalid.
     */
    public void setLogFile(String path) throws IOException
    {
        _logger = Logger.getInstance("com.zitego.backup");
        if (path != null) _logger.registerFileHandler("file", path);
        else _logger.registerConsoleHandler("console");
        _logger.log("--------------------------------------------------------------------------------");
        _logger.log("Starting BatchManager");
    }

    /**
     * Creates batch instructions and an archive schedule for the specified
     * properties file index.
     *
     * @param int The index of the properties file to initialize.
     */
    protected void initializeBackup(int index) throws Exception
    {
        _logger.log("");
        _logger.log("Loading backup instructions for "+_props[index]);

        Properties props = new Properties();
        props.load( new FileInputStream(_props[index]) );

        Vector tmp = new Vector();
        int count = 0;
        String prop = null;
        while ( (prop=props.getProperty("instruction_"+count++)) != null )
        {
            tmp.add( new BackupInstruction(prop) );
        }
        _instructions = new BackupInstruction[tmp.size()];
        tmp.copyInto(_instructions);

        _logger.log("Creating FileRetriever");
        createFileRetriever( props.getProperty("file_retriever") );

        _logger.log("Creating Archive Schedule");
        _schedule = new ArchiveSchedule(props.getProperty("archive_schedule"), _instructions, _logger);
    }

    /**
     * Runs the backup instructions to retrieve and archive the specified files.
     *
     * @throws Exception
     */
    public void run() throws Exception
    {
        for (int i=0; i<_props.length; i++)
        {
            initializeBackup(i);
            _logger.log("Retrieving files");
            for (int j=0; j<_instructions.length; j++)
            {
                _retriever.retrieveFile(_instructions[j]);
            }
            _logger.log("Archiving");
            _schedule.archive( _retriever.getLocalBackupDir() );
        }
        _logger.log("Finished BackupManager");
    }

    /**
     * Creates the file retriever based on the comma delimited string of properties.
     *
     * @param String The properties.
     */
    public void createFileRetriever(String props) throws Exception
    {
        String tokens[] = props.split(",");
        if (tokens != null)
        {
            for (int i = 0; i < tokens.length; i++)
            {
                String token[] = tokens[i].split("=");
                if ( token[0].equals("class") )
                {
                    _retriever = (FileRetriever)Class.forName(token[1]).newInstance();
                    _retriever.setLogger(_logger);
                    _retriever.setProperties(props);
                }
               }
           }
    }
}