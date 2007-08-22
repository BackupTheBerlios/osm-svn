// Sun Dec 17 16:53:23 CET 2000

package org.omg.Session;

import java.io.Serializable;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;


public class Administers
extends Accesses
implements StreamableValue, ValueFactory
{
 
    //==========================================================
    // static
    //==========================================================
    
   /**
    * Return the truncatable ids
    */
    static final String[] _ids_list = { 
        "IDL:omg.org/Session/Administers:1.0",
    };

    //==========================================================
    // constructors
    //==========================================================
   
   /**
    * Default constructor for stream internalization.
    */
    public Administers() {
    }

   /**
    * Creation of a new Administers link based on a supplied Workspace.
    */
    public Administers( Workspace resource ) {
	  super( resource );
    }

    //==========================================================
    // Administers
    //==========================================================
    
   /**
    * Return the value TypeCode
    */
    public TypeCode _type()
    {
        return AdministersHelper.type();
    }
        
   /**
    * Return the truncatable ids
    */
    public String[] _truncatable_ids() { return _ids_list; }

   /**
    * Administers factory.
    */
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new Administers() );
    }

}
