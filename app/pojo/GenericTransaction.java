package pojo;


public class GenericTransaction {

    private Long id;
    private Long transactionPurpose;
    private String transactionRefNumber;
    private String submitter;
    private String approval;
    private String dateCreated;

    public GenericTransaction() {

    }

    public Long getTransactionPurpose() {
        return transactionPurpose;
    }

    public void setTransactionPurpose(Long transactionPurpose) {
        this.transactionPurpose = transactionPurpose;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public GenericTransaction(Long id, String transactionRefNumber, String submitter, String approval, String dateCreated) {
        this.id = id;
        this.transactionRefNumber = transactionRefNumber;
        this.submitter = submitter;
        this.approval = approval;
        this.dateCreated = dateCreated;
    }

    public String getTransactionRefNumber() {
        return transactionRefNumber;
    }

    public void setTransactionRefNumber(String transactionRefNumber) {
        this.transactionRefNumber = transactionRefNumber;
    }

    public String getSubmitter() {
        return submitter;
    }

    public void setSubmitter(String submitter) {
        this.submitter = submitter;
    }

    public String getApproval() {
        return approval;
    }

    public void setApproval(String approval) {
        this.approval = approval;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public GenericTransaction(Long id, Long transactionPurpose, String transactionRefNumber, String submitter, String approval, String dateCreated) {
        this.id = id;
        this.transactionPurpose = transactionPurpose;
        this.transactionRefNumber = transactionRefNumber;
        this.submitter = submitter;
        this.approval = approval;
        this.dateCreated = dateCreated;
    }
}
