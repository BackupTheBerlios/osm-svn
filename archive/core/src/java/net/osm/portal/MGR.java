/*
 * @(#)MGR.java
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Vector;
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.util.Date;
import java.util.jar.JarFile;

import org.omg.CORBA.ORB;
import net.osm.discovery.*;
import net.osm.discovery.LogicalOperator;

import net.osm.portal.util.*;
import net.osm.util.IOR;


public class MGR {

    private static Portal portal;
    private static ORB orb;
    private static Vector v;
    private static final String service = "DiscoveryService";
    private static final String delimiter = "/";

    private static SelectionSet selectionSet;
    private static Description description;

    private static boolean list = true;
    private static boolean summary = false;
    private static boolean locate = false;
    private static boolean find = true;
    private static boolean depend = false;
    private static String desc;
    private static String title;
    private static Vector features = new Vector();
    private static Vector keys = new Vector();
    private static int policy = -1;
    private static Date after;
    private static String PORTAL_PATH = "portal.ior";

    public static void main(String[] args) {

	  for( int i = 0; i < args.length; i++ ) {
	      if( args[i].equals("-list") ) {
		    list = true;
		    summary = false;
	      } else if( args[i].equals("-brief") ) {
		    summary = true;
		    list = false;
	      } else if( args[i].equals("-locate") ) {
		    locate = true;
		    find = false;
	      } else if( args[i].equals("-find") ) {
		    find = true;
		    locate = false;
	      } else if( args[i].equals("-depend") ) {
		    depend = true;
	      } else if( args[i].equals("-nodepend") ) {
		    depend = false;
	      } else if( args[i].equals("-title") ) {
		    title = args[i+1];
		    i++;
	      } else if( args[i].equals("-desc") ) {
		    desc = args[i+1];
		    i++;
	      } else if( args[i].equals("-portal") ) {
		    PORTAL_PATH = args[i+1];
		    i++;
	      } else if( args[i].equals("-feature") ) {
		    features.add( args[i+1] );
		    i++;
	      } else if( args[i].equals("-key") ) {
		    keys.add( args[i+1] );
		    i++;
	      } else if( args[i].equals("-policy") ) {
		    String p = args[i+1].toLowerCase();
		    if( p.startsWith("u")) {
			policy = 0;
		    } else if( p.startsWith("rep")) {
			policy = 1;
		    } else if( p.startsWith("ref")) {
			policy = 2;
		    } else {
			System.err.println(
				"unknown policy argument '" + args[i] + 
				"' use one of UNKNOWN, REPLICATION or REFERRAL"
			);
			return;
		    }
		    i++;
	      } else if( args[i].equals("-after") ) {
		    try{
			  SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
		        after = df.parse( args[i+1] );
		        i++;
		    } catch (Exception e) {
			  System.out.println(e.getMessage());
			  System.out.println("format: dd-mmm=yyyy");
			  return;
		    }
	      } else if( args[i].equals("-help") ) {
		    System.out.println( printHelp());
		    return;
		}
        }

	  File portalFile = new File( PORTAL_PATH );
        try 
	  {
		ORB orb = ORB.init(args, null);
		PortalSingleton.init( orb );
            portal = PortalHelper.narrow( IOR.readIOR( orb, PORTAL_PATH ) );
        } 
	  catch (FileNotFoundException ex) 
	  {
		System.out.println("\n\tPROBLEM: Portal IOR file not found. " + portalFile.getAbsolutePath() );
		return;
        } 
	  catch (Exception ex) 
        {
		System.out.println("\n\tPROBLEM: " + ex + "\n");
	      ex.printStackTrace();
		return;
        }

	  try{

		// create a filter and resolve against directory
	      CompositeFilter f = new CompositeFilter( );

		if( title != null ) {
		    f.add( (Filter) new ContentFilter( "title", title ));
		}

		if( desc != null ) {
		    f.add( (Filter) new ContentFilter( "description", desc ));
		}

		int keysLength = keys.size();
		for( int i = 0; i<keysLength; i++){
		    f.newKeyFilter( (String) keys.get(i) );
		}

		int featureLength = features.size();
		for( int i = 0; i<featureLength; i++){
		    f.newFeatureFilter( (String) features.get(i) );
		}

		if( policy > -1 ) {
		    f.add( new ScalarFilter( "policy", policy ));
		}

		if( after != null ) {
		    ScalarFilter sf = new ScalarFilter( "timestamp", new UtcTBase(after) );
		    sf.operator = LogicalOperator.LESS_THAN_OR_EQUAL;
		    f.add( sf );
		}

		if( find ) {

		    // multiple result

		    long t = System.currentTimeMillis();
		    selectionSet = portal.find( f );
		    long tt = System.currentTimeMillis() - t;
		    System.out.println("\n  RESULT: (" + selectionSet.selections.length + ", " + tt + ")\n");
		    for( int i = 0; i < selectionSet.selections.length; i++ ) {
			  if( list ) {

			      // detailed listing

				System.out.println("\n  Resource Description: " + (i+1) );
			      Printer.print("\t",selectionSet.selections[i].artifact );
			      System.out.println("\n\tSCORE: " + selectionSet.selections[i].ranking.ground() + "%" 
					+ " (" + selectionSet.selections[i].ranking + ")" );
			      if( depend ) {
					System.out.println("\n\tDependencies: \n");
					dependency(
						"\t", (Description) selectionSet.selections[i].artifact  );
				}

			  } else {

			      // summary listing

				Description d = (Description) selectionSet.selections[i].artifact;
				System.out.println( "\t" + d.title );
			      if( depend ) {
					dependency(
						"\t\t", (Description) selectionSet.selections[i].artifact  );
				}
			  }
		    }
		} else {

		    // -locate option

		    long t = System.currentTimeMillis();
		    description = (Description) portal.locate( f );
		    long tt = System.currentTimeMillis() - t;
		    System.out.println("\n  RESULT: (" + tt + ")\n");
		    if( list ) {

			  // full listing

		        System.out.println("\n  Resource Description");
		        Printer.print("\t",description );
			  if( depend ) {
		 	      System.out.println("\n\tDependencies: \n");
					dependency(
					"\t", description );
			  }
		    } else {

			  // summary listing

			  System.out.println();
			  if( depend ) {
			      System.out.println("\t" + description.title );
				dependency("\t\t", description );
			  } else {
			      System.out.println("\t" + description.title );
			  }
		    }
           }

	  } catch (NoMatch e ) {
        	System.out.println( "\n\tResource not found: " + e + "\n");
	  } catch (Exception e ) {
        	System.out.println( "\n\tUnexpected exception: " + e + "\n");
		e.printStackTrace();
	  }
    }


    public static void dependency( String lead, Description desc ) {

        try{

	      DescriptionBase current = (DescriptionBase) desc;
		while( current != null ) {

		    // print out URL

		    System.out.println( lead + current.resource );

		    // find dependent and make that the current description

		    try{

        	        KeyBase key = (KeyBase) current.lookup("dependency");
			  
			  // The 'dependency' key contains a declaration of a dependecy.
			  // The value id defined under a feature contained in the dependecy
			  // key.  The feature is named 'reference'.  

			  try{

			      Feature reference = (Feature) key.lookup("reference");

			      // The value of 'reference' feature could be an IDL dependency 
			      // or a product key. If the string returned from the 'reference' 
			      // feature starts with "IDL:" then it is an IDL dependecy 
			      // otherwise it is product key reference.

				if( reference.value.indexOf("IDL:") < 0) {

				    // we have a product key declaration

	      		    CompositeFilter f = new CompositeFilter( );
	      		    f.newKeyFilter(reference.value);

				    try{
				        current = (DescriptionBase) portal.locate( f );
				    } catch (NoMatch nf) {
					  System.out.println( lead + reference.value );
					  current = null;
				    }

				} else {

				    // We have an IDL dependency delaration.  We need to 
				    // convert this IDL type identifier to a path sequence
				    // and a feature containing the version then look this 
				    // up.  Because the OMG use the sequence "...*.omg.org/*/..." 
				    // for all formal specifications, we need to convert
				    // this to "org/omg/*.../*/..."

				    String idlPath = idlToKeyPath( reference.value );
				    String idlVersion = idlToVersion( reference.value );
	      		    CompositeFilter f = new CompositeFilter( );
	      		    CompositeFilter last = (CompositeFilter) f.newKeyFilter( idlPath );
	      		    //last.newFeatureFilter( "version", idlVersion );

				    try{
				        current = (DescriptionBase) portal.locate( f );
				    } catch (NoMatch nf) {
					  System.out.println( "\n" + lead + "\n" + "Unresolved:\n" );
					  System.out.println( lead + reference.value );
					  System.out.println( lead + idlPath + "/version:" + idlVersion );
					  current = null;
				    } catch (Exception nf) {
					  nf.printStackTrace();
				    }

				} // end if

			  } catch (KeywordNotFound nfe ) {

				// The reference key is required part of a dependency 
			      // declaration.  If this is not found, then the jar file
				// was badly defined.

				current = null;
				System.out.println(
					"\t=The 'dependency' key does not contain the required " +
					"'reference' feature.");
			  }

 
		    } catch (KeywordNotFound knf) {
			  
			  // No dependencies declared in this description.
			  // Its probably a service or client application.

			  current = null;

		    }

		} // end while

	  } catch (Exception e ) {
        	System.out.println( "\n\tUnexpected exception: " + e + "\n");
		e.printStackTrace();
	  }
    }

    private static String idlToKeyPath( String path ) {
	  int n = path.indexOf("IDL:");
	  if( n < 0 ) return path;
	  int m = path.indexOf('/');
	  String dot = path.substring( 4, m );
	  dot = invert( dot );
	  int v = path.lastIndexOf(":");
	  String mod = path.substring( m + 1, v);
	  return append( dot, mod, delimiter );
    }

    private static String idlToVersion( String idl ) {
	  return idl.substring( idl.lastIndexOf(":") + 1, idl.length());
    }

    private static String invert( String string ) {
	String result = "";
	String subject = string;
	int i = subject.indexOf(".");
	while ( i > -1 ) {
	    String s = subject.substring(0,i);
	    result = append( s, result, delimiter );
	    subject = subject.substring(i+1, subject.length());
	    i = subject.indexOf(".");
	}
	return append( subject, result, delimiter);
    }

    private static String append( String a, String b, String del ) {
	  if( a.length() == 0 ) {
		return b;
	  } else {
		if( b.length() == 0) {
		    return a;
		} else {
		    return a + del + b;
		}
	  }
    }

    private static String printHelp() {

	  return "\n"
		+ "\t-key"
		+ "\t  Key path argument such as 'net/osm/hub'.\n"
		+ "\t\t  Can contain '*' wildcard or '**' wildcard. Multiple\n"
		+ "\t\t  occcurance of -key are allowed.\n"
		+ "\n"
		+ "\t-list"
		+ "\t  Resource descriptions corresponding to the selection\n"
		+ "\t\t  criteria are listed in full (default)\n"
		+ "\n"
		+ "\t-brief"
		+ "\t  Resource descriptions corresponding to the selection\n"
		+ "\t\t  criteria are summarised.\n"
		+ "\n"
		+ "\t-locate"
		+ "\t  Returns at most one occurance of resource.\n"
		+ "\n"
		+ "\t-find"
		+ "\t  Returns at multiple occurance matching resource selection\n"
		+ "\t\t  criteria.\n"
		+ "\n"
		+ "\t-depend"
		+ "\t  Returns pricipal and dependent resources.\n"
		+ "\n"
		+ "\t-title"
		+ "\t  Locate resources with partial matching against resource title.\n"
		+ "\n"
		+ "\t-desc"
		+ "\t  Locate resources with partial matching against resource description.\n"
		+ "\n"
		+ "\t-feature"
		+ "\t\n"
		+ "\n"
		+ "\t-key"
		+ "\t\n"
		+ "\n"
		+ "\t-policy"
		+ "\t\n"
		+ "\n"
		+ "\t-after"
		+ "\t\n"
		+ "\n";
    }

}


