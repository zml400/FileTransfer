package com.nbst.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.nbst.entity.Result;
import com.nbst.entity.ServerInfo;
import com.nbst.util.FileUtil;
import com.nbst.util.MD5Utils;
import com.nbst.util.SSHUtil;
/**
 * @author zml
 * @date 2019年8月15日 下午4:53:53
 */
@Service
public class FileTransferServiceImpl implements IFileTransferService {
	private static final Logger log = LoggerFactory.getLogger(FileTransferServiceImpl.class);
	private ScheduledExecutorService service;
	private ScheduledFuture<?> future;
	
	private int flag=0;  //flag=1正在上传，0：没有上传任务

	//用于存储添加的主机信息
	private static List<ServerInfo> clientInfoList=new ArrayList<ServerInfo>();
	
	/**
	 * 将多台主机信息添加到数组中
	* @author zhangml
	* @param clientInfo
	* @return
	* @date 2019年8月21日 下午2:10:14
	 */
	@Override
	public Result addClientInfo(ServerInfo clientInfo) {
		clientInfoList.add(clientInfo);
		System.out.println(clientInfo);
		return Result.success("新增成功");
	}
	
	
	/**
	 * 实现从多台主机指定文件夹中读取文件上传到文件服务器中指定文件夹中
	* @author zhangml
	* @param serverInfo
	* @param clientDirPath
	* @param serverDirPath
	* @return
	* @date 2019年8月21日 下午2:08:14
	 */
	@Override
	public Result mutilClientFileTransferToServer(ServerInfo serverInfo,String clientDirPath,String serverDirPath) {
		for(ServerInfo clientInfo:clientInfoList) {
			clientFileTransferToServer(serverInfo,clientInfo,clientDirPath,serverDirPath);
		}
		return Result.success("文件上传成功");
	}
	
	/**
	 * 实现从单台主机指定文件夹中读取文件上传到文件服务器中指定文件夹中
	 * 
	 * @author zhangml
	 * @param serverInfo    文件服务器信息
	 * @param clientInfo    主机信息
	 * @param clientDirPath 主机文件夹路径
	 * @param serverDirPath 文件服务器文件夹路径
	 * @date 2019年8月16日 上午11:33:13
	 */
	@Override
	public Result clientFileTransferToServer(ServerInfo serverInfo,ServerInfo clientInfo,String clientDirPath,String serverDirPath) {

		//获取主机文件夹中文件名称
		String cmdString = "cd " + clientDirPath + "&&ls";
		String resultString = SSHUtil.runSSH(clientInfo, cmdString);
		String[] fileNameStrings = resultString.split(System.lineSeparator());
		// 本地临时存放文件的路径
		String localDirPath = System.getProperty("user.dir").replace("bin", "webapps")+ "/document/upload/";
		for (String nameString : fileNameStrings) {
			String[] name = nameString.split("\\.");
			// 判断文件是否在记录中存在
			String[] record = FileUtil.isExsitRecord(nameString);
			File localFile =null;
			if (record != null) { // 存在该记录
				log.info("服务器中已经存在文件"+nameString+",判断是否更新");
				// 上传文件到本地工程
				SSHUtil.getFile(clientInfo, clientDirPath + "/" + nameString, localDirPath);
				SSHUtil.closeConnection(clientInfo.getHost());
				// 读取该文件
				localFile = new File(localDirPath + "/" + nameString);
				//计算该文件的MD5散列值
				String fileMD5String = MD5Utils.getFileMD5String(localFile);
				// 判断文件两次的md5值是否相等
				if (!fileMD5String.equals(record[4])) { // md5值不相等，代表文件已经更新
					log.info("文件"+nameString+"已经更新，需要上传");
					// 上传文件
					SSHUtil.putFile(serverInfo, localFile.getPath(), serverDirPath+"/"+name[0]);
					// 修改该文件的名称
					Long currentTimeLong = System.currentTimeMillis();
					cmdString = "cd " + serverDirPath + "/" + name[0]+ "&&mv " + nameString + " " + name[0] + "_" + currentTimeLong
							+ "_" + (Integer.valueOf(record[2]) + 1) + "." + name[1];
					SSHUtil.runSSH(serverInfo, cmdString);
					SSHUtil.closeConnection(serverInfo.getHost());
					// 将文件的提交时间和版本号写入FileRecord(覆盖写入)
					FileUtil.writeToFileRecord(nameString, currentTimeLong, Integer.valueOf(record[2]) + 1,
							clientInfo.getHost(), fileMD5String,false);
				}else {
					log.info("文件"+nameString+"没有更新，无需上传");
				}
			} else {
				log.info("服务器中没有文件"+nameString+",直接上传");
				// 上传文件到本地工程
				SSHUtil.getFile(clientInfo, clientDirPath + "/" + nameString, localDirPath);
				SSHUtil.closeConnection(clientInfo.getHost());
				// 读取该文件
				localFile = new File(localDirPath + "/" + nameString);
				//计算该文件的MD5散列值
				String fileMD5String = MD5Utils.getFileMD5String(localFile);
				//不存在该文件夹,创建该文件夹
				cmdString="cd "+serverDirPath+"&&mkdir "+name[0];
				SSHUtil.runSSH(serverInfo, cmdString);
				// 上传文件
				SSHUtil.putFile(serverInfo, localFile.getPath(), serverDirPath+"/"+name[0]);
				// 修改该文件的名称
				Long currentTimeLong = System.currentTimeMillis();
				cmdString = "cd " + serverDirPath +"/"+name[0]+ "&&mv " + nameString + " " + name[0] + "_" + currentTimeLong + "_"
						+ 1 + "." + name[1];
				SSHUtil.runSSH(serverInfo, cmdString);
				SSHUtil.closeConnection(serverInfo.getHost());
				// 将文件的提交时间和版本号写入FileRecord(追加写入)
				FileUtil.writeToFileRecord(nameString, currentTimeLong, 1, clientInfo.getHost(),
						fileMD5String, true);
			}
			localFile.delete(); //删除工程内的文件
		}
		SSHUtil.closeConnection(clientInfo.getHost());
		return Result.success("本次文件上传------成功");
	}

	/**
	 * 一键开启定时上传任务
	 * 
	 * @author zhangml
	 * @param unitTime
	 * @param serverInfo
	 * @param time
	 * @param clientDirPath
	 * @param serverDirPath
	 * @return
	 * @date 2019年8月16日 上午11:37:02
	 */
	@Override
	public Result startTimeTask(ServerInfo serverInfo, String timeUnit, String time, String clientDirPath,String serverDirPath) {
		if(flag==1) {
			return Result.success("文件上传任务已经在执行，请不要重复点击开始上传，您可以通过点击停止上传来开启新的上传任务");
		}
			//创建一个定时任务执行服务
		service=Executors.newSingleThreadScheduledExecutor();
		Runnable runnable =new Runnable() {	
            @Override
            public void run() {
            	mutilClientFileTransferToServer(serverInfo, clientDirPath,serverDirPath); 
            }
	    };
		TimeUnit tUnit=null;
		if (timeUnit.equals("day")) {
			tUnit = TimeUnit.DAYS;
		} else if (timeUnit.equals("hour")) {
			tUnit = TimeUnit.HOURS;
		} else if (timeUnit.equals("minute")) {
			tUnit = TimeUnit.MINUTES;
		} else {
			tUnit = TimeUnit.SECONDS;
		}
		flag=1;
		future=service.scheduleAtFixedRate(runnable, 0, Long.valueOf(time), tUnit);
		return Result.success("文件定时上传任务已经开启，每隔"+time+timeUnit+"上传一次");
	}
	
	/**
	 * 停止文件定时上传任务
	 */
	@Override
	public Result stopTimeTask() {
		if(flag==1) {//文件正在上传
			try {
				if(future!=null) {
					future.cancel(true);
					
				}
				flag=0;
				return Result.success("上传任务已经停止");
			} catch (Exception e) {
				return Result.success("上传任务停止失败");
			}
		}
		return Result.success("当前没有上传任务需要停止");
	}
	

}
