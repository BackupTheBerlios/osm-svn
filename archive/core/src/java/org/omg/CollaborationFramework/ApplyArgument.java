// Tue Dec 19 00:44:43 CET 2000

package org.omg.CollaborationFramework;

import java.io.Serializable;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.Session.AbstractResource;
import org.omg.Session.AbstractResourceHelper;

/**
 * ApplyArgument is a valuetype that contains the declaration of a Usage link tag name and a value containing a
 * reference to an AbstractResource to be associated to the Encounter coordinating the Collaboration
 * under a new or existing usage link with the same tag name.
 */

public class ApplyArgument
    implements StreamableValue, ValueFactory
{
    
    //
    //  state members
    //

   /**
    * An ApplyArgument is a valuetype that can be passed into an apply
    * operation. The tag value must be equal to a tag value declared under
    * the processors input usage list (declaration of InputDescriptor values
    * exposed by ProcessorModel usage field). Following assessment of any
    * preconditions associated with a referenced Trigger, an implementation
    * of apply will create or replace an existing consumption link resource
    * value on the associated Task with the value field of the ApplyArgument
    * valuetype.
    */

    public String label;
    
   /**
    * The AbstractResource to associate under a tagged consumption link with
    * the Task associated as coordinator to the Collaboration.
    */

    public AbstractResource value;
    

   /**
    * Null argument constructor used during stream internalization.
    */

    public ApplyArgument()
    {
    }

   /**
    * Creation of a new ApplyArgument valuetype based on the supplied 
    * AbstractResource and label.
    */

    public ApplyArgument( String label, AbstractResource resource )
    {
	  this.label = label;
	  this.value = resource;
    }

    //
    // implementation of Streamable
    //
    
    public org.omg.CORBA.TypeCode _type()
    {
        return ApplyArgumentHelper.type();
    }
    
    public void _read(InputStream _is)
    {
        label = LabelHelper.read(_is);
        value = AbstractResourceHelper.read(_is);
    }
    
    public void _write(OutputStream _os)
    {
        LabelHelper.write(_os, label);
        AbstractResourceHelper.write(_os, value);
    }
    
    //
    // implementation of ValueBase
    //
    
    public final String[] _ids_list = { 
        "IDL:omg.org/CollaborationFramework/ApplyArgument:1.0"
    };
    
    public String[] _truncatable_ids() { return _ids_list; }

    //
    // implementation of ValueFactory
    //
    
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new ApplyArgument() );
    }

}
