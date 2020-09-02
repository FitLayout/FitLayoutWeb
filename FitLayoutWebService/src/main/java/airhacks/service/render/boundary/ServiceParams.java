/**
 * ServiceParams.java
 *
 * Created on 2. 9. 2020, 13:52:29 by burgetr
 */

package airhacks.service.render.boundary;

import java.util.Map;

/**
 * 
 * @author burgetr
 */
public class ServiceParams
{
    private String serviceId;
    private Map<String, Object> params;

    public ServiceParams(String serviceId, Map<String, Object> params)
    {
        this.serviceId = serviceId;
        this.params = params;
    }

    public String getServiceId()
    {
        return serviceId;
    }

    public void setServiceId(String serviceId)
    {
        this.serviceId = serviceId;
    }

    public Map<String, Object> getParams()
    {
        return params;
    }

    public void setParams(Map<String, Object> params)
    {
        this.params = params;
    }

}
