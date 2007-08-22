
package net.osm.session.util;

import java.util.List;
import java.util.Arrays;
import java.util.Iterator;

import org.omg.CORBA.Any;
import org.omg.CORBA.AnyHolder;
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.TypeCode;

import net.osm.adapter.Adaptive;
import net.osm.adapter.AdaptiveHelper;

import net.osm.session.SessionRuntimeException;

/**
 * The <code>AdapterIterator</code> class provides support for client simple forward  
 * iteration backed by a org.omg.CosCollection.Iterator and exposed returned values
 * as native adapters.
 */
public class AdapterIterator extends CollectionIterator
{

    //=========================================================================
    // constructor
    //=========================================================================

   /**
    * Constructor of a new <code>AdapterIterator</code> based on supplied CosCollection 
    * iterator where the agents exposed by the iterator are agents backed by reference
    * objects (as distict from valuetypes).
    * @param iterator a CosCollection iterator
    */
    public AdapterIterator( org.omg.CosCollection.Iterator iterator )
    {
        this( iterator, false );
    }

   /**
    * Constructor of a new <code>AdapterIterator</code> based on supplied CosCollection 
    * iterator.
    * @param iterator a CosCollection iterator
    * @param isValue true if objects exposed by the CosCollection iterator should be handled 
    *    as valuetypes (as distinct from object references).
    */
    public AdapterIterator( org.omg.CosCollection.Iterator iterator, boolean isValue )
    {
        super( iterator, isValue );
    }

    //=========================================================================
    // Operations
    //=========================================================================

   /**
    * Returns the next agent in the iteration.  The type of adapter returned from the 
    * <code>next</code> method will be dynamically resolved based on the type of 
    * underlying object returned from the iteration.
    */
    public Object next()
    {
        try
        {
            final Adaptive adaptive = AdaptiveHelper.narrow( (org.omg.CORBA.Object) super.next() );
            if( adaptive != null )
            {
                return adaptive.get_adapter();
            }
            else
            {
                final String error = "Iterator returned a null next value.";
                throw new SessionRuntimeException( error );
            }
        }
        catch( Throwable e )
        {
            final String error = "Unexpected exception while resolving next iteration value.";
            throw new SessionRuntimeException( error, e );
        }
    }
}
