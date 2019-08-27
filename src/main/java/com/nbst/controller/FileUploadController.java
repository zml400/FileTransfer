package com.nbst.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.nbst.entity.Result;
import com.nbst.entity.ServerInfo;
import com.nbst.service.IFileUploadService;
import com.nbst.util.SSHUtil;

/**
 * @author zhangml
 *
 */

@RequestMapping("/file")
@Controller
public class FileUploadController {
	@Autowired
	private IFileUploadService fileUploadServiceImpl;
	
	
	/*
	 * @RequestMapping(value = "uploadTest",method = RequestMethod.POST)
	 * 
	 * @ResponseBody public void uploadTest(ServerInfo serverInfo,String
	 * localFilePath,String remoteTargetDirectory) { SSHUtil.putFile(serverInfo,
	 * localFilePath, remoteTargetDirectory); }
	 */
	
	
	@RequestMapping(value = "/startUpload.action",method = RequestMethod.POST)
	@ResponseBody
	public Result startFIleUpload(ServerInfo serverInfo,@RequestParam("files") MultipartFile[] files,
			@RequestParam("lastModefy") String[] lastModefy,String remoteFilePath,Integer time,String  timeUnit) {
		return fileUploadServiceImpl.startUpload(serverInfo, files, lastModefy,remoteFilePath, time,timeUnit);
	}
	
	@RequestMapping(value = "/upload.action",method = RequestMethod.POST)
	@ResponseBody
	public Result fileUpload(ServerInfo serverInfo,@RequestParam("files") MultipartFile[] files,
			@RequestParam("lastModefy") String[] lastModefy,String remoteFilePath) {
		return fileUploadServiceImpl.fileUpload(serverInfo,files,lastModefy,remoteFilePath);
	}
	@RequestMapping("/stopUpload.action")
	@ResponseBody
	public Result stopFileUpload() {
		return fileUploadServiceImpl.stopUpload();
	}
	
	

}
