package jaxrs

import grails.plugins.Plugin
import grails.plugins.jaxrs.DefaultJaxrsConfig
import grails.plugins.jaxrs.ProviderArtefactHandler
import grails.plugins.jaxrs.ResourceArtefactHandler
import grails.plugins.jaxrs.generator.CodeGenerator
import grails.plugins.jaxrs.provider.*
import grails.plugins.jaxrs.web.JaxrsContext

import static grails.plugins.jaxrs.web.JaxrsUtils.JAXRS_CONTEXT_NAME

class JaxrsGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "3.0.10 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    // TODO Fill in these fields
    def title = "Jaxrs" // Headline display name of the plugin
    def author = "Your name"
    def authorEmail = ""
    def description = '''\
Brief summary/description of the plugin.
'''
    def profiles = ['web']

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/jaxrs"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
//    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
//    def organization = [ name: "My Company", url: "http://www.my-company.com/" ]

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
//    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPMYPLUGIN" ]

    // Online location of the plugin's browseable source code.
//    def scm = [ url: "http://svn.codehaus.org/grails-plugins/" ]

    def watchedResources = "file:./grails-app/resources/*Resource.groovy"

    def artefacts = [grails.plugins.jaxrs.ProviderArtefactHandler, grails.plugins.jaxrs.ResourceArtefactHandler]

    Closure doWithSpring() {
        { ->
            jaxrsContext(JaxrsContext)
            defaultJaxrsConfig DefaultJaxrsConfig

            // Configure default providers
            "${XMLWriter.name}"(XMLWriter)
            "${XMLReader.name}"(XMLReader)
            "${JSONWriter.name}"(JSONWriter)
            "${JSONReader.name}"(JSONReader)
            "${DomainObjectReader.name}"(DomainObjectReader)
            "${DomainObjectWriter.name}"(DomainObjectWriter)

            // Configure application-provided resources
            grailsApplication.resourceClasses.each { rc ->
                "${rc.propertyName}"(rc.clazz) { bean ->
                    bean.scope = owner.getResourceScope()
                    bean.autowire = true
                }
            }

            // Configure application-provided providers
            grailsApplication.providerClasses.each { pc ->
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

    @Override
    void doWithApplicationContext() {
        def context = applicationContext.getBean(JAXRS_CONTEXT_NAME)
        def config = context.jaxrsConfig

        context.jaxrsProviderName = getProviderName()
        context.jaxrsProviderExtraPaths = getProviderExtraPaths()
        context.jaxrsProviderInitParameters = getProviderInitParameters()

        config.reset()
        config.classes << XMLWriter
        config.classes << XMLReader
        config.classes << JSONWriter
        config.classes << JSONReader
        config.classes << DomainObjectReader
        config.classes << DomainObjectWriter

        grailsApplication.getArtefactInfo('Resource').classesByName.values().each { clazz ->
            config.classes << clazz
        }
        grailsApplication.getArtefactInfo('Provider').classesByName.values().each { clazz ->
            config.classes << clazz
        }

    }

    @Override
    void onChange(Map<String, Object> event) {
        if (!event.ctx) {
            return
        }

        if (grailsApplication.isArtefactOfType(ResourceArtefactHandler.TYPE, event.source)) {
            def resourceClass = grailsApplication.addArtefact(ResourceArtefactHandler.TYPE, event.source)
            beans {
                "${resourceClass.propertyName}"(resourceClass.clazz) { bean ->
                    bean.scope = owner.getResourceScope()
                    bean.autowire = true
                }
            }.registerBeans(event.ctx)
        } else if (grailsApplication.isArtefactOfType(ProviderArtefactHandler.TYPE, event.source)) {
            def providerClass = grailsApplication.addArtefact(ProviderArtefactHandler.TYPE, event.source)
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


    private String getProviderName() {
        def name = grailsApplication.config.grails.plugins.jaxrs.provider.name
        if (!name) {
            name = JaxrsContext.JAXRS_PROVIDER_NAME_JERSEY
        }
        name
    }

    private String getProviderExtraPaths() {
        grailsApplication.config.grails.plugins.jaxrs.provider.extra.paths
    }

    private Map<String, String> getProviderInitParameters() {
        grailsApplication.config.grails.plugins.jaxrs.provider.init.parameters
    }

    private String getResourceScope() {
        def scope = grailsApplication.config.grails.plugins.jaxrs.resource.scope
        if (!scope) {
            scope = 'prototype'
        }
        scope
    }
}
