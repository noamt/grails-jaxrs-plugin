package grails.plugins.jaxrs.provider

import grails.core.GrailsApplication
import grails.core.support.GrailsApplicationAware
import grails.plugins.jaxrs.support.ConverterUtils
import grails.plugins.jaxrs.support.MessageBodyReaderSupport

import javax.ws.rs.Consumes
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.ext.Provider
import java.lang.annotation.Annotation
import java.lang.reflect.Type

import static ConverterUtils.getDefaultEncoding
import static ConverterUtils.xmlToMap

/**
 * A message body reader that converts an XML entity stream to a map than can be
 * used to construct Grails domain objects. Any JAX-RS resource method that
 * defines a {@link Map} as parameter and consumes either <code>text/xml</code>
 * or <code>application/xml</code> will be passed a map created from an XML
 * request entity:
 * <p/>
 * <p/>
 * <pre>
 * &#064;Path('/notes')
 * &#064;Produces('text/xml')
 * class NotesResource {
 *
 *      &#064;POST
 *      &#064;Consumes('text/xml')
 *      Response addNote(Map properties) {
 *          // create ne Note domain object
 *          def note = new Note(properties).save()
 *      }
 *
 * }
 *
 *
 * </pre>
 *
 * @author Martin Krasser
 */
@Provider
@Consumes(["text/xml", "application/xml"])
class XMLReader extends MessageBodyReaderSupport<Map> implements GrailsApplicationAware {

    private GrailsApplication grailsApplication

    public void setGrailsApplication(GrailsApplication grailsApplication) {
        this.grailsApplication = grailsApplication
    }

    @Override
    public Map readFrom(Class<Map> type, Type genericType,
                        Annotation[] annotations, MediaType mediaType,
                        MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException, WebApplicationException {

        String encoding = ConverterUtils.getEncoding(httpHeaders, mediaType, getDefaultEncoding(grailsApplication))

        // Convert XML to map used for constructing domain objects
        xmlToMap(entityStream, encoding)
    }

    @Override
    public Map readFrom(MultivaluedMap<String, String> httpHeaders,
                        InputStream entityStream) throws IOException,
            WebApplicationException {
        // TODO: Fix MessageBodyReaderSupport abstract method
        throw new RuntimeException('This should never be called, because we override the readFrom(all-parameters) method.')
    }
}
