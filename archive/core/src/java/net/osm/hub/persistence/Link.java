
package net.osm.hub.persistence;

import net.osm.hub.pss.LinkStorageBase;

public class Link extends LinkStorageBase
{
    
   /**
    * Returns true if the supplied link is equal to the link contained by this
    * sotrage object.
    * @param link - the link to compare with
    * @return boolean - true if the links are equal
    * @osm.warning Does not currently support assesment of equality of Usage links 
    *   (i.e. assessment of usage tag value).
    */
    public boolean equal( org.omg.Session.Link link )
    {
        try
        {
            //return link().resource().get_key().equal( link.resource().get_key() );
            return link().resource()._is_equivalent( link.resource() );
        }
        catch (Exception e)
        {
            throw new org.omg.CORBA.INTERNAL("failed to resolve key equality");
        }
    }
}
