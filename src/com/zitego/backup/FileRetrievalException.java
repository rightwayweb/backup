package com.zitego.backup;

/**
 * An exception when files are being retrieved.
 *
 * @see FileRetriever
 * @author John Glorioso
 * @version $Id: FileRetrievalException.java,v 1.1.1.1 2008/02/20 15:12:44 jglorioso Exp $
 */
public class FileRetrievalException extends Exception
{
    /**
     * Creates a new file retrieval exception with a message.
     *
     * @param String The message.
     */
    public FileRetrievalException(String msg)
    {
        super(msg);
    }

    /**
     * Creates a new file retrieval exception with a Throwable cause.
     *
     * @param Throwable The throwable cause.
     */
    public FileRetrievalException(Throwable t)
    {
        super(t);
    }
}