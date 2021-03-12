/**
 * FLConfig.java
 *
 * Created on 3. 10. 2020, 23:09:57 by burgetr
 */
package cz.vutbr.fit.layout.web;

import cz.vutbr.fit.layout.api.ServiceManager;
import cz.vutbr.fit.layout.bcs.BCSProvider;
import cz.vutbr.fit.layout.cssbox.CSSBoxTreeProvider;
import cz.vutbr.fit.layout.puppeteer.PuppeteerTreeProvider;
import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.segm.BasicSegmProvider;
import cz.vutbr.fit.layout.segm.op.CollapseAreasOperator;
import cz.vutbr.fit.layout.segm.op.FindLineOperator;
import cz.vutbr.fit.layout.segm.op.FlattenTreeOperator;
import cz.vutbr.fit.layout.segm.op.GroupByDOMOperator;
import cz.vutbr.fit.layout.segm.op.HomogeneousLeafOperator;
import cz.vutbr.fit.layout.segm.op.MultiLineOperator;
import cz.vutbr.fit.layout.segm.op.SortByLinesOperator;
import cz.vutbr.fit.layout.segm.op.SortByPositionOperator;
import cz.vutbr.fit.layout.segm.op.SuperAreaOperator;
import cz.vutbr.fit.layout.vips.VipsProvider;

/**
 * FitLayout configuration utilities.
 * 
 * @author burgetr
 */
public class FLConfig
{
    
    /**
     * Creates and configures a FitLayout ServiceManager instance.
     * @param repo the artifact repository to be used by the service manager or {@code null} when
     * no repository should be configured.
     * @return the created ServiceManager instance
     */
    public static ServiceManager createServiceManager(RDFArtifactRepository repo)
    {
        //initialize the services
        ServiceManager sm = ServiceManager.create();
        
        //renderers
        sm.addArtifactService(new CSSBoxTreeProvider());
        sm.addArtifactService(new PuppeteerTreeProvider());
        
        //segmentation
        sm.addArtifactService(new BasicSegmProvider());
        sm.addArtifactService(new VipsProvider());
        sm.addArtifactService(new BCSProvider());
        
        //operators
        sm.addAreaTreeOperator(new CollapseAreasOperator());
        sm.addAreaTreeOperator(new FindLineOperator());
        sm.addAreaTreeOperator(new FlattenTreeOperator());
        sm.addAreaTreeOperator(new MultiLineOperator());
        sm.addAreaTreeOperator(new SortByPositionOperator());
        sm.addAreaTreeOperator(new SortByLinesOperator());
        sm.addAreaTreeOperator(new SuperAreaOperator());
        sm.addAreaTreeOperator(new GroupByDOMOperator());
        sm.addAreaTreeOperator(new HomogeneousLeafOperator());
        
        //use RDF storage as the artifact repository
        if (repo != null)
            sm.setArtifactRepository(repo);
        return sm;
    }

}
