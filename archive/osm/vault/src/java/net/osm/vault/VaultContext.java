/*
 * File: VaultContext.java
 * License: etc/LICENSE.TXT
 * Copyright: OSM SARL 2002, All Rights Reserved.
 */

package net.osm.vault;

import java.io.File;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;

/**
 * <code>VaultContext</code> context collects together the applications
 * context for vault management.  This includes the path to the keystore,
 * keystore alias and keystore challenge.
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */
public interface VaultContext extends Context
{
   /**
    * Context key for the keystore alias.
    * @see #getAlias()
    */
    public static final String ALIAS_KEY = "ALIAS";

   /**
    * Context key for the keystore challenge.
    * @see #getChallenge()
    */
    public static final String CHALLENGE_KEY = "CHALLENGE";

   /**
    * Context key for the keystore location.
    * @see #getKeystorePath()
    */
    public static final String KEYSTORE_KEY = "KEYSTORE";

   /**
    * Returns the command-line argument <code>String[]</code>.
    * @return String[] command line arguments
    */
    public File getKeystorePath();

   /**
    * Returns the keystore alias.
    * @return String the keystore alias
    */
    public String getAlias();

   /**
    * Returns the keystore challenge.
    * @return String the keystore challenge
    */
    public String getChallenge();
}
