
package net.osm.session.user;

import java.util.List;

import org.omg.Session.connect_state;

import net.osm.session.user.User;
import net.osm.session.user.UserHelper;
import net.osm.session.resource.AbstractResourceValue;
import net.osm.session.message.SystemMessage;

/**
 * An adapter providing EJB style access to a <code>User</code>.
 */
public class UserValue extends AbstractResourceValue
implements UserAdapter
{
    //=============================================================
    // static
    //=============================================================

    public static final String BASE_KEYWORD = "user";

    /**
     * Return the truncatable ids
     * @return String[] truncatable ids
     */
    static final String[] _ids_list =
    {
        "IDL:osm.net/session/user/UserAdapter:1.0",
    };

    //=============================================================
    // state
    //=============================================================

    User m_user;
   
    //=============================================================
    // constructors
    //=============================================================
    
   /**
    * Default constructor.
    */
    public UserValue( ) 
    {
    }

   /**
    * Creation of a new DefaultUserAdapter.
    * @param primary the <code>User</code object reference 
    *   backing the adapter
    */
    public UserValue( User primary ) 
    {
	  super( primary );
    }

    //=============================================================
    // DefaultUserAdapter
    //=============================================================

    /**
     * Returns the primary object reference.
     * @return User the primary <code>User</code> object reference.
     */
    public User getPrimaryUser()
    {
        if( m_user != null ) return m_user;
        m_user = UserHelper.narrow( m_primary );
        return m_user;
    }

    //=============================================================
    // UserAdapter
    //=============================================================

    /**
     * Returns the connected state of the user.
     * @return  boolean TRUE if the user is connected, otherwise FALSE
     */
    public boolean getConnected( )
    {
        return ( getPrimaryUser().connectstate() == connect_state.connected );
    }

    /**
     * Enqueue a message to this user.
     * @param  message the message to enqueue
     * @exception UserRuntimeException if the user backing this adapter does not support
     *   mailbox semantics
     */
    public void enqueue( SystemMessage message )
    throws UserRuntimeException
    {
        getPrimaryUser().enqueue( message );
    }

    //=============================================================
    // Adapter
    //=============================================================

    /**
     * Suppliments the supplied <code>StringBuffer</code> with a short 
     * description of the adapted object.
     * @param  buffer the string buffer to append the description to
     * @param  lead a <code>String</code> that is prepended to content
     */
    public void report( StringBuffer buffer, String lead )
    {
        super.report( buffer, lead );
        buffer.append( "\n" + lead + "Connected: " + getConnected() );
    }

    /**
     * Return a URL for the adapter.
     */
     public String getURL()
     {
         return "user?resolve=" + getIdentity();
     }

   /**
    * Returns the static base keyword for the entity.
    */
    public String getBase()
    {
        return BASE_KEYWORD;
    }


    //=============================================================
    // ValueBase
    //=============================================================

    public String [] _truncatable_ids()
    {
        return _ids_list;
    }


}
