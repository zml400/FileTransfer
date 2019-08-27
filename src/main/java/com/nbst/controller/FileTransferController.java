package com.nbst.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nbst.entity.Result;
import com.nbst.entity.ServerInfo;
import com.nbst.service.IFileTransferService;

/**
* @author zml
* @date 2019年8月20日 上午10:58:01
*/
@RequestMapping("/file")
@Controller
public class FileTransferController {
	@Autowired
	private IFileTransferService fileTransferServiceImpl;
	@RequestMapping(value = "/startTransfer.action",method = RequestMethod.POST)
	@ResponseBody
	public Result startTransfer(ServerInfo serverInfo,String timeUnit,String time,String clientDirPath,String serverDirPath) {
		return fileTransferServiceImpl.startTimeTask(serverInfo, timeUnit, time, clientDirPath, serverDirPath);
	}
	
	@RequestMapping(value = "/stopTransfer.action",method = RequestMethod.POST)
	@ResponseBody
	public Result stopTransfer() {
		return fileTransferServiceImpl.stopTimeTask();
	}
	
	/**
	 * 添加主机信息
	* @author zhangml
	* @param clientInfo
	* @return
	* @date 2019年8月21日 下午4:39:33
	 */
	@RequestMapping(value = "/addClient.action",method = RequestMethod.POST)
	@ResponseBody
	public Result addClient(ServerInfo clientInfo) {
		return fileTransferServiceImpl.addClientInfo(clientInfo);
	}
	
	/**
	 * 简单上传
	* @author zhangml
	* @param serverInfo
	* @param clientDirPath
	* @param serverDirPath
	* @return
	* @date 2019年8月21日 下午5:04:13
	 */
	@RequestMapping(value = "/simpleTransfer.action",method = RequestMethod.POST)
	@ResponseBody
	public Result simpleTransfer(ServerInfo serverInfo,String clientDirPath,String serverDirPath) {
		return fileTransferServiceImpl.mutilClientFileTransferToServer(serverInfo, clientDirPath, serverDirPath);
	}
	
	
	
	
}
