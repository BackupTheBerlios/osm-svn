// Sun Dec 17 16:53:23 CET 2000

package org.omg.Session;

import java.io.Serializable;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;


public class ComposedOf
extends Collects
{
    //================================================
    // static
    //================================================

    static final String[] _ids_list = { 
        "IDL:omg.org/Session/ComposedOf:1.0",
    };
    
    //================================================
    // constructor
    //================================================
    
   /**
    * Default constructor for stream internalization.
    */
    public ComposedOf () 
    {
    }

   /**
    * Creation of a new ComposedOf link based on a supplied AbstractResource.
    */
    public ComposedOf( AbstractResource resource ) 
    {
	  super( resource );
    }

    //================================================
    // implementation
    //================================================

    public TypeCode _type()
    {
        return ComposedOfHelper.type();
    }
    
    public void _read(InputStream is)
    {
        super._read(is);
    }
    
    public void _write(OutputStream os)
    {
        super._write( os);
    }
        
    public String[] _truncatable_ids() { return _ids_list; }

    //
    // implementation of ValueFactory
    //
    
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new ComposedOf() );
    }


}
