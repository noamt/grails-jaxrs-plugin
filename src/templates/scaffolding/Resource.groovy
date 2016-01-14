<%=packageName ? "package ${packageName}\n\n" : ''%>import static grails.plugins.jaxrs.response.Responses.*

import javax.ws.rs.Consumes
import javax.ws.rs.DELETE
import javax.ws.rs.GET
import javax.ws.rs.Produces
import javax.ws.rs.PUT
import javax.ws.rs.core.Response

@Consumes(['application/xml','application/json'])
@Produces(['application/xml','application/json'])
class ${simpleName}Resource {

    def ${propertyName}ResourceService
    def id

    @GET
    Response read() {
        ok ${propertyName}ResourceService.read(id)
    }

    @PUT
    Response update(${simpleName} dto) {
        dto.id = id
        ok ${propertyName}ResourceService.update(dto)
    }

    @DELETE
    void delete() {
        ${propertyName}ResourceService.delete(id)
    }
}