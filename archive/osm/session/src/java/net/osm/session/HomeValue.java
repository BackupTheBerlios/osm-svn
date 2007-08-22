
package net.osm.session;

import java.net.URL;
import java.util.List;
import java.util.LinkedList;
import net.osm.adapter.Adapter;
import net.osm.chooser.ChooserValue;
import net.osm.adapter.ServiceValue;
import net.osm.session.user.PrincipalAdapter;
import net.osm.finder.ObjectNotFound;
import net.osm.finder.InvalidPath;

/**
 * An adapter for an <code>Home</code> that provides 
 * state accessors that follow the EJB patterns.
 */
public class HomeValue extends ChooserValue
implements HomeAdapter
{
    //=============================================================
    // static
    //=============================================================
    
    /**
     * Return the truncatable ids
     */
    static final String[] _ids_list =
    {
        "IDL:osm.net/session/HomeAdapter:1.0",
    };

    public static final String BASE_KEYWORD = "session";

    //=============================================================
    // state
    //=============================================================
    
    private Home m_home;

    //=============================================================
    // constructors
    //=============================================================
 
   /**
    * Default constructor.
    */
    public HomeValue( ) 
    {
    }
   
   /**
    * Creation of a new GatewayValue.
    * @param home the <code>Home</code> object reference
    * @param url a corbaloc URL
    * @param name the gateway presentation name
    * @param description short description of the gateway
    */
    public HomeValue( Home home, URL url, String[] path, String name, String description, String[] keys ) 
    {
        super( home, url, path, name, description, keys );
    }

    //=============================================================
    // FinderAdapter
    //=============================================================

    /**
     * Returns a object resolved from the supplied path.
     * @param  path a string that identifies a path to an object
     * @return  Adapter an adapter backed by an object reference
     * @exception  InvalidPath thrown if the path is invalid
     * @exception  ObjectNotFound thrown if the path cannot be resolved
     */
    public Adapter resolve(String path) 
    throws InvalidPath, ObjectNotFound
    {
        try
        {
            return getHome().resolve( path ).get_adapter();
        }
        catch( InvalidPath e )
        {
            final String error = "InvalidPath error rasied by remote from path: " + path;
            throw new InvalidPath( error, path );
        }
        catch( ObjectNotFound e )
        {
            final String msg = "ObjectNotFound on remote from path: " + path;
            throw new ObjectNotFound( msg, path );
        }
        catch( Throwable e )
        {
            final String error = "Remote exception while resolving path: " + path;
            throw new SessionRuntimeException( error, e );
        }
    }

    //=============================================================
    // HomeAdapter
    //=============================================================

    /**
     * Returs the primary session object reference.
     */
    public Home getHome()
    {
        if( m_home != null ) return m_home;
        m_home = HomeHelper.narrow( m_primary );
        return m_home;
    }

   /**
    * Returns a user relative to the undelying principal.
    * @param policy TRUE if a new should be created if the principal is unknown
    *   otherwise, the UnknownPrincipal exception will be thrown if the principal
    *   cannot be resolved to a user reference
    * @return PrincipalAdapter an adapter wrapping a user object reference
    * @exception UnknownPrincipal if the underlying principal does not
    *    match a registered user.
    */
    public PrincipalAdapter resolve_user( boolean policy ) throws UnknownPrincipal
    {
        return (PrincipalAdapter) getHome().resolve_user( policy ).get_adapter();
    }

    public String toString()
    {
        return toString( "session" );
    }

    public String getURL()
    {
        return "session";
    }

   /**
    * Returns the static base keyword for the entity.
    */
    public String getBase()
    {
        return BASE_KEYWORD;
    }


    //=======================================================================
    // StreamableValue
    //=======================================================================

   /**
    * Returns the truncatable ids identifying this valuetype.
    * @return String[] truncatable ids
    */
    public String [] _truncatable_ids()
    {
        return _ids_list;
    }

}
