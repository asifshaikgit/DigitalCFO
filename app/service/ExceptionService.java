package service;

public interface ExceptionService extends BaseService{
	public void sendExceptionReport(String exceptionTrace,String userEmail,String organizationName,String methodName);
}
