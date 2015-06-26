RELEASE NOTES:

Dependencies: common, filemanager

1.0a   - Changed to use the new common jar and the base file manager jar.

1.0    - Initial version

###############################################################################
# Instructions for setting up automated ssh keys from the machine that backup
# will run on to the machine that it is backing up.
#
# @author John Glorioso
# @version $Id: README.txt,v 1.1.1.1 2008/02/20 15:12:44 jglorioso Exp $
###############################################################################

Setup of SSH Keys for SSHFileRetriever:

1. From machine that backup will run on ssh into the destination box.
   Ex: ssh jglorioso@zitego.com

   This will create a .ssh directory in your home directory if it does not
   yet exist and put a known_hosts2 file there. If it is not known_hosts2,
   you need to make sure the machine uses ssh2 before continuing.

2. Type "ssh-keygen -t dsa"

3. When it asks for the file to save the key in, accept the default of
   .ssh/id_dsa

4. Hit enter when asked for a passphrase. It will ask a second time to
   verify it, so just hit enter again.

5. Copy the contents of the .ssh/id_dsa.pub file to the remote machine's
   .ssh directory for the user you are logging in as. For example:
   scp .ssh/id_dsa.pub jglorioso@zitego.com:.ssh/authorized_keys2


That's it! You should now be able to freely ssh and scp from the primary
machine to the host machine.
