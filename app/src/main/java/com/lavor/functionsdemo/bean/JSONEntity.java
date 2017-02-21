package com.lavor.functionsdemo.bean;

import java.io.Serializable;

/**
 * @author liufu on 2017/2/21.
 */

public class JSONEntity<E> implements Serializable {

	private String msg;
	private String code;
	private E result;

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public E getResult() {
		return result;
	}

	public void setResult(E result) {
		this.result = result;
	}
}
