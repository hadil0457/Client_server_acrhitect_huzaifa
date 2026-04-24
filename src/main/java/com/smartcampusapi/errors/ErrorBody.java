package com.smartcampusapi.errors;

// simple pojo for sending error responses back as json
// keeps things consistent across all our exception mappers
public class ErrorBody {

    private int statusCode;
    private String error;
    private String detail;

    public ErrorBody() {}

    public ErrorBody(int statusCode, String error, String detail) {
        this.statusCode = statusCode;
        this.error = error;
        this.detail = detail;
    }

    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
}

