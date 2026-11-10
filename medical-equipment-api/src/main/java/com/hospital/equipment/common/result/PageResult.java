package com.hospital.equipment.common.result;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 分页结果体
 */
@Data
public class PageResult<T> implements Serializable {
    private Long total;
    private List<T> list;

    public PageResult(Long total, List<T> list) {
        this.total = total;
        this.list = list;
    }
}
