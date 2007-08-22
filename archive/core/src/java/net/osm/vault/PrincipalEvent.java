/*
 * PrincipalEvent.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 22/08/2001
 */

package net.osm.vault;

import java.util.EventObject;
import java.security.Principal;


/**
 * Principal event is a class defining the notification of the 
 * change of the value of a Principal.  Event of this type are 
 * established by a Vault following sucessful loading of a 
 * keystore based on the server configuration.
 *
 * @see Vault
 * @see PrincipalListener
 */
public class PrincipalEvent extends EventObject
{

    private Principal principal;

   /**
    * PrincipalEvent contructor from a supplied arguments.
    *
    * @param vault the source of the event
    * @param principal a security principal
    */
    public PrincipalEvent( Vault vault, Principal principal )
    {
        super( vault );
        this.principal = principal;
    }

   /**
    * Return the principal.
    * 
    * @return the singleton principal established by the vault.
    */
    public Principal getPrincipal()
    {
        return principal;
    }
}
