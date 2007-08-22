// Sun Dec 17 16:53:23 CET 2000

package org.omg.Session;

import java.io.Serializable;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;


public class Collects
implements Containment, StreamableValue, ValueFactory
{
    //====================================
    // static
    //====================================

   /**
    * Return the truncatable ids
    */
    static final String[] _ids_list =
    {
        "IDL:omg.org/Session/Collects:1.0",
    };

    //====================================
    // state
    //====================================
    
    public AbstractResource resource_state;
    
    //====================================
    // constructors
    //====================================
    
   /**
    * Default constructor for stream internalization.
    */
    public Collects () 
    {
    }

   /**
    * Creation of a new Collects link based on a supplied AbstractResource.
    */

    public Collects( AbstractResource resource ) 
    {
	  this.resource_state = resource;
    }

    //====================================
    // Collects
    //====================================

   /**
    * The resource operation returns the <code>AbstractResource</code> that 
    * can be narrowed to a <code>Workspace</code> that holding this link.
    * @return  AbstractResource representing the collection source.
    */
    public AbstractResource resource()
    {
	  return this.resource_state;
    }

   /**
    * Returns the value TypeCode
    * @return TypeCode the Collects TypeCode
    */
    public TypeCode _type()
    {
        return CollectsHelper.type();
    }
    
   /**
    * Unmarshal the value into an InputStream
    */
    public void _read(InputStream is)
    {
        resource_state = AbstractResourceHelper.read(is);
    }
    
   /**
    * Marshal the value into an OutputStream
    */
    public void _write(OutputStream os)
    {
        AbstractResourceHelper.write(os, resource_state);
    }
    
    //
    // implementation of ValueBase
    //
        
    public String[] _truncatable_ids() { return _ids_list; }

    //
    // implementation of ValueFactory
    //
    
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new Collects() );
    }


}
