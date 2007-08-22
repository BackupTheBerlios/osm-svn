package net.osm.realm;

import java.io.Serializable;
import java.util.Hashtable;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA_2_3.portable.InputStream;


/**
 * A table of access descriptions for a particular role.
 */
public class AccessPolicyBase extends AccessPolicy implements ValueFactory
{

    //=======================================================
    // static
    //=======================================================

    private static final boolean trace = false;

    //=======================================================
    // state
    //=======================================================

    private Hashtable keys;

    //=======================================================
    // constructors
    //=======================================================

   /**
    * Default constructor used during internalization.
    */
    public AccessPolicyBase()
    {
    }

   /**
    * Creates a new instance of <code>AccessPolicyBase</code>
    * with the supplied set of access decision tables.
    * @param tables - role based access decision tables
    */
    public AccessPolicyBase( AccessTable[] tables )
    {
        this.tables = tables;
    }

    //===========================================================
    // AccessTable
    //===========================================================

   /**
    * Method returns true if the principal can access the 
    * operation under one or more of the supplied roles.
    * @param roles - a set of roles name against which the 
    *    access essessment is applied
    * @param operation - the operation name that the access 
    *    decision concerns
    */
    public boolean accessible( String[] roles, String operation ) 
    {
        initializeTables( );
	  for( int i=0; i<roles.length; i++ )
	  {
            AccessTable table = (AccessTable) keys.get( roles[i] );
            if( table != null )
	      {
	          boolean access = table.accessible( operation );
		    if( trace ) System.out.println("Lookup access in table " + table.getRole() + " returned " + access );
		    if( access ) return true;
	      }
        }
	  return false;
    }

    private void initializeTables( )
    {
        if( keys == null ) 
	  {
            keys = new Hashtable();
		for( int i=0; i<tables.length; i++ )
	      {
		    String role = tables[i].getRole();
		    keys.put( role, tables[i] );
	      }
	  }
    }


    //===========================================================
    // ValueFactory
    //===========================================================
    
    public Serializable read_value( InputStream is ) {
        return is.read_value( new AccessPolicyBase( ) );
    }


}

