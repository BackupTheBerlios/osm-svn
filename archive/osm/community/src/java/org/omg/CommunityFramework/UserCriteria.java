/*
 * @(#)UserCriteria.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 29/03/2001
 */

package org.omg.CommunityFramework;

import java.io.Serializable;

import org.apache.avalon.framework.configuration.Configuration;

import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA.portable.StreamableValue;

/**
 * UserCriteria is a valuetype that can be passed to a ResourceFactory 
 * facilitating the instantiation of a new User instance.
 */
public class UserCriteria 
extends Criteria
{

    //
    // constructors
    //
    
   /**
    * Null argument constructor used during stream internalization.
    */
    public UserCriteria( )
    {
    }

   /**
    * Creates a UserCriteria based on a supplied Configuration instance.
    */
    public UserCriteria( Configuration config )
    {
      super( config );
    }

    //
    // Return the truncatable ids
    //
    static final String[] _ids_list =
    {
        "IDL:omg.org/CommunityFramework/UserCriteria:1.0",
    };

    public String [] _truncatable_ids()
    {
        return _ids_list;
    }

    //
    // Unmarshal the value into an InputStream
    //
    public void _read( org.omg.CORBA.portable.InputStream is )
    {
	  super._read( is );
    }

    //
    // Marshal the value into an OutputStream
    //
    public void _write( org.omg.CORBA.portable.OutputStream os )
    {
	  super._write( os );
    }

    //
    // Return the value TypeCode
    //
    public org.omg.CORBA.TypeCode _type()
    {
        return UserCriteriaHelper.type();
    }

    //
    // implementation of ValueFactory
    //
    
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new UserCriteria() );
    }

}

