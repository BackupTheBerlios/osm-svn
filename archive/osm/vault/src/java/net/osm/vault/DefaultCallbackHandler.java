/*
 * @(#)DefaultCallbackHandler.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 6/02/2001
 */

package net.osm.vault;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.ConfirmationCallback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;

/**
 * Callback handler to process an authentication and pricipal
 * establishment responce sequence based on a supplied configuration
 * file.  The configuration must contains attribute value corresponding 
 * the the values of 'alias' and 'challenge'.
 * @see javax.security.auth.callback
 */
final class DefaultCallbackHandler extends AbstractLogEnabled
implements CallbackHandler 
{

    private VaultContext m_context;

    //======================================================
    // Constructor
    //======================================================

   /**
    * Creates a callback handler based on a supplied configuration.
    * @param config the configuration
    */
    public DefaultCallbackHandler( VaultContext context ) 
    { 
        m_context = context;
    }

    /**
     * Handles the specified set of callbacks.
     *
     * @param callbacks the callbacks to handle
     * @exception UnsupportedCallbackException if the callback is not an
     * instance  of NameCallback or PasswordCallback
     */
    public void handle( Callback[] callbacks ) throws UnsupportedCallbackException
    {
	  for (int i = 0; i < callbacks.length; i++) 
        {
	      if (callbacks[i] instanceof TextOutputCallback) 
            {
		    TextOutputCallback tc = (TextOutputCallback) callbacks[i];
		    switch (tc.getMessageType()) 
                {
		      case TextOutputCallback.INFORMATION:
			  if( getLogger().isDebugEnabled() ) getLogger().debug( tc.getMessage() );
		        break;
		      case TextOutputCallback.WARNING:
			  if( getLogger().isWarnEnabled() ) getLogger().warn( tc.getMessage() );
		        break;
		      case TextOutputCallback.ERROR:
			  if( getLogger().isErrorEnabled() ) getLogger().error( tc.getMessage() );
		        break;
		      default:
		        throw new UnsupportedCallbackException(
			    callbacks[i], "Unrecognized message type");
		    }
	      }
            else if (callbacks[i] instanceof NameCallback) 
            {
		    try
	          {
 		        final NameCallback nc = (NameCallback) callbacks[i];
		        nc.setName( m_context.getAlias() );
		    }
	          catch( Exception e )
		    {
			  final String error = "failed to resolve alias from context";
		        throw new RuntimeException( error, e );
		    }
 	      } 
            else if (callbacks[i] instanceof PasswordCallback) 
            {
		    try
	          {
 		        final PasswordCallback pc = (PasswordCallback) callbacks[i];
		        pc.setPassword( m_context.getChallenge().toCharArray());
		    }
	          catch( Exception e )
		    {
			  final String error = "failed to resolve challenge from context";
		        throw new RuntimeException( error, e );
		    }
 	      } 
            else if (callbacks[i] instanceof ConfirmationCallback) 
            {
		    // ignore
	      } 
            else 
	      {
 		    throw new UnsupportedCallbackException(
	          callbacks[i], "Unsupported Callback");
            }
        }
    }
}
