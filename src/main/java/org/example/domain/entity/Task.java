package org.example.domain.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author romic
 * @date 2021-11-20
 */
@Data
public class Task implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 任务id
     */
    private Integer taskId;
    /**
     * 本次任务外呼线路数
     */
    private Integer calloutLineCount;
    /**
     * 主叫号码
     */
    private String callingNumber;
    /**
     * 被叫号码
     */
    private String calledNumber;

    public Task() {
    }

    public Task(Integer taskId, Integer calloutLineCount, String callingNumber, String calledNumber) {
        this.taskId = taskId;
        this.calloutLineCount = calloutLineCount;
        this.callingNumber = callingNumber;
        this.calledNumber = calledNumber;
    }
}
