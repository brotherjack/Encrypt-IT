package com.encryptit.exceptions;

public class FileTooBigException extends Exception {
	static final long serialVersionUID = 1877;

	public FileTooBigException(String msg) {
		super(msg);
	}
}