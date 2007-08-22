// Tue Dec 19 00:44:43 CET 2000

package org.omg.CollaborationFramework;

import java.io.Serializable;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.Session.Task;
import org.omg.Session.TaskHelper;
import org.omg.Session.AbstractResource;

/**
 * The Execution link defined under the Task and Session specification declares an abstract
 * association between an AbstractResource, acting as a processor, and a Task. The abstract
 * Execution relationship is used as the base for definition of an abstract Coordination relationship.
 * Coordination serves as the base for the concrete links named Monitors, Coordinates, and
 * CoordinatedBy.
 */

public class CoordinatedBy
    implements Coordination, StreamableValue, ValueFactory
{
    
    //==========================================================
    // static
    //==========================================================
    
   /**
    * Return the truncatable ids
    */
    static final String[] _ids_list = { 
	"IDL:omg.org/CollaborationFramework/CoordinatedBy:1.0",
    };

    //==========================================================
    // state
    //==========================================================
    
   /**
    * The Task that Processor holding this link is coordinated by.
    */
    public Task resource_state;

    //==========================================================
    // constructors
    //==========================================================

   /**
    * Null argument constructor used during stream internalization.
    */
    public CoordinatedBy( ) 
    {
    }

   /**
    * Creation of a new ControlledBy link based on the supplied Master.
    */
    public CoordinatedBy( Task task ) 
    {
	  this.resource_state = task;
    }
    
    //==========================================================
    // CoordinatedBy
    //==========================================================

   /**
    * The resource operation returns the <code>AbstractResource</code> reference 
    * corresponding to a resource supporting the Task interface.
    * @return AbstractResource derived from Task.
    */
    public AbstractResource resource( ) {
        return (AbstractResource) resource_state;
    }

   /**
    * Return the value TypeCode
    */
    public TypeCode _type()
    {
        return CoordinatedByHelper.type();
    }
    
   /**
    * Unmarshal the value into an InputStream
    */
    public void _read(InputStream is)
    {
        resource_state = TaskHelper.read(is);
    }
    
   /**
    * Marshal the value into an OutputStream
    */
    public void _write(OutputStream os)
    {
        TaskHelper.write(os, resource_state);
    }
    
   /**
    * Return the truncatable ids
    */
    public String[] _truncatable_ids() { return _ids_list; }
    
   /**
    * CoordinatedBy factory.
    */
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new CoordinatedBy() );
    }

}
