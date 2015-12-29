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
package grails.plugins.jaxrs.infra

import grails.core.GrailsApplication
import grails.plugins.jaxrs.TestResource01
import grails.spring.BeanBuilder
import grails.util.Holders
import grails.plugins.jaxrs.ProviderArtefactHandler
import grails.plugins.jaxrs.ResourceArtefactHandler
import grails.plugins.jaxrs.provider.DomainObjectReader
import grails.plugins.jaxrs.provider.DomainObjectWriter
import grails.plugins.jaxrs.provider.JSONReader
import grails.plugins.jaxrs.provider.JSONWriter
import grails.plugins.jaxrs.web.JaxrsContext
import grails.plugins.jaxrs.web.JaxrsListener
import grails.plugins.jaxrs.web.JaxrsUtils
import org.springframework.context.ApplicationContext

import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener

/**
 * @author Martin Krasser
 */
class IntegrationTestEnvironment {

    private JaxrsContext jaxrsContext

    private String contextConfigLocations
    private String jaxrsProviderName

    private List jaxrsClasses

    private boolean autoDetectJaxrsClasses

    IntegrationTestEnvironment(String contextConfigLocations, String jaxrsProviderName, List jaxrsClasses, boolean autoDetectJaxrsClasses) {
        this.contextConfigLocations = contextConfigLocations
        this.jaxrsProviderName = jaxrsProviderName
        this.jaxrsClasses = jaxrsClasses
        this.autoDetectJaxrsClasses = autoDetectJaxrsClasses
    }

    synchronized JaxrsContext getJaxrsContext() {
        if (!jaxrsContext) {
            GrailsApplication application = Holders.grailsApplication
            ApplicationContext applicationContext = application.mainContext

            BeanBuilder beanBuilder = new BeanBuilder(applicationContext)
            beanBuilder.importBeans "grails/plugins/jaxrs/itest/integrationTestEnvironment.xml, ${contextConfigLocations}"

            ServletContextListener jaxrsListener = new JaxrsListener()

            if (autoDetectJaxrsClasses) {
                application.getArtefacts(ResourceArtefactHandler.TYPE).each { gc ->
                    jaxrsClasses << gc.clazz
                }
                application.getArtefacts(TYPE).each { gc ->
                    jaxrsClasses << gc.clazz
                }
            }

//            if (jaxrsProviderName == JaxrsContext.JAXRS_PROVIDER_NAME_RESTLET) {
            jaxrsClasses << JSONReader
            jaxrsClasses << JSONWriter
            jaxrsClasses << DomainObjectReader
            jaxrsClasses << DomainObjectWriter
//            }

            beanBuilder.beans {
                jaxrsClasses.each { clazz ->
                    "${clazz.name}"(clazz) { bean ->
                        bean.autowire = true
                    }
                }
            }.registerBeans(applicationContext.beanFactory)
            new ServletContextEvent(applicationContext.servletContext)
            jaxrsListener.contextInitialized(new ServletContextEvent(applicationContext.servletContext))
            jaxrsContext = JaxrsUtils.getRequiredJaxrsContext(applicationContext.servletContext)
            jaxrsContext.jaxrsServletContext = applicationContext.servletContext
            jaxrsClasses.each { jaxrsContext.jaxrsConfig.classes << it }

            jaxrsContext.jaxrsProviderName = jaxrsProviderName
            jaxrsContext.init()
        }
        jaxrsContext
    }
}
