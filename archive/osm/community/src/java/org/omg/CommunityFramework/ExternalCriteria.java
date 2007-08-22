
package org.omg.CommunityFramework;

import java.io.Serializable;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;
import org.apache.avalon.framework.configuration.Configuration;

/**
* An ExternalCriteria is a container of a reference to an XML resource
* containing a DPML Criteria description.
*/

public class ExternalCriteria extends Criteria
implements StreamableValue, ValueFactory
{
    
    //==========================================================
    // static
    //==========================================================
    
   /**
    * Return the truncatable ids
    */
    static final String[] _ids_list = { 
        "IDL:omg.org/CommunityFramework/ExternalCriteria:1.0",
    };

    //==========================================================
    // state
    //==========================================================
    
    /**
    * XML public identifier.
    */
    public String common;
    
    /**
    * XML system identifier.
    */
    public String system;
    
    //==========================================================
    // constructors
    //==========================================================

   /**
    * Default constructor for stream internalization.
    */
    public ExternalCriteria( )
    {
    }

   /**
    * Creation of a new Role based on a supplied configuration.
    */
    public ExternalCriteria( Configuration config )
    {
	  super( config );
	  try
	  {
	      common = config.getAttribute("public","");
            system = config.getAttribute("system","");
	  }
	  catch( Exception e )
	  {
		throw new RuntimeException("failed to create a new role", e );
	  }
    }

    //==========================================================
    // implementation
    //==========================================================

   /**
    * Return the value TypeCode
    */
    public TypeCode _type()
    {
        return ExternalCriteriaHelper.type();
    }
    
   /**
    * Unmarshal the value into an InputStream
    */
    public void _read(InputStream is)
    {
        super._read(is);
        common = org.omg.CORBA.StringValueHelper.read(is);
        system = org.omg.CORBA.StringValueHelper.read(is);
    }
    
   /**
    * Marshal the value into an OutputStream
    */
    public void _write(OutputStream os)
    {
        super._write(os);
        org.omg.CORBA.StringValueHelper.write(os,common);
	  org.omg.CORBA.StringValueHelper.write(os,system);
    }
    
   /**
    * Return the truncatable ids
    */
    public String[] _truncatable_ids() { return _ids_list; }
    
   /**
    * CollectedBy factory.
    */
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new ExternalCriteria() );
    }

}
