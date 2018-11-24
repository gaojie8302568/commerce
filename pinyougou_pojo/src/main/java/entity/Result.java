package entity;

import java.io.Serializable;

/**
 * ���ؽ��
 */
public class Result implements Serializable{
    private boolean success;//�Ƿ�ɹ�
    private String message;//���ص���Ϣ

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Result(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
