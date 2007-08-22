/*
 * File: DefaultVaultContext.java
 * License: etc/LICENSE.TXT
 * Copyright: Copyright (C) The Apache Software Foundation. All rights reserved.
 * Copyright: OSM SARL 2002, All Rights Reserved.
 */

package net.osm.vault;

import java.io.File;
import java.util.Map;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.CascadingRuntimeException;

/**
 * <code>DefaultVaultContext</code> context collects together the applications
 * context fopr valut management.  This includes the path to the keystore,
 * keystore alias and keystore challenge.
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */
public final class DefaultVaultContext extends DefaultContext implements VaultContext
{

   /**
    * Creation of a empty <code>DefaultVaultContext</code> with a supplied parent
    * context.
    * @param parent a partent context
    */
    public DefaultVaultContext( Map data, Context parent )
    {
        super( data, parent );
    }

   /**
    * Creation of a new <code>DefaultVaultContext</code> with the supplied 
    * arguments.
    * @param parent a partent context
    * @param keystore the keystore file
    * @param alias keystore alias
    * @param challenge the keystore challenge
    */
    public DefaultVaultContext( Context parent, final File keystore, final String alias, final String challenge )
    {
        super( parent );
        super.put( VaultContext.KEYSTORE_KEY, keystore );
        super.put( VaultContext.ALIAS_KEY, alias );
        super.put( VaultContext.CHALLENGE_KEY, challenge );
    }

   /**
    * Returns the command-line argument <code>String[]</code>.
    * @return String[] command line arguments
    */
    public File getKeystorePath()
    {
        try
        {
            return (File) super.get( VaultContext.KEYSTORE_KEY );
        }
        catch( ContextException e )
        {

            final String error = "Unexpected exception while retrieving keystore path.";
            throw new CascadingRuntimeException( error, e );
        }
    }

   /**
    * Returns the keystore alias.
    * @return String the keystore alias
    */
    public String getAlias()
    {
        try
        {
            return (String) super.get( VaultContext.ALIAS_KEY );
        }
        catch( ContextException e )
        {
            final String error = "Unexpected exception while retrieving alias.";
            throw new CascadingRuntimeException( error, e );
        }
    }

   /**
    * Returns the keystore challenge.
    * @return String the keystore challenge
    */
    public String getChallenge()
    {
        try
        {
            return (String) super.get( VaultContext.CHALLENGE_KEY );
        }
        catch( ContextException e )
        {
            final String error = "Unexpected exception while retrieving challenge.";
            throw new CascadingRuntimeException( error, e );
        }
    }
}
