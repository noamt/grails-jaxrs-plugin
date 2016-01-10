package grails.plugins.jaxrs

import grails.plugins.jaxrs.infra.IntegrationTestSpec
import grails.plugins.jaxrs.providers.CustomRequestEntityReader
import grails.plugins.jaxrs.providers.CustomResponseEntityWriter
import grails.plugins.jaxrs.web.JaxrsContext
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Unroll

import java.nio.charset.Charset




/*
 * Copyright 2009 - 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * @author Noam Y. Tenne
 */
abstract class JaxrsControllerIntegrationSpec extends IntegrationTestSpec {

    static doWithSpring = {
        jaxrsContext(JaxrsContext)
    }

    @Autowired
    JaxrsContext jaxrsContext
    
    // not in grails-app/resources or grails-app/providers,
    // so jaxrses are added here ...
    List getJaxrsClasses() {
        [TestResource01,
         TestResource02,
         TestResource03,
         TestResource04,
         TestResource05,
         TestResource06,
         TestQueryParamResource,
         CustomRequestEntityReader,
         CustomResponseEntityWriter]
    }

    def "Execute a GET request"() {
        when:
        sendRequest('/test/01', 'GET')

        then:
        response.status == 200
        response.contentAsString == 'test01'
        response.getHeader('Content-Type').startsWith('text/plain')
    }

    def "Execute a POST request on resource 02"() {
        when:
        sendRequest('/test/02', 'POST', ['Content-Type': 'text/plain'], 'hello'.bytes)

        then:
        response.status == 200
        response.contentAsString == 'response:hello'
        response.getHeader('Content-Type').startsWith('text/plain')
    }

    def "Execute a POST request on resource 03"() {
        when:
        sendRequest('/test/03', 'POST', ['Content-Type': 'application/json'], '{"age":38,"name":"mike"}'.bytes)

        then:
        response.status == 200
        response.contentAsString.contains('"age":39')
        response.contentAsString.contains('"name":"ekim"')
        response.getHeader('Content-Type').startsWith('application/json')
    }

    def "Execute a POST request on resource 06"() {
        when:
        sendRequest('/test/06', 'POST', ['Content-Type': 'application/json'], '{"age":38,"name":"mike"}'.bytes)

        then:
        response.status == 200
        response.contentAsString.contains('<age>39</age>')
        response.contentAsString.contains('<name>ekim</name>')
        response.getHeader('Content-Type').startsWith('application/xml')
    }

    def "Initiate a single round-trip on resource 04"() {
        setup:
        def headers = ['Content-Type': contentType, 'Accept': contentType]

        when:
        sendRequest('/test/04/single', 'POST', headers, postedContent.bytes)

        then:
        response.status == 200
        expetedPartialReturnedContent.each {
            assert response.contentAsString.contains(it)
        }
        response.getHeader('Content-Type').startsWith(contentType)

        where:
        contentType        | postedContent                                    | expetedPartialReturnedContent
        'application/xml'  | '<testPerson><name>james</name></testPerson>'    | ['<name>semaj</name>']
        'application/json' | '{"class":"TestPerson","age":25,"name":"james"}' | ['"name":"semaj"', '"age":26']
    }

    def "Retrieve a generic XML collection from resource 04"() {
        setup:
        def before = grailsApplication.config.grails.plugins.jaxrs.dowriter.require.generic.collections
        grailsApplication.config.grails.plugins.jaxrs.dowriter.require.generic.collections = genericOnly

        when:
        sendRequest('/test/04/multi/generic', 'GET', ['Accept': 'application/xml'])

        then:
        response.contentAsString.contains('<list>')
        response.contentAsString.contains('<name>n1</name>')
        response.contentAsString.contains('<name>n2</name>')
        response.getHeader('Content-Type').startsWith('application/xml')

        cleanup:
        grailsApplication.config.grails.plugins.jaxrs.dowriter.require.generic.collections = before

        where:
        genericOnly << [true, false]
    }

    def "Retrieve a raw and object XML collection from resource 04"() {
        when:
        sendRequest("/test/04/multi/$mode", 'GET', ['Accept': 'application/xml'])

        then:
        response.status == 200
        response.contentAsString.contains('<list>')
        response.contentAsString.contains('<name>n1</name>')
        response.contentAsString.contains('<name>n2</name>')
        response.getHeader('Content-Type').startsWith('application/xml')

        where:
        mode << ['raw', 'object']
    }

    def "Retrieve a raw and object XML collection from resource 04 with generic-only turned on"() {
        setup:
        grailsApplication.config.grails.plugins.jaxrs.dowriter.require.generic.collections = true

        when:
        sendRequest("/test/04/multi/$mode", 'GET', ['Accept': 'application/xml'])

        then:
        response.status == 500

        cleanup:
        grailsApplication.config.grails.plugins.jaxrs.dowriter.require.generic.collections = false

        where:
        mode << ['raw', 'object']
    }

    def "Retrieve a generic JSON collection from resource 04"() {
        when:
        sendRequest('/test/04/multi/generic', 'GET', ['Accept': 'application/json'])

        then:
        response.contentAsString.contains('"name":"n1"')
        response.contentAsString.contains('"name":"n2"')
        response.getHeader('Content-Type').startsWith('application/json')
    }

    @Unroll
    def "Post content to resource 04 while the IO facilities are disabled"() {
        setup:
        def headers = ['Content-Type': 'application/xml', 'Accept': 'application/xml']
        grailsApplication.config.grails.plugins.jaxrs.getProperty("do${facilityToDisabled}").disable = true

        when:
        sendRequest('/test/04/single', 'POST', headers, '<testPerson><name>james</name></testPerson>'.bytes)

        then:
        response.status == expectedStatus

        cleanup:
        grailsApplication.config.grails.plugins.jaxrs.getProperty("do${facilityToDisabled}").disable = false

        where:
        facilityToDisabled | expectedStatus
        'reader'           | 415
        'writer'           | 500
    }

    def "Print the default response of a POST request to resource 04"() {
        when:
        sendRequest('/test/04/single', 'POST', ['Content-Type': 'application/xml'], '<testPerson><name>james</name></testPerson>'.bytes)

        then:
        response.status == 200
        println 'default: ' + response.contentAsString
        println 'content: ' + response.getHeader('Content-Type')
    }

    def "Retrieve an HTML response from resource 05"() {
        when:
        sendRequest('/test/05', 'GET')

        then:
        response.status == 200
        response.contentAsString == '<html><body>test05</body></html>'
        response.getHeader('Content-Type').startsWith('text/html')
    }

    def "Specify query params directly on the controller"() {
        setup:
        controller.request.queryString = 'value=jim'

        when:
        sendRequest('/test/queryParam', 'GET')

        then:
        response.status == 200
        response.contentAsString == 'jim'

        cleanup:
        controller.request.queryString = null

    }

    def "Specify query params in the request path"() {
        when:
        sendRequest('/test/queryParam?value=jim', 'GET')

        then:
        response.status == 200
        response.contentAsString == 'jim'
    }

    def "Specify query params in both the request path and the controller"() {
        setup:
        controller.request.queryString = 'value=bob'

        when:
        sendRequest('/test/queryParam?value=jim', 'GET')

        then:
        response.status == 200
        response.contentAsString == 'jim'

        cleanup:
        controller.request.queryString = null

    }

    def 'testGet01'() {
        when:
        sendRequest('/test/01', 'GET')

        then:
        200 == response.status
        'test01'== response.contentAsString
        response.getHeader('Content-Type').startsWith('text/plain')
    }


    def 'testPost02'() {
        when:
        sendRequest('/test/02', 'POST', ['Content-Type': 'text/plain'], 'hello'.bytes)

        then:
        200== response.status
        'response:hello' == response.contentAsString
        response.getHeader('Content-Type').startsWith('text/plain')
    }


    def 'testPost03'() {
        when:
        sendRequest('/test/03', 'POST', ['Content-Type': 'application/json'], '{"age":38,"name":"mike"}'.bytes)

        then:
        200 == response.status
        response.contentAsString.contains('"age":39')
        response.contentAsString.contains('"name":"ekim"')
        response.getHeader('Content-Type').startsWith('application/json')
    }

    def 'testPost06'() {
        when:
        sendRequest('/test/06', 'POST', ['Content-Type': 'application/json'], '{"age":38,"name":"mike"}'.bytes)

        then:
        200 == response.status
        response.contentAsString.contains('<age>39</age>')
        response.contentAsString.contains('<name>ekim</name>')
        response.getHeader('Content-Type').startsWith('application/xml')
    }


    def 'testPost03_CustomCharset'() {
        when:
        String CUSTOM_CHARSET = 'UTF-16'
        sendRequest('/test/03', 'POST', ['Content-Type': "application/json; charset=${CUSTOM_CHARSET}".toString()], '{"class":"TestPerson","age":38,"name":"mike"}'.getBytes(CUSTOM_CHARSET))

        then:
        200 == response.status
        response.contentAsString.contains('"age":39')
        response.contentAsString.contains('"name":"ekim"')
        response.getHeader('Content-Type').startsWith('application/json')
    }


    def 'testRoundtrip04XmlSingle'() {
        when:
        def headers = ['Content-Type': 'application/xml', 'Accept': 'application/xml']
        sendRequest('/test/04/single', 'POST', headers, '<testPerson><name>james</name></testPerson>'.bytes)

        then:
        200 == response.status
        response.contentAsString.contains('<name>semaj</name>')
        response.getHeader('Content-Type').startsWith('application/xml')
    }

    def 'testRoundtrip04JsonSingle'() {
        when:
        def headers = ['Content-Type': 'application/json', 'Accept': 'application/json']
        sendRequest('/test/04/single', 'POST', headers, '{"class":"TestPerson","age":25,"name":"james"}'.bytes)

        then:
        200 == response.status
        response.contentAsString.contains('"age":26')
        response.contentAsString.contains('"name":"semaj"')
        response.getHeader('Content-Type').startsWith('application/json')
    }


    def 'testRoundtrip04JsonSingle_CustomCharset'() {
        setup:
        String CUSTOM_CHARSET = 'UTF-16'
        def headers = ['Content-Type': "application/json; charset=${CUSTOM_CHARSET}".toString(), 'Accept': 'application/json']
        def testPersonWithCustomEncoding = '{"class":"TestPerson","age":45,"name":"james"}'.getBytes(Charset.forName(CUSTOM_CHARSET))

        when:
        sendRequest('/test/04/single', 'POST', headers, testPersonWithCustomEncoding)

        then:
        200 == response.status
        response.contentAsString.contains('"age":46')
        response.contentAsString.contains('"name":"semaj"')
        response.getHeader('Content-Type').startsWith('application/json')
    }

    def 'testRoundtrip04XmlSingle_CustomCharset'() {
        setup:
        String CUSTOM_CHARSET = 'UTF-16'
        def headers = ['Content-Type': "application/xml; charset=${CUSTOM_CHARSET}".toString(), 'Accept': 'application/xml']
        byte[] testPersonWithCustomEncoding = '<testPerson><name>james</name></testPerson>'.getBytes(Charset.forName(CUSTOM_CHARSET))

        when:
        sendRequest('/test/04/single', 'POST', headers, testPersonWithCustomEncoding)

        then:
        200 == response.status
        response.contentAsString.contains('<name>semaj</name>')
        response.getHeader('Content-Type').startsWith('application/xml')
    }


    def 'testGet04XmlCollectionGeneric'() {
        when:
        sendRequest('/test/04/multi/generic', 'GET', ['Accept': 'application/xml'])

        then:
        response.contentAsString.contains('<list>')
        response.contentAsString.contains('<name>n1</name>')
        response.contentAsString.contains('<name>n2</name>')
        response.getHeader('Content-Type').startsWith('application/xml')
    }


    def 'testGet04XmlCollectionGenericGenericOnly'() {
        setup:
        grailsApplication.config.grails.plugins.jaxrs.dowriter.require.generic.collections = true

        when:
        sendRequest('/test/04/multi/generic', 'GET', ['Accept': 'application/xml'])

        then:
        response.contentAsString.contains('<list>')
        response.contentAsString.contains('<name>n1</name>')
        response.contentAsString.contains('<name>n2</name>')
        response.getHeader('Content-Type').startsWith('application/xml')

        cleanup:
        grailsApplication.config.grails.plugins.jaxrs.dowriter.require.generic.collections = false
    }


    def 'testGet04XmlCollectionRaw'() {
        when:
        sendRequest('/test/04/multi/raw', 'GET', ['Accept': 'application/xml'])

        then:
        200 == response.status
        response.contentAsString.contains('<list>')
        response.contentAsString.contains('<name>n1</name>')
        response.contentAsString.contains('<name>n2</name>')
        response.getHeader('Content-Type').startsWith('application/xml')
    }


    def 'testGet04XmlCollectionRawGenericOnly'() {
        setup:
        grailsApplication.config.grails.plugins.jaxrs.dowriter.require.generic.collections = true

        when:
        sendRequest('/test/04/multi/raw', 'GET', ['Accept': 'application/xml'])

        then:
        500 == response.status

        cleanup:
        grailsApplication.config.grails.plugins.jaxrs.dowriter.require.generic.collections = false
    }


    def 'testGet04XmlCollectionObject'() {
        when:
        sendRequest('/test/04/multi/object', 'GET', ['Accept': 'application/xml'])

        then:
        200 == response.status
        response.contentAsString.contains('<list>')
        response.contentAsString.contains('<name>n1</name>')
        response.contentAsString.contains('<name>n2</name>')
        response.getHeader('Content-Type').startsWith('application/xml')
    }


    def 'testGet04XmlCollectionObjectGenericOnly'() {
        setup:
        grailsApplication.config.grails.plugins.jaxrs.dowriter.require.generic.collections = true

        when:
        sendRequest('/test/04/multi/object', 'GET', ['Accept': 'application/xml'])

        then:
        500 == response.status

        cleanup:
        grailsApplication.config.grails.plugins.jaxrs.dowriter.require.generic.collections = false
    }


    def 'testGet04JsonCollectionGeneric'() {
        when:
        sendRequest('/test/04/multi/generic', 'GET', ['Accept': 'application/json'])

        then:
        200 == response.status
        response.contentAsString.contains('"name":"n1"')
        response.contentAsString.contains('"name":"n2"')
        response.getHeader('Content-Type').startsWith('application/json')
    }


    def 'testPost04ReaderDisabled'() {
        setup:
        grailsApplication.config.grails.plugins.jaxrs.doreader.disable = true

        when:
        sendRequest('/test/04/single', 'POST', ['Content-Type': 'application/xml'], '<testPerson><name>james</name></testPerson>'.bytes)

        then:
        415 == response.status

        cleanup:
        grailsApplication.config.grails.plugins.jaxrs.doreader.disable = false
    }


    def 'testPost04WriterDisabled'() {
        setup:
        grailsApplication.config.grails.plugins.jaxrs.dowriter.disable = true
        def headers = ['Content-Type': 'application/xml', 'Accept': 'application/xml']

        when:
        sendRequest('/test/04/single', 'POST', headers, '<testPerson><name>james</name></testPerson>'.bytes)

        then:
        500 == response.status

        cleanup:
        grailsApplication.config.grails.plugins.jaxrs.dowriter.disable = false
    }


    def 'testPost04DefaultResponse'() {
        when:
        sendRequest('/test/04/single', 'POST', ['Content-Type': 'application/xml'], '<testPerson><name>james</name></testPerson>'.bytes)

        then:
        200 == response.status
        println 'default: ' + response.contentAsString
        println 'content: ' + response.getHeader('Content-Type')
    }


    def 'testGet05HtmlResponse'() {
        when:
        sendRequest('/test/05', 'GET')

        then:
        200 == response.status
        '<html><body>test05</body></html>' == response.contentAsString
        response.getHeader('Content-Type').startsWith('text/html')
    }

    def 'specifyQueryParamsOnController'() {
        setup:
        controller.request.queryString = 'value=jim'

        when:
        sendRequest('/test/queryParam', 'GET')

        then:
        200 == response.status
        'jim' == response.contentAsString

        cleanup:
        controller.request.queryString = null
    }

    def 'specifyQueryParamsInRequestPath'() {
        when:
        sendRequest('/test/queryParam?value=jim', 'GET')

        then:
        200 == response.status
        'jim' == response.contentAsString
    }

    def 'specifyQueryParamsOnControllerAndInRequestPath'() {
        setup:
        controller.request.queryString = 'value=bob'

        when:
        sendRequest('/test/queryParam?value=jim', 'GET')

        then:
        200 == response.status
        'jim' == response.contentAsString

        cleanup:
        controller.request.queryString = null
    }
}
