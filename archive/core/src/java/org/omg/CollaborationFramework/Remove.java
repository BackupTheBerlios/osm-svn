// Thu Nov 23 07:22:02 CET 2000

package org.omg.CollaborationFramework;

import java.io.Serializable;

import org.apache.avalon.framework.configuration.Configuration;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;

/**
The Remove Directive directs a Collaboration implementation to remove a tagged Usage link
(with a tag value corresponding to source) from the coordinating Task.
*/

public class Remove
implements Directive, StreamableValue, ValueFactory
{
    
    //
    //  state members
    //
    
   /**
    * The name (tag value) of an existing link held by the coordinating Task.
    */
    public String source;


    //
    // constructors
    //
    
    /**
    * Null argument constructor used during stream internalization.
    */

    public Remove(){}

    /**
    * Creates a Remove based on a supplied Configuration instance.
    */
    public Remove( Configuration config )
    {
	  try
	  {
            source = config.getAttribute("source");
        }
	  catch( Exception e )
	  {
		throw new RuntimeException( 
              "unable to create new configured Remove instance", e );
	  }
    }


    //
    // implementation of Streamable
    //
    
    public TypeCode _type()
    {
        return RemoveHelper.type();
    }
    
    public void _read(InputStream _is)
    {
        source = LabelHelper.read(_is);
    }
    
    public void _write(OutputStream _os)
    {
        LabelHelper.write(_os, source);
    }
    
    //
    // implementation of ValueBase
    //
    
    static final String[] _ids_list = { 
        "IDL:omg.org/CollaborationFramework/Remove:1.0"
    };
    
    public String[] _truncatable_ids() { return _ids_list; }


    //
    // implementation of ValueFactory
    //
    
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new Remove() );
    }

}
