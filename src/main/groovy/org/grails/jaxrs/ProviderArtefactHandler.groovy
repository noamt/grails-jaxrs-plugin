package org.grails.jaxrs

import grails.core.ArtefactHandlerAdapter
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 * @author Noam Y. Tenne
 */
class ProviderArtefactHandler extends ArtefactHandlerAdapter {

    private static final Log LOG = LogFactory.getLog(ProviderArtefactHandler)

    public static final String TYPE = 'Provider'

    public ProviderArtefactHandler() {
        super(TYPE, GrailsProviderClass, DefaultGrailsProviderClass, TYPE)
    }

    /**
     * Returns <code>true</code> if the <code>clazz</code> either implements
     * {@link javax.ws.rs.ext.MessageBodyReader}, {@link javax.ws.rs.ext.MessageBodyWriter} or
     * {@link javax.ws.rs.ext.ExceptionMapper}.
     *
     * @param clazz
     * @<code>true</code> if the class is a JAX-RS provider.
     */
    @Override
    public boolean isArtefactClass(Class clazz) {
        if (clazz == null) {
            false
        }
        boolean match = JaxrsClasses.isJaxrsProvider(clazz)
        if (match) {
            LOG.info("Detected JAX-RS provider: ${clazz.name}")
        }
        match
    }
}
