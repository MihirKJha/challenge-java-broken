package com.celonis.challenge.controllers;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.celonis.challenge.AppConstants;
import com.celonis.challenge.model.CountertaskResponse;
import com.celonis.challenge.model.ProjectGenerationTask;
import com.celonis.challenge.services.FileService;
import com.celonis.challenge.services.TaskService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Api("Rest controller for task execution")
@RestController
@RequestMapping(value ="/api/tasks",
	produces = MediaType.APPLICATION_JSON_VALUE,
	consumes = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class TaskController {
    
	@Autowired
    private TaskService taskService;
    
	@Autowired
    private FileService fileService;
    
	@ApiOperation("API to get all tasks")
    @GetMapping("/")
    public List<ProjectGenerationTask> listTasks() {
        return taskService.listTasks();
    }
    
	@ApiOperation("API to create task")
    @PostMapping("/")
    public ProjectGenerationTask createTask(@RequestBody @Valid ProjectGenerationTask projectGenerationTask) {
        return taskService.createTask(projectGenerationTask);
    }
    
	@ApiOperation("API to get task")
    @GetMapping("/{taskId}")
    public ProjectGenerationTask getTask(@PathVariable String taskId) {
        return taskService.getTask(taskId);
    }
   
	@ApiOperation("API to update task")
    @PutMapping("/{taskId}")
    public ProjectGenerationTask updateTask(@PathVariable String taskId,
                                            @RequestBody @Valid ProjectGenerationTask projectGenerationTask) {
        return taskService.update(taskId, projectGenerationTask);
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable String taskId) {
        taskService.delete(taskId);
    }
    
    @ApiOperation("API to execute task")
    @PostMapping("/{taskId}/execute")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void executeTask(@PathVariable String taskId) {
    	fileService.executeTask(taskId);
    }
    
    @ApiOperation("API to get task result")
    @GetMapping("/{taskId}/result")
    public ResponseEntity<FileSystemResource> getResult(@PathVariable String taskId) {
        return fileService.getTaskResult(taskId);
    }
    
    @ApiOperation("API to execute counter task")
    @PostMapping(value= "/executeCounter/{x}/{y}",
    		 produces = MediaType.APPLICATION_JSON_VALUE,
    		 consumes = MediaType.APPLICATION_JSON_VALUE)
    public String executeCounterTask(@PathVariable int x,@PathVariable int y) {
	    	ProjectGenerationTask projectGenerationTask = new ProjectGenerationTask();
	    	projectGenerationTask.setName(AppConstants.TASK_NAME);
	    	ProjectGenerationTask createdTask = taskService.createTask(projectGenerationTask);
	    	String taskId = createdTask.getId(); 
	    	log.info("Assigned taskId {}" ,taskId);
	    	
	    	taskService.executeCounterTask(taskId,x, y);
    	    
    	    return taskId;
    }
    
    @ApiOperation("API to show task progress")
    @GetMapping("/{taskId}/showProgress")
    public CountertaskResponse showProgress(@PathVariable String taskId) {
        return taskService.showProgress(taskId);
    }
    
    @ApiOperation("API to cancel counter task")
    @PostMapping("/{taskId}/cancelTask")
    public void cancelTask(@PathVariable String taskId) {
        taskService.cancelTask(taskId);;
    }
   
    @ApiOperation("API to cleanup of tasks")
    @PostMapping("/cleanUp")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cleanUpTasks() {
    	 taskService.cleanUpTasks();
    }
}
