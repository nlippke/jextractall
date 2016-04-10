package com.github.jextractall.ui;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import com.github.jextractall.ui.model.ConfigModel;
import com.github.jextractall.ui.model.ExtractorTask;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Worker.State;

public class TaskManager {

    private ForkJoinPool executor;
    private List<ExtractorTask> tasks;
    private SimpleBooleanProperty runningProperty;
    private TaskCallback callback;
    private int numProcesses = 1;
    
    
    public TaskManager(List<ExtractorTask> tasks) {
        this.tasks = tasks;
        runningProperty = new SimpleBooleanProperty(false);
    }

    public void registerCallback(TaskCallback callback) {
    	this.callback = callback;
    }
    
    public void unregisterCallback(TaskCallback callback) {
    	this.callback = null;
    }
    
    public int getNumProcesses() {
    	return numProcesses;
    }
    
    public void setNumProcesses(int num) {
    	this.numProcesses = num;
    }
    
    public void runTasks(ConfigModel config) {
    	
    	resetTasks();
    	
    	if (executor == null || executor.getParallelism() != numProcesses) {
    		if (executor != null) {
    			executor.shutdownNow();
    		}
    		executor = new ForkJoinPool(numProcesses);
    	}
    	
        tasks.stream()
             .filter(task -> task.getState() == State.READY)
             .forEach(task -> {
                 task.setConfig(config);
            	 task.setOnSucceeded(e -> {
                     if (executor.isQuiescent()) {
                         runningProperty.set(false);
                     }
                     if (callback != null) {
                    	 callback.onComplete(task);
                     }
                 });
                 task.setOnFailed(e -> {
                     if (executor.isQuiescent()) {
                         runningProperty.set(false);
                     }
                     if (callback != null) {
                    	 callback.onFailure(task);
                     }
                 });
                 task.setOnCancelled(e -> {
                     if (executor.isQuiescent()) {
                         runningProperty.set(false);
                     }
                     if (callback != null) {
                    	 callback.onCancelled(task);
                     }
                 });
                 executor.execute(task);
             });
        
        if (!executor.isQuiescent()) {
        	runningProperty.set(true);
        }
    }

    private void resetTasks() {
    	tasks.replaceAll(t -> t.isCancelled() ? t.copyTask() : t);
	}

	public void addTaskToTaskList(ExtractorTask task) {
        if (!tasks.contains(task)) {
            tasks.add(task);
        }
    }

    public void removeTaskFromTaskList(List<ExtractorTask> selectedItems) {
        List<ExtractorTask> notRunning = selectedItems.stream()
                .filter(t -> !t.isRunning())
                .collect(Collectors.toList());
        notRunning.stream().forEach(t -> t.cancel());
        tasks.removeAll(notRunning);
    }
    
    public void removeAllTasks() {
    	removeTaskFromTaskList(tasks);
    }
    
    public ReadOnlyBooleanProperty runningProperty() {
        return runningProperty;
    }

    public boolean isRunning() {
    	return runningProperty.get();
    }

	public void stopTasks() {
		tasks.stream()
		.filter(ExtractorTask::isCancellable)
		.forEach(t -> { 
			t.cancel(); 
			try {t.get();} catch (Exception e) {}
		});
		runningProperty.set(false);
	}

	public boolean hasQueuedTasks() {
		if (executor.hasQueuedSubmissions()) {
			return true;
		}
		return tasks.stream().filter(t -> t.getState() == State.READY).count() > 0; 
	}
}
