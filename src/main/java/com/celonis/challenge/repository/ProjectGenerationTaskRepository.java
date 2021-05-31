package com.celonis.challenge.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.celonis.challenge.model.ProjectGenerationTask;

/**
 * JPA repository for ProjectGenerationTask table 
 * 
 * @author User
 *
 */
@Repository
public interface ProjectGenerationTaskRepository extends JpaRepository<ProjectGenerationTask, String> {
	List<ProjectGenerationTask> findAllByStorageLocation(String storageLocation);
}