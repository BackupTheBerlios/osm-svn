/*
 * Copyright 2005 Stephen J. McConnell.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dpml.depot.prefs;

import java.net.Authenticator; 
import java.net.PasswordAuthentication;

/** 
 * The PasswordAuthenticationModel is a interface through which
 * a PasswordAuthentication instance may be retired.
 */
public interface PasswordAuthenticationModel
{   
   /**
    * Return the password authentication instance.
    * @return the authentication credentials
    */
    public PasswordAuthentication getAuthentication(); 


}

