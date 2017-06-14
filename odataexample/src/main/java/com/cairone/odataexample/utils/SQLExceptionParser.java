package com.cairone.odataexample.utils;

import java.sql.SQLException;

import javax.persistence.PersistenceException;
import javax.transaction.RollbackException;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.UnexpectedRollbackException;

public class SQLExceptionParser {

	public static final String parse(Exception e) {
		if(e instanceof DataIntegrityViolationException) {
			DataIntegrityViolationException dataIntegrityViolationException = (DataIntegrityViolationException) e;
			if(dataIntegrityViolationException.getCause() != null && dataIntegrityViolationException.getCause() instanceof ConstraintViolationException) {
				ConstraintViolationException constraintViolationException = (ConstraintViolationException) dataIntegrityViolationException.getCause();
				if(constraintViolationException.getCause() != null && constraintViolationException.getCause() instanceof SQLException) {
					SQLException sqlException = (SQLException) constraintViolationException.getCause();
					return sqlException.getMessage();
				}
			}
		}
		if(e instanceof UnexpectedRollbackException) {
			UnexpectedRollbackException unexpectedRollbackException = (UnexpectedRollbackException) e;
			if(unexpectedRollbackException.getCause() != null && unexpectedRollbackException.getCause() instanceof RollbackException) {
				RollbackException rollbackException = (RollbackException) unexpectedRollbackException.getCause();
				if(rollbackException.getCause() != null && rollbackException.getCause() instanceof PersistenceException) {
					PersistenceException persistenceException = (PersistenceException) rollbackException.getCause();
					if(persistenceException.getCause() != null && persistenceException.getCause() instanceof ConstraintViolationException) {
						ConstraintViolationException constraintViolationException = (ConstraintViolationException) persistenceException.getCause();
						if(constraintViolationException.getCause() != null && constraintViolationException.getCause() instanceof SQLException) {
							SQLException sqlException = (SQLException) constraintViolationException.getCause();
							return sqlException.getMessage();
						}	
					}
				}
			}
		}
		return e.getMessage();
	}
}
