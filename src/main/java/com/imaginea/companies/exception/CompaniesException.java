package com.imaginea.companies.exception;

public class CompaniesException extends Exception {

  public CompaniesException(String message) {
    super(message);
  }

  public CompaniesException(String message, Throwable cause) {
    super(message, cause);
  }

  public CompaniesException(Throwable cause) {
    super(cause);
  }
}
