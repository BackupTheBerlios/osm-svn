
package net.osm.vault;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Executable;
import org.apache.avalon.framework.CascadingException;
import org.apache.avalon.framework.CascadingRuntimeException;

/**
 * Loads a <code>Vault</code> as a dependent service, invokes a login, and logs 
 * resulting X500 principal name. To execute this demonstration please 
 * follow the instructions concerning login configuration files deailed 
 * under the <code>{@link DefaultVault}</code> class description.
 *
 * <p><table border="1" cellpadding="3" cellspacing="0" width="100%">
 * <tr bgcolor="#ccccff">
 * <td colspan="2"><b>Component Lifecycle</b></td>
 * <tr><td width="20%"><b>Phase</b></td><td><b>Description</b></td></tr>
 * <tr><td width="20%" valign="top">Serviceable</td>
 * <td>A <code>ServiceManager</code> holding the dependent <code>{@link Vault}</code> is supplied 
 * to the component.</td></tr>
 * <tr><td valign="top">Initializable</td>
 * <td>The implementation invokes <code>{@link org.apache.avalon.framework.service.ServiceManager#lookup}</code> 
 * in order to resolve a <code>{@link Vault}</code> against which a 
 * security principal can be established.</td></tr>
 * <tr><td valign="top">Executable</td>
 * <td>Executes a login against the <code>{@link Vault}</code> and logs the resulting principal name.</td></tr>
 * <tr><td width="20%" valign="top">Disposable</td>
 * <td>Cleanup and disposal of state members.</td></tr>
 * </table>
 *
 * @see DefaultVault 
 * @author  Stephen McConnell
 * @version 1.0 20 JUN 2001
 */
public class Demonstration extends AbstractLogEnabled
implements LogEnabled, Serviceable, Initializable, Executable, Disposable
{

    private ServiceManager m_manager;
    private Vault m_vault;

    //=================================================================
    // Serviceable
    //=================================================================

    public void service( ServiceManager manager ) throws ServiceException
    {
	  getLogger().debug("service");
        m_manager = manager;
    }

    //================================================================
    // Initializable
    //================================================================
    
   /**
    * Initialization the demo during which the vault will be resolved 
    * and the current X500 principal listed in the log.
    */
    public void initialize()
    throws Exception
    {
        getLogger().debug("initialization");
        m_vault = (Vault) m_manager.lookup("vault");
    }

    //================================================================
    // Executable
    //================================================================

   /**
    * Initialization the demo during which the vault will be resolved 
    * and the current X500 principal listed in the log.
    */
    public void execute()
    throws Exception
    {
        getLogger().debug("execution");
        m_vault.login();
        getLogger().info( m_vault.getPrincipal().toString() );
    }

    //================================================================
    // Disposable
    //================================================================

   /**
    * Dispose of the value.
    */
    public void dispose()
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug("disposal");
        m_manager.release( m_vault );
        m_manager = null;
        m_vault = null;
    }
}
