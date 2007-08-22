/*
 * @(#)IOR.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 24/02/2001
 */

package net.osm.util;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.URL;
import java.net.URLConnection;

import org.omg.CORBA.ORB;


/**
 * General utilities supporting the reading and writing of an IOR to and from a file.
 */

public class IOR
{
        
   /**
    * Write an IOR to a file.
    */
    
    public static void writeIOR( ORB orb, org.omg.CORBA.Object object, String filename ) throws IOException
    {
        FileOutputStream file = new FileOutputStream( filename );
        PrintWriter pfile = new PrintWriter( file );
        pfile.println( orb.object_to_string( object ));
        pfile.close();
    }
    
   /**
    * Read an IOR from a file.
    */
    
    public static org.omg.CORBA.Object readIOR( ORB orb, String filename ) throws FileNotFoundException, IOException
    {
	  if( filename.indexOf("://") > -1 ) return readIOR( orb, new URL( filename ));

        FileInputStream file = new FileInputStream( filename );
        InputStreamReader input = new InputStreamReader( file );
        BufferedReader reader = new BufferedReader(input);
        String stringTarget = reader.readLine();
        return orb.string_to_object( stringTarget );
    }

   /**
    * Read an IOR from a URL.
    */
 
    public static org.omg.CORBA.Object readIOR( ORB orb, URL url ) throws IOException
    {
        URLConnection connection = url.openConnection();
        InputStream input = connection.getInputStream();
        BufferedReader buffer = new BufferedReader( new InputStreamReader(input) );
        String IOR = buffer.readLine();
        buffer.close();
        return orb.string_to_object( IOR );
    }
}
