package io.terminus.doctor.workflow.core;

/**
 * Desc: 工作流异常处理类
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/25
 */
public class WorkFlowException extends RuntimeException {
    private static final long serialVersionUID = 3632097572379124998L;

    public WorkFlowException() {
        super();
    }
    public WorkFlowException(String message) {
        super(message);
    }
    public WorkFlowException(String message, Throwable cause) {
        super(message);
        super.initCause(cause);
    }
    public WorkFlowException(Throwable cause) {
        super();
        super.initCause(cause);
    }
}
