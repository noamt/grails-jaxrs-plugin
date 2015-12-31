
package grails.plugins.jaxrs.infra

import grails.core.GrailsApplication
import grails.plugins.jaxrs.JaxrsController
import grails.test.mixin.TestFor
import org.apache.commons.lang.StringUtils
import grails.plugins.jaxrs.web.JaxrsContext
import grails.plugins.jaxrs.web.JaxrsUtils
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Shared
import spock.lang.Specification

import javax.servlet.http.HttpServletResponse
import javax.ws.rs.core.HttpHeaders

/**
 * @author Noam Y. Tenne
 */
@TestFor(JaxrsController)
abstract class IntegrationTestSpec extends Specification {

    @Autowired
    GrailsApplication grailsApplication

    @Shared
    def testEnvironment


    def setupSpec() {
        testEnvironment = null
    }

    def setup() {
        grailsApplication.config.grails.plugins.jaxrs.dowriter.require.generic.collections = false
        grailsApplication.config.grails.plugins.jaxrs.doreader.disable = false
        grailsApplication.config.grails.plugins.jaxrs.dowriter.disable = false

        if (!testEnvironment) {
            testEnvironment = new IntegrationTestEnvironment(getContextLocations(), getJaxrsImplementation(),
                    getJaxrsClasses(), isAutoDetectJaxrsClasses())
        }

        JaxrsContext context =  testEnvironment.jaxrsContext
        controller.jaxrsContext = context
    }

    void setRequestUrl(String url) {
        JaxrsUtils.setRequestUriAttribute(controller.request, url)
    }

    void setRequestMethod(String method) {
        controller.request.method = method
    }

    void setRequestContent(byte[] content) {
        controller.request.content = content
    }

    void addRequestHeader(String key, Object value) {
        controller.request.addHeader(key, value)
    }

    void resetResponse() {
        controller.response.committed = false
        controller.response.reset()
    }

    HttpServletResponse getResponse() {
        controller.response
    }

    HttpServletResponse sendRequest(String url, String method, byte[] content = ''.bytes) {
        sendRequest(url, method, [:], content)
    }

    HttpServletResponse sendRequest(String url, String method, Map<String, Object> headers, byte[] content = ''.bytes) {
        resetResponse()

        def uri = new URI(url)
        requestUrl = uri.path

        if (uri.query) {
            controller.request.queryString = uri.query
        }

        requestMethod = method
        requestContent = content

        headers.each { entry ->
            addRequestHeader(entry.key, entry.value)
        }

        if (content.length != 0) {
            String existingContentLength = controller.request.getHeader(HttpHeaders.CONTENT_LENGTH)
            if (StringUtils.isBlank(existingContentLength)) {
                addRequestHeader(HttpHeaders.CONTENT_LENGTH, content.length)
            }
        }

        controller.handle()
        controller.response
    }

    /**
     * Implementors can define additional Spring application context locations.
     */
    String getContextLocations() {
        ''
    }

    /**
     * Returns the JAX-RS implementation to use. Default is 'jersey'.
     */
    String getJaxrsImplementation() {
        JaxrsContext.JAXRS_PROVIDER_NAME_JERSEY
    }

    /**
     * Returns the list of JAX-RS classes for testing. Auto-detected classes
     * will be added to this list later.
     */
    List getJaxrsClasses() {
        []
    }

    /**
     * Determines whether JAX-RS resources or providers are auto-detected in
     * <code>grails-app/resources</code> or <code>grails-app/providers</code>.
     *
     * @return true is JAX-RS classes should be auto-detected.
     */
    boolean isAutoDetectJaxrsClasses() {
        true
    }
}
