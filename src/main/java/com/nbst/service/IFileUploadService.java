package com.nbst.service;

import org.springframework.web.multipart.MultipartFile;

import com.nbst.entity.Result;
import com.nbst.entity.ServerInfo;

/**
 * @author zhangml
 *
 */
public interface IFileUploadService {
	public Result fileUpload(ServerInfo serverInfo,MultipartFile[] files,String[] lastModefy,String remoteFilePath);
	public Result startUpload(ServerInfo serverInfo,MultipartFile[] files,String[] lastModefy,String remoteFilePath,Integer time,String timeUnit);
	public Result stopUpload();
	
	
	
}
