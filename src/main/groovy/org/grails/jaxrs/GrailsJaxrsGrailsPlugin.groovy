package org.grails.jaxrs

import grails.plugins.*
import org.grails.jaxrs.generator.CodeGenerator
import org.grails.jaxrs.provider.DomainObjectReader
import org.grails.jaxrs.provider.DomainObjectWriter
import org.grails.jaxrs.provider.JSONReader
import org.grails.jaxrs.provider.JSONWriter
import org.grails.jaxrs.provider.XMLReader
import org.grails.jaxrs.provider.XMLWriter
import org.grails.jaxrs.web.JaxrsContext

import static org.grails.jaxrs.web.JaxrsUtils.JAXRS_CONTEXT_NAME

class GrailsJaxrsGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "3.0.1 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/domain/*",
            "grails-app/providers/*",
            "grails-app/resources/*",
            "src/groovy/org/grails/jaxrs/test/*",
            "lib/*-sources.jar"
    ]

    def loadAfter = ['controllers', 'services', 'spring-security-core']
    def artefacts = [
            new ResourceArtefactHandler(),
            new ProviderArtefactHandler()
    ]

    def watchedResources = [
            "file:./grails-app/resources/**/*Resource.groovy",
            "file:./grails-app/providers/**/*Reader.groovy",
            "file:./grails-app/providers/**/*Writer.groovy",
            "file:./plugins/*/grails-app/resources/**/*Resource.groovy",
            "file:./plugins/*/grails-app/providers/**/*Reader.groovy",
            "file:./plugins/*/grails-app/providers/**/*Writer.groovy"
    ]

    def title = "JSR 311 plugin" // Headline display name of the plugin
    def author = "Martin Krasser"
    def authorEmail = "krasserm@googlemail.com"
    def description = """
A plugin that supports the development of RESTful web services based on the
Java API for RESTful Web Services (JSR 311: JAX-RS). It is targeted at
developers who want to structure the web service layer of an application in
a JSR 311 compatible way but still want to continue to use Grails' powerful
features such as GORM, automated XML and JSON marshalling, Grails services,
Grails filters and so on. This plugin is an alternative to Grails' built-in
mechanism for implementing  RESTful web services.

At the moment, plugin users may choose between Jersey and Restlet as JAX-RS
implementation. Both implementations are packaged with the plugin. Support for
Restlet was added in version 0.2 of the plugin in order to support deployments
on the Google App Engine. Other JAX-RS implementations such as RestEasy or
Apache Wink are likely to be added in upcoming versions of the plugin.
"""
    def profiles = ['web']

    // URL to the plugin's documentation
    def documentation = 'https://github.com/krasserm/grails-jaxrs/wiki'

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
//    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
//    def organization = [ name: "My Company", url: "http://www.my-company.com/" ]

    // Any additional developers beyond the author specified above.
    def developers = [
            [name: 'Davide Cavestro', email: 'davide.cavestro@gmail.com'],
            [name: 'Noam Y. Tenne', email: 'noam@10ne.org']
    ]

    // Location of the plugin's issue tracker.
    def issueManagement = [url: 'https://github.com/krasserm/grails-jaxrs/issues']

    // Online location of the plugin's browseable source code.
    def scm = [url: 'https://github.com/krasserm/grails-jaxrs']

/**
 * Adds the JaxrsContext and plugin- and application-specific JAX-RS
 * resource and provider classes to the application context.
 */
    Closure doWithSpring() {
        { ->
            // Configure the JAX-RS context
            'jaxrsContext'(JaxrsContext)

            // Configure default providers
            "${XMLWriter.name}"(XMLWriter)
            "${XMLReader.name}"(XMLReader)
            "${JSONWriter.name}"(JSONWriter)
            "${JSONReader.name}"(JSONReader)
            "${DomainObjectReader.name}"(DomainObjectReader)
            "${DomainObjectWriter.name}"(DomainObjectWriter)

            // Configure application-provided resources
            application.resourceClasses.each { rc ->
                "${rc.propertyName}"(rc.clazz) { bean ->
                    bean.scope = owner.getResourceScope(application)
                    bean.autowire = true
                }
            }

            // Configure application-provided providers
            application.providerClasses.each { pc ->
                "${pc.propertyName}"(pc.clazz) { bean ->
                    bean.scope = 'singleton'
                    bean.autowire = true
                }
            }

            // Configure the resource code generator
            "${CodeGenerator.name}"(CodeGenerator)
        }
    }

    void doWithDynamicMethods() {
        // TODO Implement registering dynamic methods to classes (optional)
    }

    /**
     * Reconfigures the JaxrsConfig with plugin- and application-specific
     * JAX-RS resource and provider classes. Configures the JaxrsContext
     * with the JAX-RS implementation to use. The name of the JAX-RS
     * implementation is obtained from the configuration property
     * <code>org.grails.jaxrs.provider.name</code>. Default value is
     * <code>jersey</code>.
     */
    void doWithApplicationContext() {
        def context = applicationContext.getBean(JAXRS_CONTEXT_NAME)
        def config = context.jaxrsConfig

        context.jaxrsProviderName = getProviderName(application)
        context.jaxrsProviderExtraPaths = getProviderExtraPaths(application)
        context.jaxrsProviderInitParameters = getProviderInitParameters(application)

        config.reset()
        config.classes << XMLWriter
        config.classes << XMLReader
        config.classes << JSONWriter
        config.classes << JSONReader
        config.classes << DomainObjectReader
        config.classes << DomainObjectWriter

        application.getArtefactInfo('Resource').classesByName.values().each { clazz ->
            config.classes << clazz
        }
        application.getArtefactInfo('Provider').classesByName.values().each { clazz ->
            config.classes << clazz
        }
    }

    /**
     * Updates application-specific JAX-RS resource and provider classes in
     * the application context.
     */
    void onChange(Map<String, Object> event) {
        if (!event.ctx) {
            return
        }

        if (application.isArtefactOfType(ResourceArtefactHandler.TYPE, event.source)) {
            def resourceClass = application.addArtefact(ResourceArtefactHandler.TYPE, event.source)
            beans {
                "${resourceClass.propertyName}"(resourceClass.clazz) { bean ->
                    bean.scope = owner.getResourceScope(application)
                    bean.autowire = true
                }
            }.registerBeans(event.ctx)
        } else if (application.isArtefactOfType(ProviderArtefactHandler.TYPE, event.source)) {
            def providerClass = application.addArtefact(ProviderArtefactHandler.TYPE, event.source)
            beans {
                "${providerClass.propertyName}"(providerClass.clazz) { bean ->
                    bean.scope = 'singleton'
                    bean.autowire = true
                }
            }.registerBeans(event.ctx)
        } else {
            return
        }

        // Setup the JaxrsConfig
        doWithApplicationContext(event.ctx)

        // Resfresh the JaxrsContext
        event.ctx.getBean(JAXRS_CONTEXT_NAME).refresh()
    }

    void onConfigChange(Map<String, Object> event) {
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    void onShutdown(Map<String, Object> event) {
        // TODO Implement code that is executed when the application shuts down (optional)
    }

    private String getResourceScope(application) {
        def scope = application.config.org.grails.jaxrs.resource.scope
        if (!scope) {
            scope = 'prototype'
        }
        scope
    }

    private String getProviderName(application) {
        def name = application.config.org.grails.jaxrs.provider.name
        if (!name) {
            name = JaxrsContext.JAXRS_PROVIDER_NAME_JERSEY
        }
        name
    }

    private String getProviderExtraPaths(application) {
        application.config.org.grails.jaxrs.provider.extra.paths
    }

    private Map<String, String> getProviderInitParameters(application) {
        application.config.org.grails.jaxrs.provider.init.parameters
    }
}
