/*
 * @(#)SelectionSetBase.java
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

import java.util.Random;
import java.util.Vector;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA_2_3.portable.InputStream;
import net.osm.discovery.SelectionSet;
import net.osm.discovery.Selection;
import net.osm.discovery.Identifier;
import net.osm.discovery.Filter;
import net.osm.discovery.NoMatch;
import net.osm.discovery.InvalidFilter;
import net.osm.discovery.Content;
import net.osm.discovery.Description;
import net.osm.discovery.Score;
import net.osm.discovery.Artifact;

/**
ValueFactory for SelectionSet 
*/

public class SelectionSetBase extends SelectionSet implements ValueFactory 
{

    private static final Random random = new Random();
  
    // constructors

    public SelectionSetBase ( ){}

    public SelectionSetBase ( Selection[] selections ){
	  this.id = new IdentifierBase( random );
	  this.selections = selections;
    }

    public SelectionSetBase ( Identifier id, Selection[] selections ){
	  this.id = id;
	  this.selections = selections;
    }

    // operations from ValueFactory

    public java.io.Serializable read_value( InputStream is ){
	  return is.read_value( new SelectionSetBase() );
    }

    // operations from Directory

    public Artifact locate( Filter filter ) throws NoMatch, InvalidFilter {
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
        } catch (Exception e) {
		System.err.println("SelectionSet/Exception: " + e );
		e.printStackTrace();
	  }
	  return best.artifact;
    }

    /**
    The find operation returns a selection set based on the supplied filter argument.
    */

    public SelectionSet find( Filter filter ) throws NoMatch, InvalidFilter {

	  Vector result = new Vector();

        try{
		for( int i = 0; i < this.selections.length; i++){
		    Selection sel = (Selection) this.selections[i];
		    if( sel.artifact instanceof Description ) {
		        Description r = (Description) sel.artifact;
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
		System.err.println("SelectionSet.find: exception:");
		e.printStackTrace();
        } finally {
	      if( result.size() == 0) throw new NoMatch();
        }
	  // convert selections vector to an array and pass back a SelectionSet
	  return new SelectionSetBase( (Selection[]) result.toArray( new Selection[0]) );
    }


} // SelectionSetBase
