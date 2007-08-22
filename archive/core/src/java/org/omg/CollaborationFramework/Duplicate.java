
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
Instructs an implementation of Collaboration to create a new consumption link named target
based on the state of a source link. If the value of invert is false, the type of link created is the
same as the source link. If invert is true, then if the source link is a Consumption link, the created
link will be a Production link and visa-versa. The resource associated to the new target link shall
be the same as the resource declared under the source link.
*/

public class Duplicate
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
    * If true, an implementation of Collaboration is required to create a
    * new Usage link using the inverse type (i.e. if source is Consumption then
    * target type is Production, is source is Production then target type is
    * Consumption). The new usage link is added to the coordinating Task.
    */

    public boolean invert;
    

    //
    // constructor
    //

   /**
    * Null argument constructor used during stream internalization.
    */
    public Duplicate( ){}

   /**
    * Creates a Duplicate based on a supplied Configuration instance.
    */
    public Duplicate( Configuration config )
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
        return DuplicateHelper.type();
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
        "IDL:omg.org/CollaborationFramework/Duplicate:1.0"
    };
    
    public String[] _truncatable_ids() { return _ids_list; }

    //
    // implementation of ValueFactory
    //
    
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new Duplicate() );
    }

}
