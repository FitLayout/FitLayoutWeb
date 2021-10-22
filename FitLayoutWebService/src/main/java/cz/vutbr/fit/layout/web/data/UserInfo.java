/**
 * UserInfo.java
 *
 * Created on 16. 4. 2021, 18:40:11 by burgetr
 */
package cz.vutbr.fit.layout.web.data;

import java.security.Principal;
import java.util.Date;
import java.util.Set;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 
 * @author burgetr
 */
@Schema(name="UserInfo", description="Current user information")
public class UserInfo
{
    /**
     * The user ID used for an unathorized user. This is not used when the REST endpoind is
     * disabled for anonymous sers.
     */
    public static String ANONYMOUS_USER = "guest";

    public String userId;
    public String email;
    public boolean anonymous = true;
    public boolean guest = false;
    @Schema(type = SchemaType.STRING)
    public Date expires = null;
    @Schema(type = SchemaType.ARRAY)
    public Set<String> roles = null;
    
    
    public UserInfo()
    {
    }

    public UserInfo(Principal principal, JsonWebToken jwt)
    {
        if (principal == null || principal.getName() == null || "ANONYMOUS".equals(principal.getName()))
        {
            // unauthorized users
            setUserId(ANONYMOUS_USER);
            setAnonymous(true);
        }
        else
        {
            // authorized users
            setUserId(principal.getName());
            setEmail(jwt.getClaim("email"));
            setAnonymous(false);
        }
        if (jwt != null)
            roles = jwt.getGroups();
    }
    
    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public boolean isAnonymous()
    {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous)
    {
        this.anonymous = anonymous;
    }

    public boolean isGuest()
    {
        return guest;
    }

    public void setGuest(boolean guest)
    {
        this.guest = guest;
    }

    public Date getExpires()
    {
        return expires;
    }

    public void setExpires(Date expires)
    {
        this.expires = expires;
    }
    
}
