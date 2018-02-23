package eu.paasword.spi.response;

import java.io.Serializable;

/**
 * @param <C>
 * @param <R>
 */
public class SPIResponse<C extends Enum<? extends ResponseCode>, R> implements Serializable {

    private C code;
    private String message;
    private R returnobject;

    public SPIResponse(C code, String message, R returnobject) {
        this.message = message;
        this.code = code;
        this.returnobject = returnobject;
    }

        public SPIResponse(C code, String message) {
        this.message = message;
        this.code = code;
    }

    public C getCode() {
        return code;
    }

    public void setCode(C code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public R getReturnobject() {
        return returnobject;
    }

    public void setReturnobject(R returnobject) {
        this.returnobject = returnobject;
    }

}
