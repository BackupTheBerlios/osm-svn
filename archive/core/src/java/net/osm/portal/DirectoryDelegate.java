/*
 * @(#)DirectoryDelegate.java
 *
 * Copyright 2000 OSM S.A.R.L. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM S.A.R.L.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 29/07/2000
 */

package net.osm.portal;

import java.util.Vector;
import java.util.Enumeration;
import net.osm.discovery.Entry;
import net.osm.discovery.Filter;
import net.osm.discovery.Artifact;
import net.osm.discovery.DisclosurePolicy;
import net.osm.discovery.NoMatch;
import net.osm.discovery.Selection;
import net.osm.discovery.InvalidFilter;
import net.osm.discovery.Description;
import net.osm.discovery.DirectoryOperations;
import net.osm.discovery.SelectionSet;
import net.osm.discovery.Content;
import net.osm.discovery.Score;


public class DirectoryDelegate extends RegistryDelegate implements DirectoryOperations 
{

    //===================================================
    // state
    //===================================================

    protected Vector result;

    //===================================================
    // constructor
    //===================================================

    public DirectoryDelegate( ) {
	  super();
    }
    
    //===================================================
    // Directory
    //===================================================

   /**
    * The locate operation provides support for the location of a specific 
    * artifact based on a filter argument. 
    * @param filter the filter to apply 
    * @exception NoMatch if no entry matches the supplied filter
    * @exception InvalidFilter
    */
    public Artifact locate( Filter filter ) 
    throws NoMatch, InvalidFilter 
    {

	  Selection best = null;
	  try{
	  	SelectionSet s = this.find( filter );
	  	for( int i = 0; i < s.selections.length; i++ ) {
		    Selection last = (Selection)s.selections[i];
		    if( best == null ) { 
		        best = last;
		    } else {
		        if(last.ranking.ground() > best.ranking.ground()) best = last;
		    }
		}
        } catch (NoMatch e) {
	      throw new NoMatch();
        } finally {
        }
	  return best.artifact;
    }

    /**
    The find operation returns a ranked sequence of artifact instances based on the supplied
    filter argument.
    */

    public SelectionSet find( Filter filter ) throws NoMatch, InvalidFilter {

	  long t = System.currentTimeMillis();
	  result = new Vector();
        try{
    	      Enumeration enum = index.elements();
	      while( enum.hasMoreElements() ) {
		    Artifact a = (Artifact) enum.nextElement();

		    if( a instanceof Description ) {
		        Description r = (Description) a;
			  Score s = new ScoreBase();
			  if( filter.binary ) {
			  	boolean b = ((FilterOperations)filter).equivilent( 0, r );
				if( b ) { s.score(1,1); } else { s.score(0,1); };
			  } else {
				s = ((FilterOperations)filter).measure( r );
			  }
			  if( s.value > 0 ) result.add( new SelectionBase( r, s) );
		    }
	      }
        } catch (Exception e) {
		System.err.println("DirectoryDelegate.find: exception:");
		e.printStackTrace();
        } finally {
	      if( result.size() == 0) {
			throw new NoMatch();
		}
        }
	  // convert result vector to an array and pass back as a SelectionSet
	  return new SelectionSetBase( (Selection[]) result.toArray( new Selection[0]) );
    }

} // DirectoryDelegate
