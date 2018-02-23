
package eu.paasword.rest.dbproxy.transferobject;

/**
 *
 * @author Panagiotis Gouvas (pgouvas@ubitech.eu)
 */
public class TDistributedTransactionContext {
    
    String appinstanceid;
    String tid;

    public TDistributedTransactionContext() {
    }    
    
    public TDistributedTransactionContext(String appinstanceid, String tid) {
        this.appinstanceid = appinstanceid;
        this.tid = tid;
    }

    public String getAppinstanceid() {
        return appinstanceid;
    }

    public void setAppinstanceid(String appinstanceid) {
        this.appinstanceid = appinstanceid;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }    
    
}
