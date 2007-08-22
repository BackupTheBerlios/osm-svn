/*
 * Vault.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 22/08/2001
 */

package net.osm.shell.vault;

import java.util.List;
import java.util.Set;

import net.osm.shell.Entity;
import net.osm.vault.Vault;
import net.osm.vault.VaultException;

/**
 */

public interface DesktopVault extends Vault, Entity
{

   /**
    * Returns the public X509 certificate paths.
    *
    * @return Set a possibly empty set of certificate path instances
    * @exception RuntimeException if the security subject has not been initialized
    * @see java.security.cert.CertPath
    */
    public Set getCertificatePaths();

   /**
    * Authenticates the user based on the underlying login 
    * configuration.  A side-effect of this operation is the 
    * raising of a PrincipalEvent if the current principal
    * changes as a result of the login process.
    */
    public boolean login( ) throws VaultException;

   /**
    * Logout of a established security subject.  A side-effect of 
    * this operation is the raising of a PrincipalEvent referencing  
    * the default unknown principal.
    */
    public void logout( );

}
