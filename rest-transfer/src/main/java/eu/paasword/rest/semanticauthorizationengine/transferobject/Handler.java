package eu.paasword.rest.semanticauthorizationengine.transferobject;

/**
 *
 * @author smantzouratos
 */
public class Handler {

    private String handleridentifier;
    private String restendpoint;
    
    private String domainclazzname;
    private String domainargumentinstance;
    private String rangeclazzname;   //

    private String propertyname;

    private String token;
    private String username;
    private String password;

    public Handler() {
    }

    public String getHandleridentifier() {
        return handleridentifier;
    }

    public void setHandleridentifier(String handleridentifier) {
        this.handleridentifier = handleridentifier;
    }

    public String getRestendpoint() {
        return restendpoint;
    }

    public void setRestendpoint(String restendpoint) {
        this.restendpoint = restendpoint;
    }

    public String getDomainclazzname() {
        return domainclazzname;
    }

    public void setDomainclazzname(String domainclazzname) {
        this.domainclazzname = domainclazzname;
    }

    public String getDomainargumentinstance() {
        return domainargumentinstance;
    }

    public void setDomainargumentinstance(String domainargumentinstance) {
        this.domainargumentinstance = domainargumentinstance;
    }

    public String getRangeclazzname() {
        return rangeclazzname;
    }

    public void setRangeclazzname(String rangeclazzname) {
        this.rangeclazzname = rangeclazzname;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPropertyname() {
        return propertyname;
    }

    public void setPropertyname(String propertyname) {
        this.propertyname = propertyname;
    }
}
