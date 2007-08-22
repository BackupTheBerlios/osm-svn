/*
 * @(#)XMLFilenameFilter.java
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

import java.io.File;
import java.io.FilenameFilter;


/**
 * Utility supporting the filtering of files with an ".xml" suffix.
 */

public class XMLFilenameFilter 
implements FilenameFilter
{

    public XMLFilenameFilter()
    {
    }

   /**
    * Returns true if the supplied filename ends with ".xml".
    */

    public boolean accept( File file, String name )
    {
       return name.endsWith(".xml");
    }
}
