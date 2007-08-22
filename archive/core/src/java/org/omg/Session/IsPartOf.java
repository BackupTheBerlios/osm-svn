// Sun Dec 17 16:53:23 CET 2000

package org.omg.Session;

import java.io.Serializable;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;


public class IsPartOf
extends CollectedBy
implements StreamableValue, ValueFactory
{
   
    static final String[] _ids_list = { 
        "IDL:omg.org/Session/IsPartOf:1.0",
    };
 
    //
    // constructors
    //
    
   /**
    * Default constructor for stream internalization.
    */
    public IsPartOf() {
    }

   /**
    * Creation of a new IsPartOf link based on a supplied Workspace.
    */
    public IsPartOf( Workspace resource ) 
    {
	  this.resource_state = resource;
    }

   /**
    * Return the IsPartOf value TypeCode
    */
    public TypeCode _type()
    {
        return IsPartOfHelper.type();
    }
    
   /**
    * Unmarshal the value into an InputStream
    */
    public void _read(InputStream is)
    {
        super._read(is);
    }
    
   /**
    * Marshal the value into an OutputStream
    */
    public void _write(OutputStream os)
    {
        super._write(os);
    }
        
   /**
    * Return the truncatable ids
    */
    public String[] _truncatable_ids() { return _ids_list; }
    
   /**
    * IsPartOf factory.
    */
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new IsPartOf() );
    }
}
