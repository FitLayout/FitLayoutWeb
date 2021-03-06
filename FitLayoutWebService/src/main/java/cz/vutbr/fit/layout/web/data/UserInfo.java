/**
 * UserInfo.java
 *
 * Created on 16. 4. 2021, 18:40:11 by burgetr
 */
package cz.vutbr.fit.layout.web.data;

import java.security.Principal;

/**
 * 
 * @author burgetr
 */
public class UserInfo
{
    /**
     * The user ID used for an unathorized user. This is not used when the REST endpoind is
     * disabled for anonymous sers.
     */
    public static String GUEST_USER = "guest";

    public String userId;
    public boolean anonymous = true;
    
    public UserInfo()
    {
    }

    public UserInfo(Principal principal)
    {
        if (principal == null || principal.getName() == null || "ANONYMOUS".equals(principal.getName()))
        {
            // unauthorized users
            setUserId(GUEST_USER);
            setAnonymous(true);
        }
        else
        {
            // authorized users
            setUserId(principal.getName());
            setAnonymous(false);
        }
    }
    
    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public boolean isAnonymous()
    {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous)
    {
        this.anonymous = anonymous;
    }
    
}
