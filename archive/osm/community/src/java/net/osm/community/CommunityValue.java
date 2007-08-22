// Sun Dec 17 16:53:23 CET 2000

package net.osm.community;

import java.util.List;

import org.omg.CommunityFramework.Community;
import org.omg.CommunityFramework.CommunityHelper;

import net.osm.session.workspace.WorkspaceValue;


/**
 * An adapter providing EJB style access to a <code>Community</code>.
 */
public class CommunityValue extends WorkspaceValue
implements CommunityAdapter
{
    //=============================================================
    // static
    //=============================================================

    public static final String BASE_KEYWORD = "community";

    /**
     * Return the truncatable ids
     */
    static final String[] _ids_list =
    {
        "IDL:osm.net/community/CommunityAdapter:1.0",
    };

    //=============================================================
    // state
    //=============================================================

    private Community m_community;

    
    //=============================================================
    // constructors
    //=============================================================
    
   /**
    * Default constructor.
    */
    public CommunityValue( ) 
    {
    }
    
   /**
    * Creation of a new CommunityAdapter.
    * @param primary the primary <code>Community</code> object reference
    *  backing the adapter.
    */
    public CommunityValue( org.omg.CommunityFramework.Community primary ) 
    {
	  super( primary );
    }

    //=============================================================
    // CommunityAdapter
    //=============================================================

    /**
     * Returns the primary <code>Community</code> object reference.
     * @return org.omg.CommunityFramework.Community the primary object reference
     */
    public Community getPrimaryCommunity()
    {
        if( m_community != null ) return m_community;
        m_community = CommunityHelper.narrow( m_primary );
        return m_community;
    }

    //=============================================================
    // Adapter
    //=============================================================

    /**
     * Return a URL for the adapter.
     */
     public String getURL()
     {
         return "community?resolve=" + getIdentity();
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
