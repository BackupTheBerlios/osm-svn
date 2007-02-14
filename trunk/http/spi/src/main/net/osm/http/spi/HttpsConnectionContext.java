/*
 * Copyright 2006 Stephen McConnell.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.osm.http.spi;

import java.net.URI;

import net.dpml.annotation.Context;

/**
 * HTTPS connection context.
  <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
@Context
public interface HttpsConnectionContext extends ConnectionContext
{
   /**
    * Return the keystore password.
    * @param password implementation defined default value
    * @return the supplied value unless overriden in the deployment configuration
    */
    String getKeyStorePassword( String password );
    
   /**
    * Return the certificate password.
    * @param password implementation defined default value
    * @return the supplied value unless overriden in the deployment configuration
    */
    String getCertificatePassword( String password );
    
   /**
    * Return the keystore algorithm.
    * @param algorithm implementation defined default value
    * @return the supplied value unless overriden in the deployment configuration
    */
    String getAlgorithm( String algorithm );
    
   /**
    * Return the keystore type.
    * @param type implementation defined default value
    * @return the supplied value unless overriden in the deployment configuration
    */
    String getKeyStoreType( String type );
    
   /**
    * Return the SSL protocol.
    * @param protocol implementation defined default value
    * @return the supplied value unless overriden in the deployment configuration
    */
    String getProtocol( String protocol );
    
   /**
    * Return the keystore location uri.
    * @param keystore implementation defined default value
    * @return the supplied value unless overriden in the deployment configuration
    */
    URI getKeyStore( URI keystore );
    
   /**
    * Return the 'want-client-authentication' policy.
    * @param flag implementation defined default value
    * @return the supplied value unless overriden in the deployment configuration
    */
    boolean getWantClientAuth( boolean flag );
    
   /**
    * Return the 'need-client-authentication' policy.
    * @param flag implementation defined default value
    * @return the supplied value unless overriden in the deployment configuration
    */
    boolean getNeedClientAuth( boolean flag );
    
   /**
    * Return the SSL context provider.
    * @param provider implementation defined default value
    * @return the supplied value unless overriden in the deployment configuration
    */
    String getProvider( String provider );
    
   /**
    * Return the keystore algorithm.
    * @param algorithm implementation defined default value
    * @return the supplied value unless overriden in the deployment configuration
    */
    String getTrustAlgorithm( String algorithm );
    
   /**
    * Return the keystore location uri.
    * @param uri implementation defined default value
    * @return the supplied value unless overriden in the deployment configuration
    */
    URI getTrustStore( URI uri );
    
   /**
    * Return the keystore type.
    * @param type implementation defined default value
    * @return the supplied value unless overriden in the deployment configuration
    */
    String getTrustStoreType( String type );
    
   /**
    * Return the trust store password.
    * @param password implementation defined default value
    * @return the supplied value unless overriden in the deployment configuration
    */
    String getTrustStorePassword( String password );
    
   /**
    * Set the cipher suites.
    * @param suites the default suites argument
    * @return the cipher suites
    */
    //String[] getCipherSuites( String[] suites );
}
