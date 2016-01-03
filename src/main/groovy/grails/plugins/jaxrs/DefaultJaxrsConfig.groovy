package grails.plugins.jaxrs

import grails.plugins.jaxrs.web.JaxrsFilter
import grails.plugins.jaxrs.web.JaxrsListener
import grails.util.Environment
import org.grails.web.servlet.mvc.GrailsDispatcherServlet
import org.springframework.boot.context.embedded.ServletContextInitializer
import org.springframework.context.annotation.Bean

import javax.servlet.ServletContext
import javax.servlet.ServletException

/**
 * Created by shiranr on 30/12/2015.
 */
class DefaultJaxrsConfig  {

    @Bean
    public ServletContextInitializer myInitializer() {
        return new ServletContextInitializer() {
            @Override
            public void onStartup(ServletContext servletContext) throws ServletException {
                if (Environment.current != Environment.TEST) {
                    servletContext.addFilter('jaxrsFilter', JaxrsFilter.name)
                    servletContext.addListener(JaxrsListener)
                }
            }
        }
    }


}
