package com.nbst.entity;

import lombok.Data;

@Data
public class Result {
	private Integer code;
	private String message;
	private Object data;
	
	public Result() {}
	public Result(Integer code) {
		this.code=code;
	}
	public Result(Integer code,String message) {
		this(code);
		this.message=message;
	}
	public Result(Integer code,String message,Object data) {
		this(code,message);
		this.data=data;
	}
	public static Result success() {
		return new Result(0000,"操作成功");
	}
	public static Result success(String message) {
		return new Result(0000,message);
	}
	public static Result success(Integer code,String message) {
		return new Result(code, message);
	}
	public static Result success(Integer code,String message,Object data) {
		return new Result(code, message, data);
	}
	
	public static Result failed() {
		return new Result(1111,"操作失败");
	}
	public static Result failed(String message) {
		return new Result(1111, message);
	}
	public static Result failed(Integer code,String message) {
		return new Result(code, message);
	}
	public static Result failed(Integer code,String message,Object data) {
		return new Result(code, message,data);
	}
}
