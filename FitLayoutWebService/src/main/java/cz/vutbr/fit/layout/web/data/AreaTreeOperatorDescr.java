/**
 * AreaTreeOperatorDescr.java
 *
 * Created on 12. 3. 2021, 18:44:25 by burgetr
 */
package cz.vutbr.fit.layout.web.data;

import cz.vutbr.fit.layout.api.AreaTreeOperator;

/**
 * AreaTreeOperator service description.
 * 
 * @author burgetr
 */
public class AreaTreeOperatorDescr extends ParametrizedServiceDescr
{
    public String category;
    
    public AreaTreeOperatorDescr(AreaTreeOperator service)
    {
        super(service);
        category = service.getCategory();
    }

}
