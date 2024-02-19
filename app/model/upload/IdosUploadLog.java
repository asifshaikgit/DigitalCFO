package model.upload;

	import javax.persistence.*;

import model.AbstractBaseModel;

import java.util.Date;
	import java.util.List;

	@Entity
	@Table(name="IDOS_UPLOADLOG")
	public class IdosUploadLog extends AbstractBaseModel  {
	    @Id
	    @GeneratedValue(strategy=GenerationType.IDENTITY)
	    @Column(name="FILENAME")
	    private String fileName;
	    @Column(name="TIMESTAMP")
	    private Date timestamp;
	    @Column(name="TOTALRECORDS")
	    private Long   totalRecords;
	    @Column(name="RECORDSINSERTED")
	    private Long   recordsInserted;
	    @Column(name="RECORDSDELETED")
	    private Long   recordsDeleted;
	    @Column(name="RECORDSUPDATED")
	    private Long   recordsUpdated;
	    @Column(name="RECORDSIGNORED")
	    private Long   recordsIgnored;
	    @Column(name="STATUS")
	    private String status;
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

	    public String getStatus() {
	        return status;
	    }

	    public void setStatus(String status) {
	        this.status = status;
	    }

	    public String getOperation() {
	        return operation;
	    }

	    public void setOperation(String operation) {
	        this.operation = operation;
	    }

	    @Override
	    public String toString() {
	        return "IdosUploadLog{" +
	                "fileName='" + fileName + '\'' +
	                ", timestamp=" + timestamp +
	                ", totalRecords=" + totalRecords +
	                ", recordsInserted=" + recordsInserted +
	                ", recordsDeleted=" + recordsDeleted +
	                ", recordsUpdated=" + recordsUpdated +
	                ", recordsIgnored=" + recordsIgnored +
	                ", status='" + status + '\'' +
	                '}';
	    }
	}


