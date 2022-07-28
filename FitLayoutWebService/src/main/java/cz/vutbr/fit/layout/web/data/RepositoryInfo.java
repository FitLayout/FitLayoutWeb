/**
 * RepositoryInfo.java
 *
 * Created on 15. 4. 2021, 20:41:52 by burgetr
 */
package cz.vutbr.fit.layout.web.data;

import java.util.Date;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Basic repository information transferred via the API.
 * 
 * @author burgetr
 */
@Schema(name="RepositoryInfo", description="Information about an artifact repository")
public class RepositoryInfo
{
    @Schema(description="Repository ID", required = true)
    public String id;
    @Schema(description="Repository description", required = false)
    public String description;
    @Schema(description="Metadata schema version", required = false)
    public String version;
    @Schema(description="Creator's e-mail", required = false)
    public String email;
    @Schema(description="Owner ID", required = true)
    public String owner;
    @Schema(description="Creation date", required = false)
    public Date createdOn;
    @Schema(description="Last access date", required = false)
    public Date accessedOn;
    @Schema(description="Expiration date", required = false)
    public Date expires;
    @Schema(description="Read only flag", required = false)
    public Boolean readOnly = false;
    
    
    public RepositoryInfo()
    {
    }

    public RepositoryInfo(String id, String description)
    {
        this.id = id;
        this.description = description;
    }

    public RepositoryInfo(RepositoryInfo src)
    {
        id = src.id;
        description = src.description;
        version = src.version;
        email = src.email;
        owner = src.owner;
        createdOn = src.createdOn;
        accessedOn = src.accessedOn;
        expires = src.expires;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public Date getCreatedOn()
    {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn)
    {
        this.createdOn = createdOn;
    }

    public Date getAccessedOn()
    {
        return accessedOn;
    }

    public void setAccessedOn(Date accessedOn)
    {
        this.accessedOn = accessedOn;
    }

    public Date getExpires()
    {
        return expires;
    }

    public void setExpires(Date expires)
    {
        this.expires = expires;
    }

    public String getOwner()
    {
        return owner;
    }

    public void setOwner(String owner)
    {
        this.owner = owner;
    }
    
    public Boolean getReadOnly()
    {
        return readOnly;
    }

    public void setReadOnly(Boolean readOnly)
    {
        this.readOnly = readOnly;
    }

    public void updateWith(RepositoryInfo other)
    {
        setDescription(other.getDescription());
        setEmail(other.getEmail());
        setReadOnly(other.getReadOnly() != null && other.getReadOnly() == true);
    }
    
}
