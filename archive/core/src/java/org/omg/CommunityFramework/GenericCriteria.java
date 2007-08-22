
package org.omg.CommunityFramework;

import java.io.Serializable;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;
import org.apache.avalon.framework.configuration.Configuration;

/**
* An GenericCriteria is used as an argument to a ResourceFactory during 
* the construction of a new GenericResource business object.  GenericCriteria
* extends the base Criteria type through the addition of the 'identifier' 
* state member.  The identitier is an IDL repository identifier of the type
* of object that can be held by a generic resource created using this criteria.
*/

public class GenericCriteria 
extends Criteria
{
    
    //==========================================================
    // static
    //==========================================================
    
   /**
    * Return the truncatable ids
    */
    static final String[] _ids_list = { 
        "IDL:omg.org/CommunityFramework/GenericCriteria:2.1",
    };

    //==========================================================
    // static
    //==========================================================

   /**
    * IDL identifier of permissable valuetypes that can be
    * added to a generic resource.
    */
    public String identifier;

    //==========================================================
    // constructors
    //==========================================================
    
   /**
    * Default constructor for stream internalization.
    */
    public GenericCriteria(){}

   /**
    * Creation of a new GenericCriteria based on a label and note.
    */
    public GenericCriteria( String label, String note, String identifier ) 
    {
	  super( label, note );
        this.identifier = identifier;
    }
    
   /**
    * Criteria based on a supplied Configuration instance.
    */
    public GenericCriteria( Configuration conf )
    {
	  super( conf );
        try
	  {
		// need a better default value - something that is usable
		this.identifier = conf.getAttribute("identifier","IDL:omg.org/CORBA/ValueBase:1.0");
        }
	  catch( Exception e )
	  {
	      throw new RuntimeException("Failed to create configured criteria.", e );
	  }
    }

   /**
    * Return the value TypeCode
    */
    public TypeCode _type()
    {
        return GenericCriteriaHelper.type();
    }
    
   /**
    * Return the truncatable ids
    */
    public String[] _truncatable_ids() { return _ids_list; }
    
   /**
    * Unmarshal the value into an InputStream
    */
    public void _read( org.omg.CORBA.portable.InputStream is )
    {
        super._read( is );
        identifier = is.read_string();
    }

   /**
    * Marshal the value into an OutputStream
    */
    public void _write( org.omg.CORBA.portable.OutputStream os )
    {
	  super._write( os );
	  os.write_string(identifier);
    }

   /**
    * GenericCriteria factory.
    */
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new GenericCriteria() );
    }

}
