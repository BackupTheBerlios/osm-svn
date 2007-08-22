package net.osm.realm;

import java.io.Serializable;
import java.util.Hashtable;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA_2_3.portable.InputStream;


/**
 * A table of access descriptions for a particular role.
 */
public class AccessTableBase extends AccessTable implements ValueFactory
{

    private Hashtable keys;

    //=======================================================
    // constructors
    //=======================================================

   /**
    * Default constructor used during internalization.
    */
    public AccessTableBase()
    {
    }

   /**
    * Creates a new instance of <code>AccessTableBase</code>
    * for the supplied role with a supplied default access policy and 
    * access exceptions.
    * @param role - name of the role that this table applies to
    * @param defaultAccessPolicy - default access policy to apply if not 
    *   explicity declared under the access exceptions list.
    * @param exceptions - sequence of access descriptors that declare 
    *   access policy that overrides the default policy
    */
    public AccessTableBase( final String role, boolean defaultAccessPolicy, AccessDescriptor[] exceptions )
    {
        this.role = role;
        this.defaultAccessPolicy = defaultAccessPolicy;
        this.exceptions = exceptions;
    }

    //===========================================================
    // AccessTable
    //===========================================================

   /**
    * Returns the name of the role that this access table is 
    * defining access policy for.
    */
    public String getRole()
    {
        return this.role;
    }

   /**
    * Returns true if the supplied operation is accessible
    * for a principal holding a role of the same name.
    */
    public boolean accessible( String operation )
    {
        initializeKeys( );
        Boolean value = (Boolean) keys.get( operation );
        if( value == null ) return defaultAccessPolicy;
        return value.booleanValue();
    }

    private void initializeKeys( )
    {
        if( keys == null ) 
	  {
	      keys = new Hashtable();
		for( int i=0; i<exceptions.length; i++ )
	      {
		    boolean policy = exceptions[i].accessible;
		    String operation = exceptions[i].operation;
		    keys.put( operation, new Boolean( policy ) );
	      }
	  }
    }

    //===========================================================
    // ValueFactory
    //===========================================================
    
    public Serializable read_value( InputStream is ) {
        return is.read_value( new AccessTableBase( ) );
    }


}

