package com.encryptit.exceptions;

public class KeyGenFailException extends Exception {
	static final long serialVersionUID = 1878;

	public enum failureTypes {
		KEY_NULL, DATABASE_ERROR, EMPTY_FIELD
	}

	public failureTypes fail;

	public KeyGenFailException(failureTypes failureType) {
		fail = failureType;
	}
}