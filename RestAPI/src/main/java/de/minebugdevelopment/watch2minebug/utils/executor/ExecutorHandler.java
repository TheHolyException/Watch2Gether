package de.minebugdevelopment.watch2minebug.utils.executor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class ExecutorHandler {

    private int lastTaskIdentifier = 0;

    private final List<ExecutorTask> taskList;
    private final ExecutorService executorService;

    public ExecutorHandler(ExecutorService executorService) {
        this.executorService = executorService;
        this.taskList = new ArrayList<>();
    }

    public int getNewTaskIdentifier() {
        return lastTaskIdentifier++;
    }

    /**
     * Add a task to the list of tasks, and execute it.
     *
     * @param task The task to be executed.
     */
    public void putTask(ExecutorTask task) {
        task.setTaskId(getNewTaskIdentifier());
        taskList.add(task);
        executorService.execute(task);
    }

    /**
     * "Return true if there is at least one task in the task list that has the given group ID and is not completed."
     *
     * The first thing to notice is that the function is declared as public boolean. This means that the function returns a
     * boolean value and can be called from outside the class
     *
     * @param groupID The ID of the group you want to check.
     * @return A boolean value.
     */
    public boolean hasGroupRunningThreads(long groupID) {
        return taskList.stream().anyMatch(t -> (t.getGroupId() == groupID) && !t.isCompleted());
    }

    /**
     * @return true if any of the tasks in the task list are not completed.
     */
    public boolean areThreadsRunning() {
        return taskList.stream().anyMatch(t -> !t.isCompleted());
    }

    /**
     * Wait until all threads in the group have finished executing.
     *
     * @param groupID The group ID of the threads you want to wait for.
     * @param checkInterval The time in milliseconds to wait between checks.
     */
    public void awaitGroup(long groupID, long checkInterval) {
        while (hasGroupRunningThreads(groupID)) {
            try {
                Thread.sleep(checkInterval);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * "Wait for the specified group to be completed, or until the specified timeout has elapsed."
     *
     * The first parameter is the group ID. The second parameter is the timeout in seconds
     *
     * @param groupID The group ID of the group you want to await.
     */
    public void awaitGroup(long groupID) {
        awaitGroup(groupID, 20);
    }

    public void closeAfterExecution() {
        while (taskList.stream().anyMatch(t -> !t.isCompleted())) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        executorService.shutdown();
    }

    public void closeForce() {
        executorService.shutdownNow();
    }

    public List<ExecutorTask> getTaskList() {
        return taskList;
    }
}
