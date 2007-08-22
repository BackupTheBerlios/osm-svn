
package net.osm.shell.vault;

import java.util.LinkedList;
import javax.security.auth.Subject;
import java.security.Principal;
import javax.security.auth.x500.X500Principal;
import javax.swing.ImageIcon;

import net.osm.shell.DefaultEntity;
import net.osm.util.IconHelper;


/**
 * The <code>PrincipalEntity</code> class represents a X500 security principal and its
 * associated public and private credentials.
 *
 * @author  Stephen McConnell
 * @version 1.0 20 JUN 2001
 */
public class PrincipalEntity extends DefaultEntity
{

    private final Principal principal;

    private LinkedList views;

    private LinkedList properties;

    private static final String path = "net/osm/shell/image/principal.gif";

    private static final ImageIcon icon = IconHelper.loadIcon( path );


    //===================================================================
    // constructor
    //===================================================================

    PrincipalEntity( Principal principal )
    {
        super( principal.getName() );

        if( principal == null ) throw new RuntimeException(
	    "Attempting to construct a new PrincipalEntity with a null principal.");

        this.principal = principal;
    }

    //===================================================================
    // Entity impementation
    //===================================================================

   /**
    * Set the name of the entity to the supplied <code>String</code>.
    * The <codePrincipalEntity</code> implementation does nothing.
    *
    * @param name the new entity name
    */
    public void setName( String name ){}

   /**
    * Returns an <code>View</code> representing the view of the content 
    * of the object.
    */
    //public View getView()
    //{
    //    return getPropertyView();
    //}

   /**
    * The <code>getStandardViews</code> operation returns a list of panels 
    * representing different views of the content and/or associations 
    * maintained by holder.
    */
    //public LinkedList getStandardViews()
    //{
    //    if( views == null ) 
	//  {
	//      views = super.getStandardViews();
	//	views.add( getView() );
	//  }
   //     return views;
   // }

    //public LinkedList getProperties()
    //{
    //    if( properties == null )
     //   {
   //         properties = super.getProperties();
	//	properties.add( new Property("name", getName()));
    //    }
    //    return properties;
    //}

   /**
    * Returns the name of the <code>Entity</code> as a <code>String</code>.
    */
    public String getName()
    {
	  if( principal != null ) return principal.getName();
        return "Null principal";
    }
}
