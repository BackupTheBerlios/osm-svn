// Sun Dec 17 16:53:23 CET 2000

package net.osm.session.desktop;

import java.util.List;

import net.osm.session.workspace.WorkspaceValue;
import net.osm.session.user.UserValue;
import net.osm.session.user.UserAdapter;
import net.osm.session.user.UserHelper;
import net.osm.session.user.User;

/**
 * An adapter providing EJB style access to a <code>Desktop</code>.
 */
public class DesktopValue extends WorkspaceValue
implements DesktopAdapter
{
    //=============================================================
    // static
    //=============================================================

    public static final String BASE_KEYWORD = "desktop";

    /**
     * Return the truncatable ids
     */
    static final String[] _ids_list =
    {
        "IDL:osm.net/session/desktop/DesktopAdapter:1.0",
    };

    //=============================================================
    // state
    //=============================================================

    private Desktop m_desktop;

   /**
    * Cached reference to the desktop owner adapter.
    */
    private UserAdapter m_user_adapter;
    
    //=============================================================
    // constructors
    //=============================================================
    
   /**
    * Default constructor.
    */
    public DesktopValue( ) 
    {
    }
    
   /**
    * Creation of a new DefaultDesktopAdapter.
    * @param primary the primary <code>Desktop</code> object reference
    *  backing the adapter.
    */
    public DesktopValue( org.omg.Session.Desktop primary ) 
    {
	  super( primary );
    }

    //=============================================================
    // DefaultDesktopAdapter
    //=============================================================

    /**
     * Returns the primnary <code>Desktop</code> obhect reference.
     * @return org.omg.Session.Desktop the primary object reference
     */
    public Desktop getPrimaryDesktop()
    {
        if( m_desktop != null ) return m_desktop;
        m_desktop = DesktopHelper.narrow( m_primary );
        return m_desktop;
    }

    //=============================================================
    // DesktopAdapter
    //=============================================================

    /**
     * Returns a <code>UserAdapter</code> that owns the desktop.
     * @return UserAdapter the user that owns the desktop
     */
    public UserAdapter getUser()
    {
        if( m_user_adapter != null ) return m_user_adapter;
        m_user_adapter = (UserAdapter) UserHelper.narrow( 
               getPrimaryDesktop().belongs_to() ).get_adapter();
        return m_user_adapter;
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
        buffer.append( "\n" + lead + "Owner: " + getUser().getName() );
    }

    /**
     * Return a URL for the adapter.
     */
     public String getURL()
     {
         return "desktop?resolve=" + getIdentity();
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

   /**
    * Returns the truncatable ids identifying this valuetype.
    * @return String[] truncatable ids
    */
    public String [] _truncatable_ids()
    {
        return _ids_list;
    }
}
