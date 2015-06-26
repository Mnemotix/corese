package fr.inria.edelweiss.kgramserver.webservice;

import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;
import static fr.inria.edelweiss.kgramserver.webservice.EmbeddedJettyServer.HOME_PAGE;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Redirect.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 22 juin 2015
 */
@Path("/srv")
public class SrvWrapper {

    private static final String headerAccept = "Access-Control-Allow-Origin";
    static final String CONTENT_HTML = "<div class=\"content\" id=\"contentOfSite\">";

    @GET
    @Path("/{path:template|spin/tospin|spin/tosparql|sdk|tutorial/.*|process/.*}")
    @Produces("text/html")
    public Response transformGet(
            @PathParam("path") String path,
            @QueryParam("profile") String profile, // query + transform
            @QueryParam("uri") String resource, // URI of resource focus
            @QueryParam("query") String query, // SPARQL query
            @QueryParam("name") String name, // SPARQL query name (in webapp/query or path or URL)
            @QueryParam("value") String value, // values clause that may complement query           
            @QueryParam("transform") String transform, // Transformation URI to post process result
            @QueryParam("default-graph-uri") List<String> defaultGraphUris,
            @QueryParam("named-graph-uri") List<String> namedGraphUris) {

        Response rs;

        if (path.equalsIgnoreCase("template")) {
            rs = new Transformer().queryGETHTML(profile, resource, query, name, value, transform, defaultGraphUris, namedGraphUris);
        } else if (path.equalsIgnoreCase("spin/tospin")) {
            rs = new SPIN().toSPIN(query);
        } else if (path.equalsIgnoreCase("spin/tosparql")) {
            rs = new SPIN().toSPARQL(query);
        } else if (path.equalsIgnoreCase("sdk")) {
            rs = new SDK().sdk(query, name, value);
        } else if (path.startsWith("tutorial") || path.startsWith("process")) {
            rs = new Tutorial().get(getService(path), profile, resource, query, name, value, transform, defaultGraphUris, namedGraphUris);
        } else if (path.startsWith("process")) {
            rs = new Processor().typecheck(resource, transform, getService(path));
        } else {
            rs = Response.status(Response.Status.BAD_REQUEST).header(headerAccept, "*").entity("Can not get right service solver.").build();
        }

        return Response.status(rs.getStatus()).header(headerAccept, "*").entity(wrapper(rs).toString()).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/{path:template|spin/tospin|spin/tosparql|sdk|tutorial/.*|process/.*}")
    @Produces("text/html")
    public Response transformPost(
            @PathParam("path") String path,
            @FormParam("profile") String profile, // query + transform
            @FormParam("uri") String resource, // URI of resource focus
            @FormParam("query") String query, // SPARQL query
            @FormParam("name") String name, // SPARQL query name (in webapp/query or path or URL)
            @FormParam("value") String value, // values clause that may complement query           
            @FormParam("transform") String transform, // Transformation URI to post process result
            @FormParam("default-graph-uri") List<String> defaultGraphUris,
            @FormParam("named-graph-uri") List<String> namedGraphUris) {

        Response rs;

        if (path.equalsIgnoreCase("template")) {
            rs = new Transformer().queryPOSTHTML(profile, resource, query, name, value, transform, defaultGraphUris, namedGraphUris);
        } else if (path.equalsIgnoreCase("spin/tospin")) {
            rs = new SPIN().toSPINPOST(query);
        } else if (path.equalsIgnoreCase("spin/tosparql")) {
            rs = new SPIN().toSPARQLPOST(query);
        } else if (path.equalsIgnoreCase("sdk")) {
            rs = new SDK().sdk(query, name, value);
        } else if (path.startsWith("tutorial")) {
            rs = new Tutorial().post(getService(path), profile, resource, query, name, value, transform, defaultGraphUris, namedGraphUris);
        } else if (path.startsWith("process")) {
            rs = new Processor().typecheckPost(resource, transform, getService(path));
        } else {
            rs = Response.status(Response.Status.BAD_REQUEST).header(headerAccept, "*").entity("Can not get right service solver.").build();
        }

        return Response.status(rs.getStatus()).header(headerAccept, "*").entity(wrapper(rs).toString()).build();
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/{path:template|spin/tospin|spin/tosparql|sdk|tutorial/.*|process/.*}")
    @Produces("text/html")
    public Response transformPostMD(
            @PathParam("path") String path,
            @FormDataParam("profile") String profile, // query + transform
            @FormDataParam("uri") String resource, // URI of resource focus
            @FormDataParam("query") String query, // SPARQL query
            @FormDataParam("name") String name, // SPARQL query name (in webapp/query or path or URL)
            @FormDataParam("value") String value, // values clause that may complement query           
            @FormDataParam("transform") String transform, // Transformation URI to post process result
            @FormDataParam("default-graph-uri") List<FormDataBodyPart> defaultGraphUris,
            @FormDataParam("named-graph-uri") List<FormDataBodyPart> namedGraphUris) {

        Response rs;

        if (path.equalsIgnoreCase("template")) {
            rs = new Transformer().queryPOSTHTML_MD(profile, resource, query, name, value, transform, defaultGraphUris, namedGraphUris);
        } else if (path.equalsIgnoreCase("spin/tospin")) {
            rs = new SPIN().toSPINPOST_MD(query);
        } else if (path.equalsIgnoreCase("spin/tosparql")) {
            rs = new SPIN().toSPARQLPOST_MD(query);
        } else if (path.equalsIgnoreCase("sdk")) {
            rs = new SDK().sdkPostMD(query, name, value);
        } else if (path.startsWith("tutorial")) {
            rs = new Tutorial().postMD(getService(path), profile, resource, query, name, value, transform, defaultGraphUris, namedGraphUris);
        } else if (path.startsWith("process")) {
            rs = new Processor().typecheckPost_MD(resource, transform, getService(path));
        } else {
            rs = Response.status(Response.Status.BAD_REQUEST).header(headerAccept, "*").entity("Can not get right service solver.").build();
        }

        return Response.status(rs.getStatus()).header(headerAccept, "*").entity(wrapper(rs).toString()).build();
    }

    //Put the response text in the #content of home page
    private StringBuilder wrapper(Response rs) {
        StringBuilder html = new StringBuilder();
        //if not using ajax, donot wrap
        if (!SPARQLRestAPI.isAjax) {
            return html.append(rs.getEntity().toString());
        }

        try {
            String home = EmbeddedJettyServer.resourceURI.getPath() + "/" + HOME_PAGE;//get file path
            html = Utility.readFile(home);//read file
            int pos = html.indexOf(CONTENT_HTML) + CONTENT_HTML.length();//find place to insert
            html.replace(pos, pos, rs.getEntity().toString());//insert content
        } catch (IOException ex) {
            Logger.getLogger(SrvWrapper.class.getName()).log(Level.WARNING, "can not read home page");
        }
        return html;
    }

    //get the string after first "/"
    private String getService(String s) {
        return (s == null || s.isEmpty()) ? "" : s.substring(s.indexOf("/") + 1);
    }
}
