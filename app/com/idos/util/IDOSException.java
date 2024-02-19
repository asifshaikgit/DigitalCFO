/* Project: IDOS 1.0
 * Module: Idos exception handling wrapper
 * Filename: IDOSException.java
 * Component Realisation: Java Class
 * Prepared By: Sunil Namdev
 * Description: Modules to Idos exception handling
 * Copyright (c) 2016 IDOS
 * MODIFICATION HISTORY
 * Version      Date                    Author            Remarks
 * -------------------------------------------------------------------------
 *  0.1         Aug 31, 2016                              - Initial Version
 * -------------------------------------------------------------------------
 */

package com.idos.util;


/**
 * This is a custom Exception class that acts as a wrapper for all kinds of exception that might be encountered.
 * Created by Sunil Namdev on 31-08-2016.
 */
public class IDOSException extends Exception{

    /**
     * Creates a IDOSException object using the errorCode, errorType and errorText as parameters
     */

    public IDOSException(String errorCode, String errorType, String errorDescription, String errorText) throws IDOSException {
        super(errorText);
       // if (IdosUtil.isNull(errorCode) || IdosUtil.isNull(errorType) || IdosUtil.isNull(errorDescription) || IdosUtil.isNull(errorText)) {
         //   throw new IDOSException(IdosConstants.NULL_KEY_EXC_ESMF,IdosConstants.TECHNICAL_EXCEPTION, "IDOSException :: IDOSException",IdosConstants.NULL_KEY_EXC_ESMF_MSG);
        //}
        this.errorCode = errorCode;
        this.errorType = errorType;
        this.errorDescription = errorDescription;
        this.errorText = errorText;
    }
    /**
     *
     * @return errorCode
     */

    public String getErrorCode() {
        return errorCode;

    }

    /**
     *
     * @return errorType
     */

    public String getErrorType() {
        return errorType;

    }
    /**
     *
     * @return errorText
     */


    public String getErrorText() {
        return errorText;

    }

    public String getErrorDescription() {
        return errorDescription;

    }

    /**
     * Error code that describes the error
     */

    private String errorCode;

    /**
     * Error Type (Business/Technical)
     */

    private String errorType;

    /**
     * Error Text that will have the description of the error
     */

    private String errorText;

    private String errorDescription;
}
