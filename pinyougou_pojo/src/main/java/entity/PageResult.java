package entity;

import java.io.Serializable;
import java.util.List;

/**
 * ��ҳ�����
 */
public class PageResult implements Serializable{
    private  long total;//�ܼ�¼��
    private List rows;  //��ǰҳ��¼

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List getRows() {
        return rows;
    }

    public void setRows(List rows) {
        this.rows = rows;
    }

    public PageResult(long total, List rows) {
        this.total = total;
        this.rows = rows;
    }
}
