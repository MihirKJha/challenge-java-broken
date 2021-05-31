package com.celonis.challenge.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Entity class
 * 
 * @author User
 *
 */
@Entity
@Getter
@Setter
@ToString
public class ProjectGenerationTask {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    private String name;
    private Date creationDate;
    @JsonIgnore
    private String storageLocation;
    private String status;
}
