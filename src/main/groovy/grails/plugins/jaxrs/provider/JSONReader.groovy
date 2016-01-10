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
import static ConverterUtils.jsonToMap

/**
 * A message body reader that converts a JSON entity stream to a map than can be
 * used to construct Grails domain objects. Any JAX-RS resource method that
 * defines a {@link Map} as parameter and consumes either
 * <code>text/x-json</code> or <code>application/json</code> will be passed a
 * map created from a JSON request entity:
 * <p/>
 * <p/>
 * <pre>
 * &#064;Path('/notes')
 * &#064;Produces('text/x-json')
 * class NotesResource {
 *
 *      &#064;POST
 *      &#064;Consumes('text/x-json')
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
@Consumes(["text/x-json", "application/json"])
class JSONReader extends MessageBodyReaderSupport<Map> implements GrailsApplicationAware {

    private GrailsApplication grailsApplication;

    public void setGrailsApplication(GrailsApplication grailsApplication) {
        this.grailsApplication = grailsApplication
    }

    @Override
    public Map readFrom(Class<Map> type, Type genericType,
                        Annotation[] annotations, MediaType mediaType,
                        MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException, WebApplicationException {

        String encoding = ConverterUtils.getEncoding(httpHeaders, mediaType, getDefaultEncoding(grailsApplication))

        // Convert JSON to map used for constructing domain objects
        jsonToMap(entityStream, encoding)
    }

    @Override
    public Map readFrom(MultivaluedMap<String, String> httpHeaders,
                        InputStream entityStream) throws IOException,
            WebApplicationException {
        // TODO: Fix MessageBodyReaderSupport abstract method (remove it completely or add empty implementation?)
        throw new RuntimeException('This should never be called, because we override the readFrom(all-parameters) method.')
    }
}
