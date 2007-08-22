/*
 */
package net.osm.session;

/**
 * Interface implemented by objects capable of exposing an object reference.
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */
public interface ReferenceObject
{

   /**
    * Returns an object reference.
    * @return the object reference
    */
    org.omg.CORBA.Object getReference();
    
}



