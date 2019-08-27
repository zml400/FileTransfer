package com.nbst.service;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.nbst.entity.Result;
import com.nbst.entity.ServerInfo;
import com.nbst.util.FileUtil;
import com.nbst.util.SSHUtil;

/**
 * 
* @author zml
* @date 2019年8月7日 下午4:16:08
 */
@Service
public class FileUploadServiceImpl implements IFileUploadService{
	private ScheduledExecutorService service;
	private ScheduledFuture<?> future;
	
	private int flag=0;  //flag=1开始上传，0：默认状态
	
	
	/**
	 * 本地文件上传函数
	 */
	@Override
	public Result fileUpload(ServerInfo serverInfo,MultipartFile[] multipartFiles,String[] lastModefy,String remoteFilePath) {
		System.out.println("host:"+serverInfo.getHost());
		flag=1;  //开始上传

//		//判断该路径是文件还是文件夹
//		List<File> files=new ArrayList<File>();
//		if(FileUtil.isFileOrDir(localfilePath)==0) {//0：文件
//			File file = new File(localfilePath);
//			files.add(file);
//		}else if(FileUtil.isFileOrDir(localfilePath)==1){//1：文件夹
//			File file = new File(localfilePath);
//			files =Arrays.asList(file.listFiles());
//		}else {
//			//路径为空
//			return Result.failed("本地路径不存在");
//		}
		//将文件上传到工程中
		File[] files = new File[multipartFiles.length];
		for(int i=0;i<multipartFiles.length;i++) {  //将文件转存到工程的document/upload目录
			try {
				File file2 = new File(System.getProperty("user.dir")+"/document/upload/"+multipartFiles[i].getOriginalFilename()); 
				if(!file2.exists()) {
					file2.createNewFile();
					multipartFiles[i].transferTo(file2);     //写入文件
					System.out.println("文件不存在，创建文件");
				}else {
					System.out.println("文件已经存在");
				}
				//TODO  再次写入有问题
				//multipartFiles[i].transferTo(file2);     //写入文件
				files[i]=file2;
				
			} catch (IOException e) {
				System.out.println("写入文件失败"+e.getMessage());
			}
		}
		System.out.println("文件个数："+files.length);
		for(int i=0;i<files.length;i++) {
			File file = files[i];
			//获取文件名的前缀和
			String[] nameStr=file.getName().split("\\.");
			System.out.println("fileName:"+nameStr[0]);
			//文件是否存在标志
			int fileExist=0;  //先置为不存在
			//判断服务器目标文件夹中是否有该文件对应的文件夹
			String cmdString="cd /&&cd d&&cd test&&ls";
			String resuString=SSHUtil.runSSH(serverInfo,cmdString);
			if(!resuString.isEmpty()) {  
				String[] strs= resuString.split(System.lineSeparator());
				for(String str:strs) {
					if(str.equals(nameStr[0])) {//存在该文件夹
						fileExist=1;
						//判断该文件夹中文件的最近更新时间,若两次更新时间相同则取消上传
						int VNum=FileUtil.updateTimeIsEqual(file.getName(),Long.valueOf(lastModefy[i]));
						if(VNum>0) {//两次更新时间不相同,上传文件
							//将文件上传到该文件夹
							SSHUtil.putFile(serverInfo, file.getPath(), remoteFilePath+"/"+nameStr[0]);
							//修改该文件的名称
							Long currentTimeLong=System.currentTimeMillis();
							cmdString="cd /&&cd d&&cd test&&cd "+nameStr[0]+"&&mv "+file.getName()+" "+nameStr[0]+"_"+currentTimeLong+"_"+(VNum+1)+"."+nameStr[1];
							SSHUtil.runSSH(serverInfo, cmdString);
							//将文件的最后更改时间和版本号写入FileRecord(覆盖写入)
							//FileUtil.writeToFileRecord(file.getName(),Long.valueOf(lastModefy[i]), VNum+1, false);
						}else if(VNum==-2){  //不存在文件
							fileExist=0;
							System.out.println("不存在文件");
						}else {//两次更新时间相同
							System.out.println("文件没有更新，无需上传");
						}
						break;
					}
				}
				System.out.println("");
			}
			if(fileExist==0){
				//第一次上传该文件
				//不存在该文件夹,创建该文件夹
				cmdString="cd /&&cd d&&cd test&&mkdir "+nameStr[0];
				SSHUtil.runSSH(serverInfo, cmdString);
				//将文件上传到该文件夹
				SSHUtil.putFile(serverInfo, file.getPath(), remoteFilePath+"/"+nameStr[0]);
				//修改该文件的名称
				Long currentTimeLong=System.currentTimeMillis();
				cmdString="cd /&&cd d&&cd test&&cd "+nameStr[0]+"&&mv "+file.getName()+" "+nameStr[0]+"_"+currentTimeLong+"_1"+"."+nameStr[1];
				SSHUtil.runSSH(serverInfo, cmdString);
				//将文件的最后更改时间和版本号写入FileRecord(追加写入)
				//FileUtil.writeToFileRecord(file.getName(),Long.valueOf(lastModefy[i]), 1, true);
			}
		}
		//删除工程中的文件
//		for(File file:files) {
//			if(file.exists()) {
//				file.delete(); 	
//			}
//		}
		//关闭ssh连接
		SSHUtil.closeConnection(serverInfo.getHost());
		return Result.success("文件上传成功");
	}
	
	
	/**
	 * 定时任务：
	 * 当flag=0时，每隔time时间执行一次文件上传
	 */
	@Override
	public Result startUpload(ServerInfo serverInfo,MultipartFile[] files,String[] lastModefy,String remoteFilePath,Integer time,String timeUnit) {
		
		if(flag==1) {
			return Result.success("文件上传任务已经在执行，请不要重复点击开始上传，您可以通过点击停止上传来开启新的上传任务");
		}
		if(flag==0) {
			//创建一个定时任务执行服务
			service=Executors.newSingleThreadScheduledExecutor();
			Runnable runnable =new Runnable() {	
	            @Override
	            public void run() {
	            	fileUpload(serverInfo, files, lastModefy,remoteFilePath); 
	            }
	        };
	        TimeUnit tUnit;
	        if(timeUnit.equals("day")) {
	        	tUnit=TimeUnit.DAYS;
	        }else if(timeUnit.equals("hour")) {
	        	tUnit=TimeUnit.HOURS;
	        }else if(timeUnit.equals("minute")){
	        	tUnit=TimeUnit.MINUTES;
			}else {
				tUnit=TimeUnit.SECONDS;
			}
			future=service.scheduleAtFixedRate(runnable, 0, time, tUnit);
	    }
		return Result.success("上传任务已经开启");
	}
	
	/**
	 * 停止文件定时上传任务
	 */
	@Override
	public Result stopUpload() {
		if(flag==1) {//文件正在上传
			try {
				if(future!=null) {
					future.cancel(true);
				}
				flag=0;
				System.out.println("文件上传任务停止成功");
				return Result.success("文件上传任务已经停止");
			} catch (Exception e) {
				return Result.success("停止任务失败");
			}
		}
		return Result.success("当前没有上传任务需要停止");
	}
	
	
	
}
