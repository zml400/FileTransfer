package com.nbst;

import java.io.File;
import java.io.IOException;

import javax.sound.midi.Soundbank;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.nbst.entity.ServerInfo;
import com.nbst.service.IFileTransferService;
import com.nbst.util.CMDUtil;
import com.nbst.util.FileUtil;
import com.nbst.util.MD5Utils;
import com.nbst.util.SSHUtil;

/**
 * @author zhangml
 *
 */
@RunWith(SpringRunner.class)
@WebAppConfiguration // 开启web应用配置
@SpringBootTest
public class test {
    @Autowired
    private IFileTransferService fileTransferServiceImpl;
	@Test
	public void cmdTest() {
		String cmd ="pscp -pw nbst123 -ls root@47.96.190.16:/D:/package";
		System.out.println(CMDUtil.excuteCMDCommand(cmd));
	}
	@Test
	public void pathTest() {
		String path = "e:\\test.txt";
		System.out.println(FileUtil.isFileOrDir(path));
	}
	
	@Test
	public void sshTest() {
		ServerInfo sInfo=new ServerInfo();
		String cmd = "cd /&&cd d&&ls";
        System.out.println(SSHUtil.runSSH(sInfo,cmd));
	}
	
	@Test
	public void fileTest() {
//		String filePath="E:\\test.txt";
//		File file= new File(filePath);
//		System.out.println(Times.TimeToString(file.lastModified()));
		ServerInfo sInfo=new ServerInfo();
		String cmdString="cd /&&cd d&&cd test&&cd test&&ls";
		System.out.println(SSHUtil.runSSH(sInfo,cmdString));
	}
	
	@Test
	public void resultTest() {
//		String filePath = "doucument/FIleRecord.txt";
//		String fileString = FileUtil.readToString(filePath);
//		System.out.println(fileString);
		//System.out.println(nameStrings[0]);
		//System.out.println(Result.success("123"));
		
		
		String filePath = "document/FIleRecord.txt";
		String fileString = FileUtil.readToString(filePath);
		String[] recordStrings=fileString.split(System.lineSeparator());
		for(String record:recordStrings) {
			String[] str=record.split("#");
			System.out.println(str[0]);
		}
	}
	@Test
	public void fileSaveTest() throws IOException{
//		File file = new File("document/upload/test.txt"); 
//		if (!file.exists()) {
//			file.createNewFile();
//		}
		String filePath = System.getProperty("user.dir")+"/document/FileRecord.txt";
		System.out.println(filePath);
		File file = new File(filePath);
		if (!file.exists()) {
			file.createNewFile();
		}
	}
	@Test
	public void fileDownloadTest() {
		ServerInfo serverInfo = new ServerInfo();
		serverInfo.setHost("192.168.7.187");
		serverInfo.setPort(22);
		serverInfo.setUserName("zcd");
		serverInfo.setUserPwd("123456");
		String remoteFile="e:/test/test1.txt";
		String localTargetDirectory = System.getProperty("user.dir")+"/document/upload/";
		File file= new File(localTargetDirectory+"/test1.txt");
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("md5:"+MD5Utils.getFileMD5String(file));
		System.out.println("filePath:"+file.getPath());
		SSHUtil.getFile(serverInfo, remoteFile, localTargetDirectory);
	}
	@Test
	public void fileTransferTest() {
		ServerInfo serverInfo = new ServerInfo();
		serverInfo.setHost("47.96.190.16");
		serverInfo.setPort(22);
		serverInfo.setUserName("root");
		serverInfo.setUserPwd("nbst123");
		String clientFilePath="e:/test";
		String serverFilePath="d:/test";
		//fileTransferServiceImpl.clientFileTransferToServer(serverInfo, clientFilePath, serverFilePath);
	}
	@Test
	public void writeToFileTest() {
		String fileName = "test3.txt";
		Long submitTime =new Long(454313131);
		Integer VNum=2;
		String clientHost ="127.0.0.1";
		String md5String = "fdsa413a1fa4d3";
		Boolean flag =false;//追加写入
		FileUtil.writeToFileRecord(fileName, submitTime, VNum, clientHost, md5String, flag);
	}
	@Test
	public void simpleTest() {

	}
}