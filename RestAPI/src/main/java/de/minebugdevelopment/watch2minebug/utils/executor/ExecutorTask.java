package de.minebugdevelopment.watch2minebug.utils.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutorTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger("ExecutorTask");
    private final Runnable command;
    private long taskId;
    private long groupId;
    private boolean completed;
    private long startTime;
    private long runtime;

    public ExecutorTask(Runnable command) {
        this.command = command;
        this.groupId = -1L;
        this.taskId = -1L;
        this.completed = false;
    }

    public ExecutorTask(Runnable command, long groupId) {
        this.command = command;
        this.groupId = groupId;
        this.taskId = -1L;
        this.completed = false;
    }

    public boolean isCompleted() {
        return completed;
    }

    public long getRuntime() {
        runtime = System.currentTimeMillis()- startTime;
        return runtime;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        if (this.taskId == -1L)
            this.taskId = taskId;
        else
            logger.warn("Cannot set task id, this task already has an task id ({})", this.taskId);
    }

    @Override
    public void run() {
        startTime = System.currentTimeMillis();
        try {
            command.run();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        completed = true;
        runtime = System.currentTimeMillis()- startTime;
    }

    @Override
    public String toString() {
        return "ExecutorTask{" +
                "taskId=" + taskId +
                ", groupId=" + groupId +
                ", start-time=" + startTime +
                ", runtime=" + runtime +
                '}';
    }
}
