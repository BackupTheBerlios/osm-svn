/*
 * @(#)IconHelper.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 6/02/2001
 */


package net.osm.util;

import javax.swing.ImageIcon;

/**
 *This class provides a set of static utility classes support icon resource loading.
 */

public class IconHelper
{

   /**
    * Return an image icon from a system reosurce.
    * @param c class from which to locate the classloader
    * @param path the reosurce path in the form 'mypackage/image/hello.gif'
    */
   
    public static ImageIcon loadIcon( Class c, String path ) 
    {
	  try
	  {
            if( path == null ) throw new RuntimeException("Null path supplied to loadIcon");
            if( c == null ) throw new RuntimeException("Null class supplied to loadIcon");
	      return new ImageIcon( c.getClassLoader().getResource( path ));
        }
	  catch( Throwable e )
        {
	      throw new RuntimeException("Failed to load resource from path \"" + path + "\"", e );
        }
    }

    public static ImageIcon loadIcon( String path ) 
    {
        return loadIcon( IconHelper.class, path );
    }

}
