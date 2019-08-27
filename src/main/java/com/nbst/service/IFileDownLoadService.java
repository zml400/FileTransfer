package com.nbst.service;

import java.io.File;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.nbst.entity.Result;

/**
* @author zml
* @date 2019年8月22日 下午1:25:53
*/
public interface IFileDownLoadService {
	//下载文件
	public Result fileDownLoad(String remoteFilePath, HttpServletResponse response);
	//下载单个文件
	public Result singleFileDownLoad(String filePath, String filename,HttpServletResponse response);
	//下载多个文件
	public Result mutilFileDownLoad(List<File> files,HttpServletResponse response);
}
