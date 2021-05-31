package com.celonis.challenge.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.celonis.challenge.exceptions.InternalException;
import com.celonis.challenge.exceptions.NotFoundException;
import com.celonis.challenge.model.ProjectGenerationTask;
import com.celonis.challenge.repository.ProjectGenerationTaskRepository;

/**
 * Service class to handle file related operations
 * 
 * @author User
 *
 */
@Service
public class FileService {
    
	@Autowired
    private  ProjectGenerationTaskRepository projectGenerationTaskRepository;
    
	/**
	 * Method to get task execution results
	 * 
	 * @param taskId
	 * @return
	 */
    public ResponseEntity<FileSystemResource> getTaskResult(String taskId) {
        ProjectGenerationTask projectGenerationTask = get(taskId);
        File inputFile = new File(projectGenerationTask.getStorageLocation());

        if (!inputFile.exists()) {
            throw new InternalException("File not generated yet");
        }

        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        respHeaders.setContentDispositionFormData("attachment", "challenge.zip");

        return new ResponseEntity<>(new FileSystemResource(inputFile), respHeaders, HttpStatus.OK);
    }
   
    /**
     * Method to stored result in local temp folder
     * 
     * @param taskId
     * @param url
     * @throws IOException
     */
    public void storeResult(String taskId, URL url) throws IOException {
        ProjectGenerationTask projectGenerationTask = get(taskId);
        File outputFile = File.createTempFile(taskId, ".zip");
        outputFile.deleteOnExit();
        projectGenerationTask.setStorageLocation(outputFile.getAbsolutePath());
        projectGenerationTaskRepository.save(projectGenerationTask);
        try (InputStream is = url.openStream();
             OutputStream os = new FileOutputStream(outputFile)) {
            IOUtils.copy(is, os);
        }
    }
    
    /**
     * Method to get task id details from DB
     * 
     * @param taskId
     * @return
     */
    private ProjectGenerationTask get(String taskId) {
        Optional<ProjectGenerationTask> projectGenerationTask = projectGenerationTaskRepository.findById(taskId);
        return projectGenerationTask.orElseThrow(NotFoundException::new);
    }
    
    /**
     * Method to execute task
     * 
     * @param taskId
     */
    public void executeTask(String taskId) {
        URL url = Thread.currentThread().getContextClassLoader().getResource("challenge.zip");
        if (url == null) {
            throw new InternalException("Zip file not found");
        }
        try {
            storeResult(taskId, url);
        } catch (Exception e) {
            throw new InternalException(e);
        }
    }
}
