<%=packageName ? "package ${packageName}\n\n" : ''%>import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces

@Path('/api/${propertyName}')
class ${simpleName} {

    @GET
    @Produces('text/plain')
    String get${simpleName}Representation() {
        '${simpleName}'
    }
}