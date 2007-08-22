
package net.osm.session.linkage;

import net.osm.session.SessionRuntimeException;

/**
 * Concrete implementation of the <code>LinkStorage</code> type providing support
 * for the persistent storage of link instances.
 */
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
            return link().resource()._is_equivalent( link.resource() );
        }
        catch( Exception e )
        {
            throw new SessionRuntimeException( "Unable to resolve link equality.", e );
        }
    }
}
