
package eu.paasword.drools;

/**
 *
 * @author ubuntu
 */
public class Advice {
    
    private String advice;
    private String rulename;
    private String requestid;

    public Advice(String advice, String requestid) {
        this.advice = advice;
        this.requestid = requestid;
    }

    public Advice(String advice, String requestid, String rulename) {
        this.advice = advice;
        this.rulename = rulename;
        this.requestid = requestid;
    }

    public Advice(String requestid) {
        this.requestid = requestid;
    }

    public String getRulename() {
        return rulename;
    }

    public void setRulename(String rulename) {
        this.rulename = rulename;
    }

    public String getAdvice() {
        return advice;
    }

    public void setAdvice(String advice) {
        this.advice = advice;
    }

    public String getRequestid() {
        return requestid;
    }

    public void setRequestid(String requestid) {
        this.requestid = requestid;
    }    
    
}
