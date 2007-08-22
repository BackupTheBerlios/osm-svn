// Tue Dec 19 00:44:43 CET 2000

package org.omg.CollaborationFramework;

import java.io.Serializable;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.Session.AbstractResource;
import org.omg.Session.TaskHelper;
import org.omg.Session.Link;

import net.osm.collaboration.CollaborationRuntimeException;


/**
 * The Execution link defined under the Task and Session specification declares an abstract
 * association between an AbstractResource, acting as a processor, and a Task. The abstract
 * Execution relationship is used as the base for definition of an abstract Coordination relationship.
 * Coordination serves as the base for the concrete links named Monitors, Coordinates, and
 * CoordinatedBy.
 */

public class Monitors
implements Coordination, StreamableValue, ValueFactory
{
    
    //==========================================================
    // static
    //==========================================================
    
   /**
    * Return the truncatable ids
    */
    static final String[] _ids_list = { 
        "IDL:omg.org/CollaborationFramework/Monitors:1.0",
    };

    //==========================================================
    // state
    //==========================================================
    
    /**
    * A reference to a Processor that the Task holding this link monitors.
    */
    public Processor resource_state;
    
    //==========================================================
    // constructors
    //==========================================================

    /**
    * Null argument constructor used during stream internalization.
    */
    public Monitors( )
    {
        super();
    }

   /**
    * Creates a Monitors link based on a supplied Processor.
    */
    public Monitors( Processor processor )
    {
        super();
        this.resource_state = processor;
    }

    //==========================================================
    // Monitors
    //==========================================================

    /**
     * The resource operation returns the <code>AbstractResource</code> reference 
     * corresponding to a concrete Processor reference.
     * @return AbstractResource corresponding to a concrete Processor reference.
     */
    public AbstractResource resource( ) {
        return (AbstractResource) resource_state;
    }

   /**
    * Factory operation through which the inverse link can be created.
    * @param resource the <code>AbstractResource</code> to bind as the 
    * source of the inverse relationship.
    */
    public Link inverse( final AbstractResource resource )
    {
        try
        {
            return new CoordinatedBy( TaskHelper.narrow( resource ));
        }
        catch( Throwable e )
        {
            throw new CollaborationRuntimeException( 
               "Unexpected error occured while attempting to create an inverse link.",
               e );
        }
    } 

   /**
    * Return the value TypeCode
    */    
    public TypeCode _type()
    {
        return MonitorsHelper.type();
    }
    
   /**
    * Unmarshal the value into an InputStream
    */
    public void _read(InputStream is)
    {
        resource_state = ProcessorHelper.read(is);
    }
    
   /**
    * Marshal the value into an OutputStream
    */
    public void _write(OutputStream os)
    {
        ProcessorHelper.write(os, resource_state);
    }
    
   /**
    * Return the truncatable ids
    */
    public String[] _truncatable_ids() { return _ids_list; }
    
   /**
    * Monitors factory.
    */
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new Monitors() );
    }

}
