package com.celonis.challenge.services;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.celonis.challenge.AppConstants;
import com.celonis.challenge.exceptions.InternalException;
import com.celonis.challenge.exceptions.NotFoundException;
import com.celonis.challenge.model.CountertaskResponse;
import com.celonis.challenge.model.ProjectGenerationTask;
import com.celonis.challenge.repository.ProjectGenerationTaskRepository;

import lombok.extern.slf4j.Slf4j;


/**
 * Service class to handle general task related operations
 * 
 * @author User
 *
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
@Slf4j
public class TaskService {

    @Autowired
    private  ProjectGenerationTaskRepository projectGenerationTaskRepository;
    
    private final ConcurrentMap<String, CountertaskResponse> map = new ConcurrentHashMap<>();
    
    /**
     * Method to get all task list
     * 
     * @return
     */
    public List<ProjectGenerationTask> listTasks() {
        return projectGenerationTaskRepository.findAll();
    }
    
    /**
     * Method to create task in DB
     * 
     * @param projectGenerationTask
     * @return
     */
    public ProjectGenerationTask createTask(ProjectGenerationTask projectGenerationTask) {
        projectGenerationTask.setId(null);
        projectGenerationTask.setCreationDate(new Date());
        projectGenerationTask.setStatus(AppConstants.STATUS_NORMAL);
        
        return projectGenerationTaskRepository.save(projectGenerationTask);
    }
   
    /**
     * Method to get task by task id 
     * 
     * @param taskId
     * @return
     */
    public ProjectGenerationTask getTask(String taskId) {
        return get(taskId);
    }
    
    /**
     * Method to update task details in DB
     * 
     * @param taskId
     * @param projectGenerationTask
     * @return
     */
    public ProjectGenerationTask update(String taskId, ProjectGenerationTask projectGenerationTask) {
        ProjectGenerationTask existing = get(taskId);
        existing.setCreationDate(projectGenerationTask.getCreationDate());
        existing.setName(projectGenerationTask.getName());
        
        return projectGenerationTaskRepository.save(existing);
    }
   
    /**
     * Method to delete task by id
     * 
     * @param taskId
     */
    public void delete(String taskId) {
        projectGenerationTaskRepository.deleteById(taskId);
    }
   
    /**
     * Method to get task details by id
     * 
     * @param taskId
     * @return
     */
    private ProjectGenerationTask get(String taskId) {
        Optional<ProjectGenerationTask> projectGenerationTask = projectGenerationTaskRepository.findById(taskId);
        return projectGenerationTask.orElseThrow(NotFoundException::new);
    }
    
    /**
     * Async method to execute counter task in backgroup using threads
     * 
     * @param taskId
     * @param x
     * @param y
     */
    @Async("threadPoolTaskExecutor")
    public void executeCounterTask(String taskId,int x, int y) {
    	
    	CountertaskResponse countertaskResponse=  new CountertaskResponse();
    	countertaskResponse.setX(x);
    	countertaskResponse.setY(y);
    	map.put(taskId, countertaskResponse);

    	while(x <y) {
    		
    		log.info("x -->  {}" ,x);
    		
    		if(Thread.currentThread().isInterrupted() ) {
    			return ;
    		}

    		synchronized (map) {
    			CountertaskResponse countertask =  map.get(taskId);
    			
    			if(countertask ==null) {
    				return;
    			}
    			
    			countertask.setX(countertask.getX() +1);
    			map.put(taskId, countertask);
    			++x;
    		}

    		try {
    			Thread.sleep(1000);
    		} catch (InterruptedException e) {
    			throw new InternalException(AppConstants.EXECUTION_FAILED);
    		}	
    	} 
    	
    	//update status of Task in database
    	ProjectGenerationTask projectGenerationTask = getTask(taskId);
    	projectGenerationTask.setStatus(AppConstants.STATUS_COMPLETED);
    	projectGenerationTaskRepository.save(projectGenerationTask);
    }
    
    /**
     * Method to show progress of task execution
     * 
     * @param taskId
     * @return
     */
    public CountertaskResponse showProgress(String  taskId) {
    	return map.get(taskId);
    }
    
    /**
     * Method to show progress of task execution
     * 
     * @param taskId
     * @return
     */
    public void cancelTask(String  taskId) {
    	map.remove(taskId);
    	
    	//update status of Task in database
    	ProjectGenerationTask projectGenerationTask = getTask(taskId);
    	projectGenerationTask.setStatus(AppConstants.STATUS_CANCELLED);
    	projectGenerationTaskRepository.save(projectGenerationTask);
    }
    
    /**
     * Scheduled Method to run clean up activity of not executed task 
     * from DB using cron expresssion
     * 
     */
    @Scheduled(cron = "${cron.expression}")
    public void cleanUpTasks() {
    	List<ProjectGenerationTask> tasks =projectGenerationTaskRepository.findAllByStorageLocation(StringUtils.EMPTY);
    	log.info("Task list to be deleted  {}" ,tasks);
    	projectGenerationTaskRepository.deleteAllInBatch(tasks);
    }
}