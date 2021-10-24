/**
 * StorageStatus.java
 *
 * Created on 16. 4. 2021, 11:00:19 by burgetr
 */
package cz.vutbr.fit.layout.web.data;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * The storage service status.
 * 
 * @author burgetr
 */
@Schema(name = "StorageStatus", description = "Overall storage status")
public class StorageStatus
{
    /** Single mode or multi mode? */
    public boolean singleMode;
    /** Can the user create repositories? */
    public boolean createAvailable;
    /** Number of existing repositories */
    public int repositories;
    /** Number of available repositories for creating */
    public int available;
    
    public StorageStatus()
    {
    }

    public StorageStatus(boolean singleMode, boolean createAvailable,
            int repositories, int available)
    {
        this.singleMode = singleMode;
        this.createAvailable = createAvailable;
        this.repositories = repositories;
        this.available = available;
    }

    public boolean isSingleMode()
    {
        return singleMode;
    }

    public void setSingleMode(boolean singleMode)
    {
        this.singleMode = singleMode;
    }

    public boolean isCreateAvailable()
    {
        return createAvailable;
    }

    public void setCreateAvailable(boolean createAvailable)
    {
        this.createAvailable = createAvailable;
    }

    public int getRepositories()
    {
        return repositories;
    }

    public void setRepositories(int repositories)
    {
        this.repositories = repositories;
    }

    public int getAvailable()
    {
        return available;
    }

    public void setAvailable(int available)
    {
        this.available = available;
    }
    
}
