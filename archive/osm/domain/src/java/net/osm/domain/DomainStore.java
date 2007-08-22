/*
 * Copyright 2002 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 03/04/2001
 */

package net.osm.domain;

import org.omg.NamingAuthority.AuthorityId;
import org.omg.NamingAuthority.RegistrationAuthority;

/**
 * DomainStore is the PSS implementation of the storage object 
 * for the persistant management of domains exposed by the OMG
 * <code>NamingAuthority</code> specification. 
 */
public class DomainStore extends DomainStorageBase
{

    private AuthorityId m_id;

    //======================================================================
    // DomainStorage implementation
    //======================================================================

   /**
    * Returns the <code>DomainStorage</code> state in the 
    * form of a <code>omg.omg.AuthorityId</code> struct.
    * @return AuthorityId the domain identifier
    */
    public AuthorityId authority_id()
    {
        if( m_id != null ) return m_id;
        m_id = new AuthorityId( 
          RegistrationAuthority.from_int( authority() ), naming_entity() );
        return m_id;
    }
}
