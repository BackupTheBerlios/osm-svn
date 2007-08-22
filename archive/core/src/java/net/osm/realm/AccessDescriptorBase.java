package net.osm.realm;

import java.io.Serializable;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA_2_3.portable.InputStream;


/**
 * Association of an operation name with an access policy.  Used
 * within an AccessTable to declare access policy for an operation
 * that is different from a default policy.
 */
public class AccessDescriptorBase extends AccessDescriptor implements ValueFactory
{

    //=======================================================
    // constructors
    //=======================================================

   /**
    * Default constructor used during internalization.
    */
    public AccessDescriptorBase()
    {
    }

   /**
    * Creates a new instance of <code>AccessDescriptorBase</code>
    * with the supplied operation name and access policy.
    * @param operation - name of the operation againsrt which the access policy applies
    * @param accessible - the access policy for the operation
    */
    public AccessDescriptorBase( final String operation, final boolean accessible )
    {
        this.operation = operation;
        this.accessible = accessible;
    }

    //===========================================================
    // ValueFactory
    //===========================================================
    
    public Serializable read_value( InputStream is ) {
        return is.read_value( new AccessDescriptorBase( ) );
    }

}

