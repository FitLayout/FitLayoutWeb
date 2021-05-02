/**
 * UserService.java
 *
 * Created on 2. 5. 2021, 10:34:36 by burgetr
 */
package cz.vutbr.fit.layout.web.ejb;

import java.security.Principal;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import cz.vutbr.fit.layout.web.data.UserInfo;

/**
 * 
 * @author burgetr
 */
@RequestScoped
public class UserService
{
    @Inject
    private Principal principal;

    @Inject
    private StorageService storage;

    @Inject
    @ConfigProperty(name = "fitlayout.auth.guestEnabled", defaultValue = "false")
    boolean guestEnabled;
    
    private UserInfo user;
    
    @PostConstruct
    public void init()
    {
        user = new UserInfo(principal);
    }
    
    public boolean isAuthorized()
    {
        if (storage.isSingleMode())
            return true; // no authorization is required when the storage is in sigle mode
        else if (guestEnabled)
            return true; // multi mode but guest user is used when authorization is missing
        else
            return !user.isAnonymous(); // no guest users allowed
    }
    
    public UserInfo getUser()
    {
        return user;
    }
    
}
