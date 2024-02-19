package model.upload;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import model.AbstractBaseModel;

@Entity
@Table(name = "IDOS_FILES_LOG")
public class IdosFileLog extends AbstractBaseModel {

	    @Column(name = "FILE_NAME")
	    private String fileName;
	    @Column(name = "TIMESTAMP")
	    private Date timestamp;
	    @Column(name = "TABLE_NAME")
	    private String tableName;
	    @Column(name = "TOTAL_RECORDS")
	    private Long totalRecords;
	    @Column(name = "RECORDS_INSERTED")
	    private Long recordsInserted;
	    @Column(name = "RECORDS_DELETED")
	    private Long recordsDeleted;
	    @Column(name = "RECORDS_UPDATED")
	    private Long recordsUpdated;
	    @Column(name = "RECORDS_IGNORED")
	    private Long recordsIgnored;
	    @Column(name = "SEGMENT_NAME")
	    private String segmentName;
	    @Column(name = "OPERATION")
	    private String operation;

	    public String getFileName() {
	        return fileName;
	    }

	    public void setFileName(String fileName) {
	        this.fileName = fileName;
	    }

	    public Date getTimestamp() {
	        return timestamp;
	    }

	    public void setTimestamp(Date timestamp) {
	        this.timestamp = timestamp;
	    }

	    public Long getTotalRecords() {
	        return totalRecords;
	    }

	    public void setTotalRecords(Long totalRecords) {
	        this.totalRecords = totalRecords;
	    }

	    public Long getRecordsInserted() {
	        return recordsInserted;
	    }

	    public void setRecordsInserted(Long recordsInserted) {
	        this.recordsInserted = recordsInserted;
	    }

	    public Long getRecordsDeleted() {
	        return recordsDeleted;
	    }

	    public void setRecordsDeleted(Long recordsDeleted) {
	        this.recordsDeleted = recordsDeleted;
	    }

	    public Long getRecordsUpdated() {
	        return recordsUpdated;
	    }

	    public void setRecordsUpdated(Long recordsUpdated) {
	        this.recordsUpdated = recordsUpdated;
	    }

	    public Long getRecordsIgnored() {
	        return recordsIgnored;
	    }

	    public void setRecordsIgnored(Long recordsIgnored) {
	        this.recordsIgnored = recordsIgnored;
	    }

	    public String getSegmentName() {
	        return segmentName;
	    }

	    public void setSegmentName(String segmentName) {
	        this.segmentName = segmentName;
	    }

	    public String getOperation() {
	        return operation;
	    }

	    public void setOperation(String operation) {
	        this.operation = operation;
	    }

	    public String getTableName() {
	        return tableName;
	    }

	    public void setTableName(String tableName) {
	        this.tableName = tableName;
	    }
	    
	    
	}



