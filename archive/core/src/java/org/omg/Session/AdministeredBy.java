// Sun Dec 17 16:53:23 CET 2000

package org.omg.Session;

import java.io.Serializable;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;

public class AdministeredBy
extends AccessedBy
{
    
    //==========================================================
    // static
    //==========================================================
    
   /**
    * Return the truncatable ids
    */
    static final String[] _ids_list = { 
        "IDL:omg.org/Session/AdministeredBy:1.0",
    };

    //==========================================================
    // constructors
    //==========================================================
    
   /**
    * Default constructor for stream internalization.
    */
    public AdministeredBy() 
    {
    }

   /**
    * Creation of a new AdministeredBy link based on a supplied User.
    */
    public AdministeredBy( User resource ) 
    {
	  super( resource );
    }

    //==========================================================
    // AdministeredBy
    //==========================================================
    
   /**
    * Return the value TypeCode
    */
    public TypeCode _type()
    {
        return AdministeredByHelper.type();
    }
    
   /**
    * Return the truncatable ids
    */
    public String[] _truncatable_ids() { return _ids_list; }

   /**
    * AdministeredBy factory.
    */
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new AdministeredBy() );
    }

}
