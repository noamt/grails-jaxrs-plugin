package grails.plugins.jaxrs

import grails.plugins.jaxrs.providers.CustomRequestEntityReader
import grails.plugins.jaxrs.providers.CustomResponseEntityWriter
import grails.test.mixin.TestFor
import grails.test.mixin.integration.Integration
import grails.plugins.jaxrs.infra.IntegrationTestSpec
import grails.plugins.jaxrs.web.JaxrsContext
import org.springframework.beans.factory.annotation.Autowired

/**
 * @author Noam Y. Tenne
 */
@TestFor(JaxrsController)
@Integration
class ExampleIntegrationSpec extends IntegrationTestSpec {

    public static final String JAXRS_CONTEXT_NAME = "jaxrsContext"

    List getJaxrsClasses() {
        [TestResource01,
         TestResource02,
         CustomRequestEntityReader,
         CustomResponseEntityWriter]
    }

    static doWithSpring = {
        jaxrsContext(JaxrsContext)
    }

    @Autowired
    JaxrsContext jaxrsContext

    def "Execute a GET request"() {
        when:
        sendRequest('/test/01', 'GET')

        then:
        response.status == 200
        response.contentAsString == 'test01'
        response.getHeader('Content-Type').startsWith('text/plain')
    }

    def "Execute a POST request"() {
        when:
        sendRequest('/test/02', 'POST', ['Content-Type': 'text/plain'], 'hello'.bytes)

        then:
        response.status == 200
        response.contentAsString == 'response:hello'
        response.getHeader('Content-Type').startsWith('text/plain')
    }
}
