/*
 * Copyright 2002 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 16/03/2002
 */

package net.osm.domain;

import org.apache.avalon.framework.component.Component;
import org.omg.CosPersistentState.NotFound;
import org.omg.NamingAuthority.AuthorityId;

/**
 * The <code>DomainService</code> provides support for the 
 * creation of new <code>DomainStorage</code> storage instances, 
 * and the retrival of existing instances.  The <code>DomainStorage</code>
 * provides access to persistently registered 
 * <code>org.omg.NamingAuthority.AuthorityId</code> structures.
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */
public interface DomainService extends Component
{

    public static final String DOMAIN_SERVICE_KEY = "domain.key";

   /**
    * Return the default <code>AuthorityId</code> the the domain manager 
    * is managing.
    * @return byte[] default domain short PID
    */
    public byte[] getDefaultDomain();
 
   /**
    * Creation of a new <code>AuthorityId</code> structure under a 
    * persistent store.
    * @param int the naming authority scheme
    * @param entity the domain name
    * @exception DomainException
    */
    public AuthorityId createDomainEntry( int scheme, String name ) 
    throws DomainException;

   /**
    * Locate an existing <code>AuthorityId</code> based on a supplied 
    * scheme and name.
    * @param int the naming authority scheme
    * @param entity the domain name
    * @exception NotFound
    */
    public AuthorityId locateDomainEntry( int scheme, String name ) 
    throws NotFound;

   /**
    * Resolve a domain and return a corresponding <code>AuthorityId</code> 
    * based on a supplied scheme and name.  If a domain does not exist in the
    * domain store, a new domain will be created.
    * @param int the naming authority scheme
    * @param entity the domain name
    */
    public AuthorityId resolveDomainEntry( int scheme, String name );

   /**
    * Get the short PID of a <code>DomainStorage</code> based on a supplied
    * scheme and name.
    * @param int the naming authority scheme
    * @param entity the domain name
    */
    public byte[] resolveDomainPID( int scheme, String name );

   /**
    * Returns an AuthorityID matching a supplied short PID.
    * @param pid short persitent object identifier
    * @return AuthorityId the matching authority ID structure
    */
    public AuthorityId authorityFromPID( byte[] pid );

}
