/* 
 * XWEB.java
 */

package net.osm.xweb;

import java.io.*;
import java.net.URL;
import java.text.*;
import java.util.*;
import java.util.Enumeration;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogKitLogger;
import org.apache.avalon.phoenix.frontends.PhoenixServlet;
import org.apache.avalon.phoenix.components.embeddor.SingleAppEmbeddor;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.phoenix.interfaces.Embeddor;
import org.apache.log.output.ServletOutputLogTarget;
import org.apache.log.Hierarchy;

import org.omg.CORBA.ORB;
import org.omg.Session.AbstractResource;

import net.osm.agent.Agent;
import net.osm.agent.AgentServer;
import net.osm.agent.AgentService;
import net.osm.audit.Audit;
import net.osm.audit.AuditService;
import net.osm.hub.home.Finder;
import net.osm.hub.home.FinderHelper;
import net.osm.hub.home.ResourceFactory;
import net.osm.hub.home.ResourceFactoryHelper;
import net.osm.util.IOR;
import net.osm.vault.Vault;
import net.osm.vault.LocalVault;

/**
 * The XWEB class implements an HttpServlet supporting the 
 * configuration and initialization of subsidary services including 
 * event auditing mechanisms, remote object adapters, and local 
 * business agents.
 *
 * @author Stephen McConnell
 */

public class XWEB extends PhoenixServlet implements Controls
{

   /**
    * Constant string identifier of the application.
    */
    public static final String IDENTIFIER = "xweb";

   /**
    * The default configuration path if not declared in the web.xml file.
    */
    private static final String defaultConfigurationPath = "/WEB-INF/xweb.xml";

   /**
    * Rreference to the ServletContext containing initialization arguments.
    */
    private ServletContext servletContext;

   /**
    * Factory finder reference.
    */
    private static Finder finder;

   /**
    * Agent Server.
    */
    private static AgentServer server;

   /**
    * The embedded application.
    */
    SingleAppEmbeddor embeddor;


    //=========================================================================
    // Methods
    //=========================================================================

   /**
    * The <code>init</code> method invoked by the servlet loader.  The 
    * implementation resolves the configuration file for the servlet and executes  
    * server configuration, initialization and statrup consistent with the Avalon 
    * lifecycle patterns.
    */

    public void init( ServletConfig conf )
    throws ServletException 
    {

        System.out.println("XWEB initialization");
        try
	  {
            super.init(conf);
	      this.servletContext = conf.getServletContext();
		embeddor = (SingleAppEmbeddor)getServletContext().getAttribute( Embeddor.ROLE );

		System.out.println("EMBEDDOR: " + embeddor );

		//String[] list = embeddor.list();
		//for( int i=0; i<list.length; i++)
		//{
		//    Component c = embeddor.lookup( list[i] );
		//    System.out.println("COMPONENT: " + c );
		//}
        }
	  catch( Throwable e )
	  {
		System.out.println("XWEB INITILIZATION ERROR");
	      e.printStackTrace();
	  }

        servletContext.setAttribute("xweb", this );
        System.out.println("XWEB bootstrap commencing.");
    }

    // ======================================================================
    // Control operations
    // ======================================================================


   /**
    * Returns the current ORB.
    * @return ORB
    */
    public ORB getOrb()
    {
	  return getAgentService().getOrb();
    }

   /**
    * Returns the agent service.
    * @return AgentService
    */
    public AgentService getAgentService()
    {
        try
	  {
	      //System.out.println("AGENT SERVICE: " + embeddor.lookup("agent"));
            return (AgentService) embeddor.lookup("agent");
        }
	  catch( Throwable t )
	  {
		final String error = "lookup of the XWEB agent service failed";
	      throw new RuntimeException( error, t );
	  }
    }

   /**
    * Returns a factory finder reference.
    */
    public Finder getFinder()
    {
	  return getAgentService().getFinder();
    }

   /**
    * Returns the root community as an agent.
    */
    public Agent getRoot()
    {
	  return getAgentService().getRoot();
    }

}
