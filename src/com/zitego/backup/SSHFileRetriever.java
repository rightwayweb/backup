package com.zitego.backup;

import com.zitego.markup.xml.XmlTag;
import java.io.File;

/**
 * Retrieves files using secure copy and ssh. The required properties to
 * specify are ssh_cmd, scp_cmd, prep_script, clean_script, user, and
 * remote_server. The meaning of each of these is as follows:<br>
 * class - The java class to instantiate.<br>
 * ssh_cmd - The ssh command path.<br>
 * scp_cmd - The scp command path.<br>
 * clean_script - The remote script that will clean out the remote staging directory.<br>
 * user - The user to ssh and scp as.<br>
 * remote_server - The remote server to ssh and scp to.<br>
 * remote_staging_dir - The remote directory to retrieve files from.<br>
 * local_backup_dir - The local directory to copy to.<br>
 * <br>
 * Example:<br>
 * class=com.zitego.backup.SSHFileRetriever,\<br>
 * ssh_cmd*=/usr/bin/ssh,\<br>
 * scp_cmd=/usr/bin/scp,\<br>
 * user=jglorioso,\<br>
 * remote_server=zitego.com,\<br>
 * clean_script*=/home/httpd/domains/penwrights.com/WEB-INF/bin/clean_backup_files.sh,\<br>
 * remote_staging_dir=/home/httpd/domains/bak_staging,\<br>
 * local_backup_dir=/home/jglorioso/backups<br>
 * <br>
 * * Remote scripts must echo "----- EOF -----"
 *
 * @author John Glorioso
 * @version $Id: SSHFileRetriever.java,v 1.1.1.1 2008/02/20 15:12:44 jglorioso Exp $
 */
public class SSHFileRetriever extends FileRetriever
{
    /** The ssh command. */
    protected String _ssh;
    /** The scp command. */
    protected String _scp;
    /** The clean script to run. */
    protected String _cleanScript;
    /** The user to connect as. */
    protected String _user;
    /** The remote server. */
    protected String _remoteServer;

    public void setProperties(String props)
    {
        String tokens[] = props.split(",");
        if (tokens != null)
        {
            for (int i = 0; i < tokens.length; i++)
            {
                String token[] = tokens[i].split("=");
                if ( token[0].equals("ssh_cmd") ) _ssh = token[1];
                else if (token[0].equals("scp_cmd") ) _scp = token[1];
                else if (token[0].equals("clean_script") ) _cleanScript = token[1];
                else if (token[0].equals("user") ) _user = token[1];
                else if (token[0].equals("remote_server") ) _remoteServer = token[1];
                else if (token[0].equals("remote_staging_dir") ) setRemoteStagingDir(token[1]);
                else if (token[0].equals("local_backup_dir") ) setLocalBackupDir(token[1]);
            }
        }
        log("ssh_cmd="+_ssh);
        log("scp_cmd="+_scp);
        log("clean_script="+_cleanScript);
        log("user="+_user);
        log("remoteServer="+_remoteServer);
        log("remote_staging_dir="+_remoteStagingDir);
        log("local_backup_dir="+_localBackupDir);
    }

    public void setPropertiesFromXml(XmlTag tag)
    {
        String val = tag.getChildValue("ssh_cmd");
        if (val != null) _ssh = val;
        val = tag.getChildValue("scp_cmd");
        if (val != null) _scp = val;
        val = tag.getChildValue("clean_script");
        if (val != null) _cleanScript = val;
        val = tag.getChildValue("user");
        if (val != null) _user = val;
        val = tag.getChildValue("remote_server");
        if (val != null) _remoteServer = val;
        val = tag.getChildValue("remote_staging_dir");
        if (val != null) _remoteStagingDir = val;
        val = tag.getChildValue("local_backup_dir");
        if (val != null) _localBackupDir = val;
        log("ssh_cmd="+_ssh);
        log("scp_cmd="+_scp);
        log("clean_script="+_cleanScript);
        log("user="+_user);
        log("remoteServer="+_remoteServer);
        log("remote_staging_dir="+_remoteStagingDir);
        log("local_backup_dir="+_localBackupDir);
    }

    public void retrieveFile(BackupInstruction instr) throws FileRetrievalException
    {
        log(instr.remoteStagedFile);
        File localDir = new File( getLocalBackupDir() );
        if ( !localDir.exists() )
        {
            if ( !localDir.mkdir() )
            {
                throw new FileRetrievalException("Could not retrieve remote file: "+instr);
            }
        }

        String connectString = _user+"@"+_remoteServer;
        try
        {
            if (instr.prepScript != null)
            {
                //Run the prepare script
                if (instr.args == null) instr.args = new String[0];
                String[] cmdAndArgs = new String[instr.args.length+3];
                cmdAndArgs[0] = _ssh;
                cmdAndArgs[1] = connectString;
                cmdAndArgs[2] = instr.prepScript;
                for (int i=0; i<instr.args.length; i++)
                {
                    cmdAndArgs[i+3] = instr.args[i];
                }
                execute(cmdAndArgs);
            }
            //Copy the file locally
            execute
            (
                new String[]
                {
                    _scp, connectString+":" + (getRemoteStagingDir() != null ? getRemoteStagingDir() + "/" : "") + instr.remoteStagedFile,
                    getLocalBackupDir() + "/" + getDatedFilename(instr.remoteStagedFile)
                }
            );
        }
        finally
        {
            //Clean the remote staging dir
            if (_cleanScript != null)
            {
                execute
                (
                    new String[]
                    {
                        _ssh, connectString, _cleanScript, instr.remoteStagedFile
                    }
                );
            }
        }
    }

    /**
     * Sets the ssh command.
     *
     * @param ssh The ssh command.
     */
    public void setSshCommand(String ssh)
    {
        _ssh = ssh;
    }

    /**
     * Returns the ssh command.
     *
     * @return String
     */
    public String getSshCommand()
    {
        return _ssh;
    }

    /**
     * Sets the scp command.
     *
     * @param scp The scp command.
     */
    public void setScpCommand(String scp)
    {
        _scp = scp;
    }

    /**
     * Returns the scp command.
     *
     * @return String
     */
    public String getScpCommand()
    {
        return _scp;
    }

    /**
     * Sets the clean script.
     *
     * @param script The clean script.
     */
    public void setCleanScript(String script)
    {
        _cleanScript = script;
    }

    /**
     * Returns the clean script.
     *
     * @return String
     */
    public String getCleanScript()
    {
        return _cleanScript;
    }

    /**
     * Sets the user.
     *
     * @param user The user.
     */
    public void setUser(String user)
    {
        _user = user;
    }

    /**
     * Returns the user.
     *
     * @return String
     */
    public String getUser()
    {
        return _user;
    }

    /**
     * Sets the remote server.
     *
     * @param remote The remote server.
     */
    public void setRemoteServer(String remote)
    {
        _remoteServer = remote;
    }

    /**
     * Returns the remote server.
     *
     * @return String
     */
    public String getRemoteServer()
    {
        return _remoteServer;
    }
}