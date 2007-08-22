

package net.osm.pki.process;

import java.io.Serializable;

import org.apache.avalon.excalibur.i18n.ResourceManager;
import org.apache.avalon.excalibur.i18n.Resources;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.activity.Initializable;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Any;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.PortableServer.POA;
import org.omg.CosLifeCycle.NotRemovable;
import org.omg.CollaborationFramework.Processor;
import org.omg.CollaborationFramework.ProcessorHelper;
import org.omg.CollaborationFramework.ProcessorOperations;
import org.omg.CommunityFramework.GenericResource;
import org.omg.CommunityFramework.GenericResourceHelper;
import org.omg.CommunityFramework.GenericCriteria;
import org.omg.Session.Consumes; 
import org.omg.Session.CannotStart; 
import org.omg.Session.AlreadyRunning;
import org.omg.Session.CurrentlySuspended;
import org.omg.Session.CannotStop;
import org.omg.Session.NotRunning;
import org.omg.Session.CannotSuspend;
import org.omg.Session.SystemMessage;
import org.omg.Session.SystemMessageBase;
import org.omg.Session.MessageHeader;
import org.omg.Session.MessageHeaderBase;
import org.omg.Session.MessageBody;
import org.omg.Session.MessageBodyBase;
import org.omg.Session.MessagePriority;
import org.omg.Session.MessageClassification;
import org.omg.PKIAuthority.RegistrationAuthority;
import org.omg.PKIAuthority.RequestCertificateManager;
import org.omg.PKI.CertificateListHolder;
import org.omg.PKI.Continue;
import org.omg.PKI.ContinueHolder;
import org.omg.PKI.PKISuccess;
import org.omg.PKI.PKISuccessWithWarning;
import org.omg.PKI.PKIContinueNeeded;
import org.omg.PKI.PKIFailed;
import org.omg.PKI.PKIPending;
import org.omg.PKI.PKISuccessAfterConfirm;

import net.osm.util.ExceptionHelper;
import net.osm.hub.pss.ProcessorStorage;
import net.osm.hub.gateway.CannotTerminate;
import net.osm.hub.processor.ApplianceContext;
import net.osm.hub.processor.AbstractAppliance;
import net.osm.hub.processor.ProcessorCallback;
import net.osm.hub.processor.AbstractProcessorDelegate;
import net.osm.hub.resource.AbstractResourceDelegate;
import net.osm.hub.generic.GenericResourceService;
import net.osm.pki.authority.RegistrationAuthorityService;
import net.osm.pki.pkcs.PKCS10;
import net.osm.pki.pkcs.PKCS7;
import net.osm.pki.pkcs.PKCS7Base;

/**
 * CertificationAppliance handles execution of a certification request.
 */

class CertificationAppliance extends AbstractAppliance implements Composable, Initializable
{
    //=======================================================================
    // static
    //=======================================================================

    public static final String outputTagName = "response";
    public static final String inputTagName = "request";

    //=======================================================================
    // state
    //=======================================================================

    private RegistrationAuthorityService authority;

    private PKCS10 request;
    private RequestCertificateManager manager;
    private GenericResourceService generic;
    private long tid;
    private String id;

    private static final Resources REZ =
        ResourceManager.getPackageResources( CertificationAppliance.class );

     
    //=======================================================================
    // Composable
    //=======================================================================
    
    /**
     * A hosting processor may provide supplimentary resources to an appliance
     * under a ComponentManager.
     *
     * @param manager the <code>ComponentManager</code>
     */
    public void compose( ComponentManager manager )
    throws ComponentException
    {
        authority = ((RegistrationAuthorityService) manager.lookup("AUTHORITY"));
        generic = ((GenericResourceService)manager.lookup("GENERIC"));
    }

    //=======================================================================
    // Initializable
    //=======================================================================
    
   /**
    * The <code>initialize</code> method will be invoked on completion 
    * log enablement, composition, and contextualization.  An appliance 
    * implemetation is responsible for executing any pre-run initialization
    * actions under this method.
    * @exception Exception if the servant cannot complete normal initialization
    */
    public void initialize()
    throws Exception
    {
        if( authority == null ) throw new IllegalStateException( "null authority" );
        if( generic == null ) throw new IllegalStateException( "null generic service" );
        if( generic == null ) throw new IllegalStateException( "null generic service" );
    }

    //=================================================================
    // Runnable
    //=================================================================

   /**
    * The run method handles the extraction of a PKCS10 certificate reqests
    * and the appication of that request to a PKI registration authority.
    * The implememtation behaviour follwing submissionn of the requerst is 
    * dependent on the result of the request submission.  If supplimentary
    * information is required, the process will suspend and raise a user 
    * message the additional requirements.  A successful conclusion of the 
    * request process will result in the establishment of a PKCS7  
    * certificate contained within a GenericResource.
    */
    public void run()
    {
	  setState( RUNNING );
        if( getLogger().isDebugEnabled() ) getLogger().debug( "run" );

        //
        // get the certification request from the task's input
        //

	  if( request == null ) try
	  {
            final String message = REZ.getString( "pkcs10.log" );
            if( getLogger().isDebugEnabled() ) getLogger().debug( message );
            request = getPKCS10Input();
	  }
	  catch( Throwable e )
	  {
            final String subject = REZ.getString( "pkcs10.subject" );
            if( getLogger().isWarnEnabled() ) getLogger().warn( subject, e );
		final String error = ExceptionHelper.packExceptionAsHTML( subject, e );
	      signalSuspension( 
		  createMessage( MessageClassification.ERROR, subject, error )
		);
		return;
	  }

        if(( getState() == TERMINATED ) || ( getState() == SUSPENDED )) return;

        //
        // get a certificate request manager from the RA initalized with the 
        // certification request
        //

	  if( manager == null ) try
	  {
            final String message = REZ.getString( "manager.log" );
            if( getLogger().isDebugEnabled() ) getLogger().debug( message );
            RegistrationAuthority ra = authority.getRegistrationAuthority();
            manager = ra.request_certificate( request );
	      tid = manager.transaction_ID();
		id = "" + tid;
	  }
	  catch( Throwable e )
	  {
            final String subject = REZ.getString( "manager.subject" );
            if( getLogger().isWarnEnabled() ) getLogger().warn( "manager.subject", e );
		final String error = ExceptionHelper.packExceptionAsHTML( subject, e );
	      signalSuspension( 
		  createMessage( MessageClassification.ERROR, subject, error )
		);
		return;
	  }

        if(( getState() == TERMINATED ) || ( getState() == SUSPENDED )) return;

	  try
	  {
            final String debug = REZ.getString( "status.log", id );
            if( getLogger().isDebugEnabled() ) getLogger().debug( debug );
		CertificateListHolder certificates = new CertificateListHolder( );
		ContinueHolder continuation = new ContinueHolder( );
		int status = manager.get_certificate_request_result( certificates, continuation );

		while( status == PKIPending.value )
	      {
		    try
		    {
		        Thread.currentThread().sleep( 100000 );
			  status = manager.get_certificate_request_result( certificates, continuation );
                    if(( getState() == TERMINATED ) || ( getState() == SUSPENDED )) return;
		    }
		    catch( Throwable x )
		    {
		    }
	      }

		if( status == PKIFailed.value )
		{
		    //
		    // Need to unpack the result and get the reason for the failure
		    //

                final String subject = REZ.getString( "status.failed.subject", id );
                final String message = REZ.getString( "status.failed.message", id );
                if( getLogger().isDebugEnabled() ) getLogger().debug( message );
		    signalCompletion( 
		      createMessage( MessageClassification.ERROR, subject, message )
                );
		    return;
		}
            else if( status == PKIContinueNeeded.value )
	      {
		    //
		    // Need to unpack the result and prepare an input request for 
		    // the supplimentary informaton
		    //
                final String subject = REZ.getString( "status.continue.subject", id );
                final String message = REZ.getString( "status.continue.message", id );
                if( getLogger().isDebugEnabled() ) getLogger().debug( message );
		    signalSuspension( createMessage( MessageClassification.INFORM, subject, message ));
		    return;
	      }
            else if( status == PKISuccessWithWarning.value )
	      {
                final String subject = REZ.getString( "status.warning.subject", id );
                final String message = REZ.getString( "status.warning.message", id );
                if( getLogger().isDebugEnabled() ) getLogger().debug( message );
		    signalCompletion( createMessage( MessageClassification.WARNING, message, subject ));
		    return;
	      }
            else if( status == PKISuccessAfterConfirm.value )
	      {
		    //
		    // Need to issue a confirmation
		    //
                final String subject = REZ.getString( "status.confirm.subject", id );
                final String message = REZ.getString( "status.confirm.message", id );
                if( getLogger().isDebugEnabled() ) getLogger().debug( message );
		    signalSuspension( createMessage( MessageClassification.INFORM, subject, message ));
		    return;
	      }
            else if( status == PKISuccess.value )
	      {
                final String subject = REZ.getString( "status.success.subject", id );
                final String message = REZ.getString( "status.success.message", id );
                if( getLogger().isDebugEnabled() ) getLogger().debug( message );
		    createPKCS7Production( message, continuation.value );

		    //
		    // make sure the manager is destroyed because it is no longer needed
		    //

		    manager._release();
		    authority.getRequestCertificateService().disposeRequestCertificateManager( manager );
		    signalCompletion( createMessage( MessageClassification.INFORM, subject, message ));
		    return;
	      }
        }
	  catch( Throwable throwable )
	  {
            final String error = REZ.getString( "certification.error", id );
            if( getLogger().isWarnEnabled() ) getLogger().warn( error, throwable );
	      signalTermination( 
		  new Exception( error, throwable )
		);
	  }
    }

    private PKCS10 getPKCS10Input()
    {
        try
	  {
            Consumes link = getProcessorCallback().coordinator().get_consumed( inputTagName );
		GenericResource r = GenericResourceHelper.narrow( link.resource() );
	      return (PKCS10) r.value();
	  }
	  catch( Throwable e )
	  {
            final String error = REZ.getString( "pkcs10.error" );
		throw new RuntimeException( error, e );
	  }
    }

    private void createPKCS7Production( String message, Continue value )
    {
	  try
	  {
		//
		// create the criteria argument to the generic resources factory
		//

            final String criteriaName = REZ.getString( "pkcs7.crieria.name" );
		GenericCriteria criteria = new GenericCriteria(
		  "net.osm.pki.certification.response", criteriaName,
		  "IDL:osm.net/pki/pkcs/PKCS7:1.0"
		);

		//
		// create the output generic resource and assign the PKCS7 
		// result as the generic resource value and set the resource
            // as a production result of the coordinating task
		//

            final String name = REZ.getString( "pkcs7.resource.name" );
	      GenericResource output = generic.createGenericResource( 
		  name + "(" + tid + ")", criteria );
		output.set_value( new PKCS7Base( value.getEncoded() ) );
		getProcessorCallback().coordinator().add_produced( output, outputTagName );
	  }
	  catch( Throwable e )
	  {
            final String error = REZ.getString( "pkcs7.error", id );
		throw new RuntimeException( error, e );
	  }
    }
}
