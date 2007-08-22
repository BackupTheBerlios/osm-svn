/*
 * @(#)DPML.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 6/02/2001
 */

package net.osm.dpml;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Hashtable;

import org.omg.CommunityFramework.*;
import org.omg.CollaborationFramework.*;

import org.omg.CommunityFramework.UserCriteria;
import org.omg.CommunityFramework.MessageCriteria;

import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.Configurable;

/**
 * The DMPL class provides support for the internalization of a DPML 
 * (Digital Product Modelling Language) XML schema using a DOM object model.
 */

public class DPML {
    
    private Criteria criteria = null;
    private Exception exception = null;
    private static final DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();

    public static Criteria buildCriteria( URL url ) throws ConfigurationException
    {
	  Configuration configuration = null;
	  try
	  {
            configuration = builder.build( url.toString() );
	  }
	  catch( Exception e )
	  {
		throw new ConfigurationException("Failed to load url '" + url + "'.", e );
	  }
        return buildCriteria( configuration );
    }

    public static Criteria buildCriteria( File file ) throws ConfigurationException
    {
	  Configuration configuration = null;
	  try
	  {
            configuration = builder.buildFromFile( file );
	  }
	  catch( Exception e )
	  {
		throw new ConfigurationException("Failed to load file '" + file + "'.", e );
	  }
        return buildCriteria( configuration );
    }

    public static Hashtable buildCriteriaTable( Configuration configuration  ) throws ConfigurationException
    {
	  if( !configuration.getName().toLowerCase().equals("dpml")) throw new ConfigurationException(
          "enclosing element is a DPML element");
        Hashtable table = new Hashtable();
        Configuration[] children = configuration.getChildren();
        for( int i=0; i<children.length; i++ )
        {
            try
		{
		    Criteria c = buildCriteriaElement( children[i] );
		    table.put( c.label, c );
            }
		catch( Exception e )
		{
		    throw new ConfigurationException("unable to build criteria", e );
		}
        }
	  return table;
    }

   /**
    * Creates a single Criteria instance based on the first child element of the supplied configuration.
    * @param configuration the enclosing XML configuration containg a criteria description
    * @return Criteria
    * @exception ConfigurationException
    */
    public static Criteria buildCriteria( Configuration configuration ) throws ConfigurationException
    {
	  Criteria criteria = null;
        Configuration[] children = configuration.getChildren();
	  if( children.length > 0 ) return buildCriteriaElement( children[0] );
	  throw new ConfigurationException( "Supplied configuration instance does not contain a child.");
    }

    public static Criteria buildCriteriaElement( Configuration child ) throws ConfigurationException
    {
	  Criteria criteria = null;
        try
        {
	      String type = child.getName();
	      if( type.equals( "generic" ) )
	      {
		    criteria = new GenericCriteria( child );
		}
		else if( type.equals( "user" ) )
	      {
		    criteria = new UserCriteria( child );
		}
		else if( type.equals( "message" ) )
	      {
		    criteria = new MessageCriteria( child );
		}
		else if( type.equals( "community" ) )
	      {
		    criteria = new CommunityCriteria( child );
		}
		else if( type.equals( "agency" ) )
	      {
		    criteria = new AgencyCriteria( child );
		}
		else if ( type.equals("encounter") ) 
            {
		    criteria = new EncounterCriteria( child );
            }
		else if ( type.equals("processor") | type.equals("engagement") 
		  | type.equals("vote") | type.equals("collaboration") ) 
            {
		    criteria = new ProcessorCriteria( child );
            }
		else if ( type.equals("external") ) 
		{
		    criteria = new ExternalCriteria( child );
            }
		else
	      {
	          criteria = new Criteria( child );
            }
        }
        catch( Exception e )
	  {
            throw new ConfigurationException("Unexpected exception while parsing DPML entry.", e );
        }
        return criteria;
    }
}
