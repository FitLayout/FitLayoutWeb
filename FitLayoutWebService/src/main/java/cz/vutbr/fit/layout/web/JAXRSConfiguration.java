
package cz.vutbr.fit.layout.web;

import javax.annotation.security.DeclareRoles;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.eclipse.microprofile.auth.LoginConfig;

/**
 *
 * @author burgetr
 */
@ApplicationPath("api")
@LoginConfig(authMethod = "MP-JWT")
@DeclareRoles({ "user", "admin" })
public class JAXRSConfiguration extends Application
{

}
