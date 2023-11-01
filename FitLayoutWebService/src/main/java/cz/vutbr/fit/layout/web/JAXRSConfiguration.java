
package cz.vutbr.fit.layout.web;

import javax.annotation.security.DeclareRoles;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.eclipse.microprofile.auth.LoginConfig;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;

/**
 *
 * @author burgetr
 */
@ApplicationPath("api")
@LoginConfig(authMethod = "MP-JWT")
@DeclareRoles({ "user", "admin" })
@OpenAPIDefinition(
    info = @Info(title = "FitLayout REST API",
                    version = "1.0.0",
                    description = "FitLayout API for artifact creation, storage and manipulation",
                    contact = @Contact(url = "https://github.com/FitLayout/FitLayout/wiki",
                    email = "burgetr@fit.vut.cz")),
    security = @SecurityRequirement(name = "jwt", scopes = {}))
@SecurityScheme(securitySchemeName = "jwt", type = SecuritySchemeType.HTTP,
    description = "JWT authentication with bearer token",
    scheme = "bearer", bearerFormat = "jwt")
public class JAXRSConfiguration extends Application
{

}
