description('Generates JAX-RS resources and CRUD service classes for domain classes') {
    usage 'grails generate-resources [DOMAIN-CLASS]'
    argument name: 'Domain Class Name', description: 'The name of the domain class to generate a resource for'
}

model = model(args[0])
render template: 'scaffolding/CollectionResource.groovy',
        destination: file("grails-app/resources/$model.packagePath/${model.simpleName}CollectionResource.groovy"),
        model: model
render template: 'scaffolding/Resource.groovy',
        destination: file("grails-app/resources/$model.packagePath/${model.simpleName}Resource.groovy"),
        model: model
render template: 'scaffolding/ResourceService.groovy',
        destination: file("grails-app/services/$model.packagePath/${model.simpleName}ResourceService.groovy"),
        model: model