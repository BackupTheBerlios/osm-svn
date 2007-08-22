/*
 * Copyright 2002 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 09/04/2001
 */

package net.osm.community;

import org.omg.CORBA.ORB;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.AnyHolder;
import org.omg.CommunityFramework.Recognizes;
import org.omg.Session.UserIteratorOperations;
import org.omg.CosPersistentState.StorageObject;

import net.osm.session.linkage.LinkStorage;
import net.osm.session.linkage.LinkStorageIteratorDelegate;
import net.osm.session.resource.AbstractResourceIteratorDelegate;
import net.osm.list.Iterator;

/**
 * Implementation of the UserIterator interface.
 */

public class UserIteratorDelegate extends AbstractResourceIteratorDelegate 
implements UserIteratorOperations
{

   /**
    * Role constraint.
    */
    protected String m_label;

   /**
    * UserIteratorDelegate constructor.
    * @param orb
    * @param Iterator from the persistent list
    * @link the last LinksStorage instance returned from the iterator
    */
    public UserIteratorDelegate( Iterator iterator )
    {
        super( iterator );
    }

   /**
    * UserIteratorDelegate constructor for role filtered lists of users.
    * @param iterator of a list of links
    * @param role label
    */
    public UserIteratorDelegate( Iterator iterator, String label )
    {
        super( iterator );
	  if( label != null )
        {
            m_label = label;
	      set_to_first_element();
	  }
    }

   /**
    * Returns true if the supplied LinkStorage object is a candidate.
    */
    public boolean evaluate( StorageObject s, TypeCode t )
    {
	  boolean candidate = super.evaluate( s, t );
	  if( candidate && ( m_label != null )) 
	  {
		try
	      {
		    return hasRole( (Recognizes) ((LinkStorage)s).link(), m_label );
	      }
	      catch( Exception e )
	      {
	          return false;
            }
	  }
	  else
	  {
	      return candidate;
        }
    }
      
   /**
    * Returns true if the supplied <code>Recognizes</code> link contains the supplied role label.
    * @param role the link containing a sequence of role labels
    * @param label the role label to locate
    */
    protected boolean hasRole( Recognizes role, String label )
    {
	  // WARNING: The following implementation needs to be updated to handle
        // assessment of roles inherited from any parent roles of the roles
        // listed in the Recognizes link - i.e. this test should be moved to a 
        // static method in a seperate MembershipManager class

        for( int i=0; i<role.roles.length; i++ )
        {
            if( role.roles[i].equals( label )) return true;
        }
        return false;
    }

}
