
package com.nbst.service;

import com.nbst.entity.Result;
import com.nbst.entity.ServerInfo;

/**
* @author zml
* @date 2019年8月15日 下午4:53:09
*/
public interface IFileTransferService {

	//从指定主机中下载文件传输到文件服务器
	public Result clientFileTransferToServer(ServerInfo serverInfo,ServerInfo clientInfo,String clientFilePath,String serverFilePath);
	public Result startTimeTask(ServerInfo serverInfo,String unitTime,String time, String clientDirPath,String serverDirPath);	
	public Result stopTimeTask();
	public Result addClientInfo(ServerInfo clientInfo);
	public Result mutilClientFileTransferToServer(ServerInfo serverInfo,String clientDirPath,String serverDirPath);
	//普通上传
	
}
