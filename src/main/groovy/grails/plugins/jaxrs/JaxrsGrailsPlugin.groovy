package grails.plugins.jaxrs

import org.grails.jaxrs.web.JaxrsFilter
import org.grails.jaxrs.web.JaxrsListener


class JaxrsGrailsPlugin {
    def groupId = "org.grails.plugins"
    def version = "0.11"
    def grailsVersion = "3.0 > *"

    /**
     * Adds the JaxrsFilter and JaxrsListener to the web application
     * descriptor.
     */
    def doWithWebDescriptor = { xml ->

        def lastListener = xml.'listener'.iterator().toList().last()
        lastListener + {
            'listener' {
                'listener-class'(JaxrsListener.name)
            }
        }

        def firstFilter = xml.'filter'[0]
        firstFilter + {
            'filter' {
                'filter-name'('jaxrsFilter')
                'filter-class'(JaxrsFilter.name)
            }
        }

        def firstFilterMapping = xml.'filter-mapping'[0]
        firstFilterMapping + {
            'filter-mapping' {
                'filter-name'('jaxrsFilter')
                'url-pattern'('/*')
                'dispatcher'('FORWARD')
                'dispatcher'('REQUEST')
            }
        }

        def grailsServlet = xml.servlet.find { servlet ->

            'grails'.equalsIgnoreCase(servlet.'servlet-name'.text())

        }

        // reload default GrailsDispatcherServlet adding 'dispatchOptionsRequest':'true'
        grailsServlet.replaceNode { node ->
            'servlet' {
                'servlet-name'('grails')
                'servlet-class'(GrailsDispatcherServlet.name)
                'init-param' {
                    'param-name'('dispatchOptionsRequest')
                    'param-value'('true')
                }
                'load-on-startup'('1')
            }
        }
    }


    def doWithSpring = {

    }
}
