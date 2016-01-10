package org.grails.jaxrs

import grails.core.ArtefactHandlerAdapter
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 * @author Noam Y. Tenne
 */
class ResourceArtefactHandler extends ArtefactHandlerAdapter {

    private static final Log LOG = LogFactory.getLog(ResourceArtefactHandler)

    public static final String TYPE = 'Resource'

    public ResourceArtefactHandler() {
        super(TYPE, GrailsResourceClass, DefaultGrailsResourceClass, TYPE)
    }

    /**
     * Returns <code>true</code> if the <code>clazz</code> contains at least one
     * JAX-RS annotation on class-level or method-level and none of the provider
     * interfaces is implemented.
     *
     * @param clazz
     * @<code>true</code> if the class is a JAX-RS resource.
     */
    @Override
    public boolean isArtefactClass(Class clazz) {
        if (clazz == null) {
            false
        }
        boolean match = JaxrsClasses.isJaxrsResource(clazz)
        if (match) {
            LOG.info("Detected JAX-RS resource: ${clazz.name}")
        }
        match
    }
}
