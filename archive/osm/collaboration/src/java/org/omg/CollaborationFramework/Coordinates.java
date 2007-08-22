// Tue Dec 19 00:44:43 CET 2000

package org.omg.CollaborationFramework;

import java.io.Serializable;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.Session.AbstractResource;
import org.omg.Session.TaskHelper;
import org.omg.Session.Link;


/**
The Coordinates link is a type of Monitors (which in turn is a type of Execution) that references the Processor that a Task (or more typically an Encounter) holding this link coordinates.  A Task (or Encounter) may coordinate at most one Processor.
*/

public class Coordinates
extends Monitors
{
    
    //==========================================================
    // static
    //==========================================================
    
   /**
    * Return the truncatable ids
    */
    static final String[] _ids_list = { 
        "IDL:omg.org/CollaborationFramework/Coordinates:1.0",
    };

    //==========================================================
    // constructors
    //==========================================================

   /**
    * Null argument constructor used during stream internalization.
    */
    public Coordinates( ) 
    {
    }

   /**
    * Creation of a new Coordinates link based on the supplied Processor.
    * @param processor Processor - processor to be coordinated
    */
    public Coordinates( Processor processor ) 
    {
	  super( processor );
    }

    //==========================================================
    // Coordinates
    //==========================================================
    
   /**
    * Return the value TypeCode
    */
    public TypeCode _type()
    {
        return org.omg.CollaborationFramework.CoordinatesHelper.type();
    }
    
   /**
    * Return the truncatable ids
    */
    public String[] _truncatable_ids() { return _ids_list; }
    
   /**
    * Coordinates factory.
    */
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new Coordinates() );
    }

}
