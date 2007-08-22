// Thu Nov 23 07:22:02 CET 2000

package org.omg.CollaborationFramework;

import java.io.Serializable;

import org.apache.avalon.framework.configuration.Configuration;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.ValueFactory;

/**
The Move directive is a directive to a Collaboration implementation to change a source
Consumption link name to the value of target. If the invert value of the Move instance is true, the
move directive implies replacement of the link with its inverse type – i.e. if the source link is a type
of Consumption link, then replace the link with a type of Production link. If the source link is a
type of Production link then replace the link with a type of Consumption link.
*/

public class Move
implements Directive, StreamableValue, ValueFactory
{
    
    //
    //  state members
    //
    
   /**
    * The name (tag value) of an existing link held by the coordinating Task.
   */

    public String source;

   /**
    * The name (tag value) of a Usage Link to be created or replaced on the coordinating Task.
    */

    public String target;

   /**
    * If true, an implementation of Collaboration is required to replace an
    * existing Usage link with the inverse (i.e. Consumption is replaced by
    * Production, Production is replaced by Consumption).
    */

    public boolean invert;
    

    //
    // constructors
    //
    
    /**
    * Null argument constructor used during stream internalization.
    */
    public Move( ){}

   /**
    * Creates a Duplicate based on a supplied Configuration instance.
    */
    public Move( Configuration config )
    {
        try
	  {
		source = config.getAttribute("source");
		target = config.getAttribute("target");
		invert = config.getAttributeAsBoolean("switch",false);
	  }
	  catch( Exception e )
	  {
            throw new RuntimeException("unable to create new configured Duplicate", e );
	  }
    }

    //
    // implementation of Streamable
    //
    
    public TypeCode _type()
    {
        return MoveHelper.type();
    }
    
    public void _read(InputStream is)
    {
        source = LabelHelper.read(is);
        target = LabelHelper.read(is);
        invert = is.read_boolean();
    }
    
    public void _write(OutputStream os)
    {
        LabelHelper.write(os, source);
        LabelHelper.write(os, target);
        os.write_boolean(invert);
    }
    

    //
    // implementation of ValueBase
    //
    
    static final String[] _ids_list = { 
        "IDL:omg.org/CollaborationFramework/Move:1.0"
    };
    
    public String[] _truncatable_ids() { return _ids_list; }


    //
    // implementation of ValueFactory
    //
    
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new Move() );
    }

}
