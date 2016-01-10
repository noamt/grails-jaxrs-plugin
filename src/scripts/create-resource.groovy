description('Generates a JAX-RS resource') {
    usage 'grails create-resource [NAME]'
    argument name: 'Resource Name', description: 'The name of the resource to generate'
}

model = model(args[0])
render template: 'artifacts/Resource.groovy',
        destination: file("grails-app/resources/$model.packagePath/${model.simpleName}Resource.groovy"),
        model: model