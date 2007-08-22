/*
 */
package net.osm.session.resource;

import net.osm.realm.StandardPrincipal;


/**
 * OwnedResource exposes operations dealing with access to the principal owner
 * of the resource.
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */
public interface OwnedResource 
{

   /**
    * Method to return the pricipal owner of this instance.
    *
    * @return StandardPrincipal - the security principal that owns this instance.
    */
    public StandardPrincipal getPrincipalOwner();

}



