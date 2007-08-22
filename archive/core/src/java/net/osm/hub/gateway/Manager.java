/*
 */
package net.osm.hub.gateway;

import org.apache.avalon.framework.component.ComponentManager;


/**
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public interface Manager extends ComponentManager
{

   /**
    * Operation invoked to removal a servant from the cache.
    * @param oid persistent object identifier
    */
    public void remove( byte[] oid );

   /**
    * Creation of an object reference given persistent storage object 
    * identifier and IDL identifier.
    * @param pid the persitent object identifier
    * @param id the IDL type identifier
    * @return org.omg.CORBA.Object the object reference
    */
    public org.omg.CORBA.Object getReference( byte[] pid, String id );

}



