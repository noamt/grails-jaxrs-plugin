<%=packageName ? "package ${packageName}\n\n" : ''%>import static grails.plugins.jaxrs.response.Responses.*

import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.Produces
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.POST
import javax.ws.rs.core.Response

@Path('/api/${propertyName}')
@Consumes(['application/xml','application/json'])
@Produces(['application/xml','application/json'])
class ${simpleName}CollectionResource {

    def ${propertyName}ResourceService

    @POST
    Response create(${simpleName} dto) {
        created ${propertyName}ResourceService.create(dto)
    }

    @GET
    Response readAll() {
        ok ${propertyName}ResourceService.readAll()
    }

    @Path('/{id}')
    ${simpleName}Resource getResource(@PathParam('id') Long id) {
        new ${simpleName}Resource(${propertyName}ResourceService: ${propertyName}ResourceService, id:id)
    }
}
