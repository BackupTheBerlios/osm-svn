
package org.omg.CommunityFramework;

import java.io.Serializable;

import org.apache.avalon.framework.configuration.Configuration;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;

/**
* AgencyCriteria is a valuetype derived from CommunityCriteria that is 
* used as an argument to a ResourceFactory.  A ResourceFactory implementation
* is responsible for seperating the "about" named value as the value of the 
* Agency <code>about</code> state member.
*/


public class AgencyCriteria extends CommunityCriteria
implements StreamableValue, ValueFactory
{
    
    //==========================================================
    // static
    //==========================================================
    
   /**
    * Return the truncatable ids
    */
    static final String[] _ids_list = { 
        "IDL:omg.org/CommunityFramework/AgencyCriteria:1.0",
    };

    //==========================================================
    // constructors
    //==========================================================

   /**
    * Default constructor for stream internalization.
    */
    public AgencyCriteria( ){}

   /**
    * Creation of an AgencyCriteria based on a supplied configuration.
    */
    public AgencyCriteria( Configuration config ) {
	  super( config );
    }

    //==========================================================
    // AgencyCriteria
    //==========================================================

   /**
    * Return the value TypeCode
    */
    public TypeCode _type()
    {
        return AgencyCriteriaHelper.type();
    }
        
   /**
    * Return the truncatable ids
    */
    public String[] _truncatable_ids() { return _ids_list; }
    
   /**
    * AgencyCriteria factory.
    */    
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new AgencyCriteria() );
    }

}
