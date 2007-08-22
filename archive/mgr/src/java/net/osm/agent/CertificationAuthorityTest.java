
package net.osm.agent;

import java.awt.event.ActionEvent;
import java.awt.Component;
import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JProgressBar;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import java.security.cert.X509Certificate;

import org.omg.CORBA.ORB;
import org.omg.CommunityFramework.Criteria;
import org.omg.CommunityFramework.GenericResource;
import org.omg.CommunityFramework.GenericResourceHelper;
import org.omg.CollaborationFramework.Processor;
import org.omg.CollaborationFramework.ProcessorHelper;

import org.omg.PKI.CertificateListHolder;
import org.omg.PKI.Continue;
import org.omg.PKI.ContinueHolder;
import org.omg.PKI.PKISuccess;
import org.omg.PKI.PKISuccessWithWarning;
import org.omg.PKI.PKIContinueNeeded;
import org.omg.PKI.PKIFailed;
import org.omg.PKI.PKIPending;
import org.omg.PKI.PKISuccessAfterConfirm;

import net.osm.entity.EntityService;
import net.osm.hub.home.ResourceFactory;
import net.osm.pki.pkcs.PKCS10;
import net.osm.shell.Shell;
import net.osm.shell.Activity;
import net.osm.vault.Vault;
import net.osm.util.IOR;

/**
 * The <code>CertificationRequestActivity</code> handles the establishment
 * of a PKCS10 certification request.  The activity creates a new GenericResource
 * and adds a PKCS10 request as the resource value.
 * @author  Stephen McConnell
 * @version 1.0 23 DEC 2001
 */
class CertificationAuthorityTest extends AbstractAction implements Activity
{

    //==========================================================
    // state
    //==========================================================

   /**
    * The change event listener list.
    */
    private final EventListenerList listeners = new EventListenerList();
    
   /**
    * A change event signalling modification of the activity status.
    */
    private ChangeEvent changeEvent;

   /**
    * The valut against which a PKCS10 instance will be created.
    */
    private final Vault vault;

   /**
    * The principal user agent under which the certification request
    * task will be established.
    */
    private final UserAgent user;

   /**
    * The resource factory that will be used to create the generic 
    * resource containing the certification request value.
    */
    private final ResourceFactory factory;

   /**
    * The entity servicce against which supporting agents can be 
    * created.
    */
    private final EntityService service;

   /**
    * Declaration of a progress monitor will be made to the shell
    * and the shell will handle presentation of progress to the user
    * via the status panel.
    */
    private final Shell shell;

    private boolean indeterminate = true;
    private int maximum = 100;
    private int minimum = 0;
    private int value = 0;
    private Throwable error;
    private ORB orb;

    //==========================================================
    // constructor
    //==========================================================

   /**
    * Creation of a new CertificationRequestActivity.
    * @param shell the shell within which the status of the action will be presented
    * @param vault the vault from which the PKCS10 request will be created
    * @param user the user agent to be assigned as owner of the certification task
    * @param factory the factory on the server for creation of a new generic resource
    *    containing the PKCS10 request
    * @param service the service suppporting creation of new entities wrapping the 
    *    remote resources and tasks
    */
    CertificationAuthorityTest( 
      final String name, final Shell shell, final Vault vault, final UserAgent user, final ResourceFactory factory, final EntityService service, ORB orb ) 
    {
        super( name );
        this.shell = shell;
        this.vault = vault;
        this.user = user;
        this.factory = factory;
        this.service = service;
        this.orb = orb;
        net.osm.pki.authority.RegistrationAuthoritySingleton.init( orb );
        net.osm.pki.base.PKISingleton.init( orb );
        net.osm.pki.pkcs.PKCSSingleton.init( orb );
        net.osm.pki.repository.RepositorySingleton.init( orb );
        setEnabled( true );
    }

    //==========================================================
    // Activity
    //==========================================================

   /**
    * Add a change listener to the activity.
    */
    public void addChangeListener( ChangeListener listener )
    {
        listeners.add( ChangeListener.class, listener );
    }

   /**
    * Remove a change listener from the activity.
    */
    public void removeChangeListener( ChangeListener listener )
    {
        listeners.remove( ChangeListener.class, listener );
    }

   /**
    * Returns the maximum possible value of activity progress.
    */
    public int getMaximum()
    {
        return maximum;
    }

   /**
    * Returns the minimum possible value of activity progress.
    */
    public int getMinimum()
    {
        return minimum;
    }

   /**
    * Returns the current value of activity progress.
    */
    public int getValue()
    {
        return value;
    }

   /**
    * Returns the indeterminate status of the activitity.
    */
    public boolean getIndeterminate()
    {
        return indeterminate;
    }

   /**
    * Returns an exception (normally null) resulting from the 
    * execution of the executable instance.
    */
    public Throwable getError()
    {
        return error;
    }

   /**
    * Creates a thread within which a PKCS10 certificate request is instantiated,
    * placed within a generic resource and assigned as an input argument to a new
    * task owned by the primary user.
    * @exception Exception if the executable instance cannot initialize
    */
    public void execute() throws Exception
    {
	  System.out.println("executing request");
        Thread thread = new Thread(
          new Runnable() 
          {
            public void run()
            {
                doAuthorityTest();
		    notifyCompletion();
            }
          }
        );
        thread.start();
    }

   /**
    * Returns a factory finder reference.
    */
    public void doAuthorityTest()
    {
        org.omg.PKIAuthority.RegistrationAuthority authority = null;
        org.omg.PKIAuthority.RequestCertificateManager manager = null;
	  try
	  {
	      final PKCS10 request = vault.createPKCS10();
            authority = 
		  org.omg.PKIAuthority.RegistrationAuthorityHelper.narrow( 
                IOR.readIOR( orb, "http://home.osm.net/gateway/authority.ior" ) );
		System.out.println( authority.get_provider_info() );
		manager = authority.request_certificate( request );
		CertificateListHolder certificates = new CertificateListHolder( );
		ContinueHolder continuation = new ContinueHolder( );
		int status = manager.get_certificate_request_result( certificates, continuation );

		//
		// check out the response
	      //

		String message = "";
		if( status == PKIFailed.value )
		{
		    //
		    // Need to unpack the result and get the reason for the failure
		    //
		    message = "Certificate request was not sucessful.";
		}
            else if( status == PKIPending.value )
	      {
		    //
		    // Need to set up a thread to monitor the RA
		    //
		    message = "Certificate request in process.";
	      }
            else if( status == PKIContinueNeeded.value )
	      {
		    //
		    // Need to unpack the result and prepare an input request for 
		    // the supplimentary informaton
		    //
		    message = "Certificate request pending supplimentary information.";
	      }
            else if( status == PKISuccessWithWarning.value )
	      {
		    //
		    // Need to update the pricipal but also post a warning message to
		    // the operator
		    //
		    message = "Certificate request successfull (with modifications).";
	      }
            else if( status == PKISuccess.value )
	      {
		    //
		    // Need to update the principal and a post a notification message to
		    // the operator
		    //
		    // NOTE:
		    // ignoring continue type
		    // ignoring content
		    // just grab the certificates
                //

		    message = "Certificate request successfull.";
		    byte[] pkcs7array = continuation.value.getEncoded();
		    System.out.println("CONTINUE: " + pkcs7array );
		    sun.security.pkcs.PKCS7 pkcs7 = new sun.security.pkcs.PKCS7( pkcs7array );
		    System.out.println("PKCS7:\n" + pkcs7 );

		    //sun.security.pkcs.ContentInfo info = pkcs7.getContentInfo();
		    //X509Certificate[] certs = pkcs7.getCertificates();
		    //for( int i=0; i<certs.length; i++ )
                //{
                //    System.out.println("CERTIFICATE:" + i + "\n" + certs[i]);
                //}
	      }
            else if( status == PKISuccessAfterConfirm.value )
	      {
		    //
		    // Need to issue a confirmation
		    //
		    message = "Certificate request pending confirmation.";
	      }
		shell.setMessage( message );

        }
        catch( Exception e )
	  {
		final String error = "test exception";
		throw new RuntimeException( error, e );
        }

    }


    //==========================================================
    // ActionListener
    //==========================================================

   /**
    * Triggers execution of the certificate request under the shell.
    * @param event action event initiating the action
    */
    public void actionPerformed( ActionEvent event )
    {
        shell.execute( this );
    }

    /** 
     * Runs each <code>ChangeListener</code>'s <code>stateChanged</code> method.
     */
    protected void fireStateChanged() 
    {
        Object[] list = listeners.getListenerList();
        for (int i = list.length - 2; i >= 0; i -=2 ) {
            if (list[i] == ChangeListener.class) {
                if (changeEvent == null) {
                    changeEvent = new ChangeEvent(this);
                }
                ((ChangeListener)list[i+1]).stateChanged(changeEvent);
            }          
        }
    }

   /**
    * Set the activities status to indicate completion.
    */
    protected void notifyCompletion()
    {
        value = 100;
        indeterminate = false;
	  fireStateChanged();
    }
}
