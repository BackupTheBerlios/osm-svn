/*
 * @(#)RegistryDelegate.java
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
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.phoenix.BlockContext;

import net.osm.discovery.Artifact;
import net.osm.discovery.Receipt;
import net.osm.discovery.Disclosure;
import net.osm.discovery.Registry;
import net.osm.discovery.RegistryOperations;
import net.osm.discovery.Description;
import net.osm.discovery.ExistingArtifact;
import net.osm.discovery.RegistrationDenied;
import net.osm.discovery.MallformedArtifact;
import net.osm.discovery.UnknownReceipt;
import net.osm.discovery.MallformedReceipt;
import net.osm.discovery.ArtifactUnavailable;
import net.osm.discovery.RetractionDenied;
import net.osm.discovery.ReceiptClass;
import net.osm.discovery.DisclosurePolicy;
import net.osm.discovery.URI;
import net.osm.portal.util.JarDescription;


/**
 * The RegistryDelegate implementation support the server side 
 * registry actions enabling the registration of an artifiact, 
 * the selection of an artificat given a receipt, and the 
 * modification and retraction of a registered artifact.  This 
 * implementation provides a test framework for service 
 * assessment.
 */
public class RegistryDelegate 
implements RegistryOperations, LogEnabled, Contextualizable, Configurable, Initializable
{

    //============================================================
    // state
    //============================================================

   /**
    * The log channel.
    */
    protected Logger log;

    private static Hashtable receipts = new Hashtable();
    protected static Hashtable index = new Hashtable();

   /**
    * Directory path of the repository.
    */
    private File repository;

   /**
    * Application context
    */
    BlockContext context;


    //============================================================
    // constructor
    //============================================================

    public RegistryDelegate( ) 
    {
    }

    //================================================================
    // Loggable
    //================================================================
    
   /**
    * Sets the logger to be used during configuration, conposition, initialization 
    * and execution phase.
    *
    * @param logger Logger to direct log entries to
    */ 
    public void enableLogging( final Logger logger )
    {
        log = logger;
    }

    //=================================================================
    // Contextualizable
    //=================================================================

    public void contextualize( Context context ) throws ContextException
    {
	  if( context instanceof BlockContext ) 
	  {
	      this.context = (BlockContext) context;
	  }
	  else
	  {
		throw new ContextException("Supplied context does not implement BlockContext.");
	  }
    }

    //================================================================
    // Configurable
    //================================================================
    
   /**
    * Configuration of the runtime environment based on a supplied 
    * Configuration arguments
    * @param config Configuration profile.
    * @exception ConfigurationException if the supplied configuration is 
    *   incomplete or badly formed.
    */
    public void configure( final Configuration config )
    throws ConfigurationException
    {
	  // repository path
	  repository = new File( 
		context.getBaseDirectory(),
            config.getChild("repository").getAttribute( "path", "repository" )
        );
	  log.debug("setting repository to " + repository.getAbsolutePath() );
    }

    //================================================================
    // Initializable
    //================================================================

   /**
    * Initialization is invoked by the framework following configuration.
    * @exception Exception
    */
    public void initialize()
    throws Exception
    {
	  log.debug("initalizing registry" );
	  try
	  {
        	repository.mkdir();
		Vector descriptions = JarDescription.createDescriptionsFromJarFiles( repository );
		Enumeration enum = descriptions.elements();
		while( enum.hasMoreElements() ) 
	      {
		    Description d = (Description)enum.nextElement();
	          this.register( d );
		}
	  } 
	  catch (Exception e) 
        {
		String error = "Unexpected exception while creating descriptions from jar files." +
		  "\n\tsource directory: " + repository.getAbsolutePath();
		log.fatalError( error, e );
		throw new Exception( error, e );
        }
	  log.debug("registry initialization complete" );
    }

    //============================================================
    // Registry
    //============================================================

   /**
    * Registration of an Artifact (Description or Disclosure).  
    * This limited implementation creates a vectors for storing Descriptions
    * and Reciepts.
    * @param artifact the artifact description or disclosure statement to register
    * @exception ExistingArtifact
    * @exception RegistrationDenied
    * @exception MallformedArtifact
    */
    public Receipt register( Artifact artifact ) 
    throws ExistingArtifact, RegistrationDenied, MallformedArtifact 
    {

	  // get artifact id
	  String id = artifact.id.value;

	  // check that an artifact of the same id is not registered

	  Enumeration enum = index.keys();
	  while( enum.hasMoreElements() ) {
		if( id.equals((String) enum.nextElement())) throw new ExistingArtifact();
	  }

	  // add artifact to the index
        this.index.put( id, artifact );

 	  // create and return receipt to client
	  ReceiptBase receipt = null;
	  try{
	  	receipt = new ReceiptBase( ReceiptClass.REGISTRATION, artifact.id );
	      receipts.put( receipt.id.value, receipt );
	  } catch (Exception e) {
	      throw new RegistrationDenied("");
        }
        return receipt;
    }

   /**
    * Return an artifact (Description or Disclosure) based on a supplied receipt.
    */
    public Artifact select( Receipt receipt)
    throws UnknownReceipt, MallformedReceipt, ArtifactUnavailable 
    {
	  try{
		Receipt r = (Receipt) receipts.get( receipt.id.value );
		// should execute receipt.verify() here to make sure the 
		// supplied receipt is genuine
		return (Artifact) this.index.get( r.artifact.value );
	  } catch (Exception e) {
		throw new UnknownReceipt("");
	  }
    }

   /**
    * Replace an existing artifact with another.
    */
    public Receipt update( Receipt receipt,  Artifact artifact)
    throws RegistrationDenied, UnknownReceipt, MallformedReceipt, MallformedArtifact 
    {
	  throw new RegistrationDenied( "no implementation" );
    }

   /**
    * Retract an artifact.
    */
    public Receipt retract( Receipt receipt)
        throws UnknownReceipt, MallformedReceipt, RetractionDenied {
	  throw new RetractionDenied( "no implementation" );
    }

} // RegistryDelegate
