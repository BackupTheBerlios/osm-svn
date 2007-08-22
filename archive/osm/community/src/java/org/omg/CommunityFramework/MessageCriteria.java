/*
 * @(#)MessageCriteria.java
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
 * @osm.warning defintion of message criteria is currently incomplete - needs
 * supplimentary info such as message subject, body, etc.
 */
public class MessageCriteria 
extends Criteria
{

    //
    // constructors
    //
    
   /**
    * Null argument constructor used during stream internalization.
    */
    public MessageCriteria( ){}

   /**
    * Creates a UserCriteria based on a supplied DOM element.
    */
    public MessageCriteria( Configuration config )
    {
        super( config );
    }

    //
    // Return the truncatable ids
    //
    static final String[] _ids_list =
    {
        "IDL:omg.org/CommunityFramework/MessageCriteria:1.0",
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
        return MessageCriteriaHelper.type();
    }

    //
    // implementation of ValueFactory
    //
    
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new MessageCriteria() );
    }

}

