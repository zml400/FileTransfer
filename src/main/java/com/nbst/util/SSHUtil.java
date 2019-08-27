package com.nbst.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nbst.entity.ServerInfo;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.SFTPv3Client;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import lombok.Data;

/**
 * SSH工具类
* @author zml
* @date 2019年8月7日 下午4:12:02
 */
@Data
public class SSHUtil {
	
    private static final Logger log = LoggerFactory.getLogger(SSHUtil.class);
    private static String  DEFAULTCHART="UTF-8";
    private static Connection connection=null;

	/**
	 * 获得一个ssh连接
	* @author zml
	* @return
	* @date 2019年8月7日 下午4:11:19
	* @return Connection
	 */
	private static Connection getConnection(ServerInfo sInfo){
		boolean flg=false;
        if(connection==null) {
			try {
				connection = new Connection(sInfo.getHost(),sInfo.getPort());
				connection.connect();//连接
	            flg=connection.authenticateWithPassword(sInfo.getUserName(), sInfo.getUserPwd());//认证
	            if(flg){
	                log.info("连接到"+sInfo.getHost()+"------登录成功");
	                return connection;
	            }else {
	            	log.error("连接到"+sInfo.getHost()+"------认证失败");
				}
	        } catch (IOException e) {
	            log.error("连接到"+sInfo.getHost()+"------登录失败");
	            e.printStackTrace();
	        }
        }
        return connection;
	}
	
	/**
	 * 运行cmd命令
	* @author zml
	* @param cmd
	* @return
	* @date 2019年8月7日 下午4:10:47
	* @return String
	 */
	public static String runSSH(ServerInfo sInfo,String cmd){
        String result="";
		if(connection==null) {
			//获得一个ssh连接
			connection=getConnection(sInfo);
		}
		try {
            Session session= connection.openSession();//打开一个会话
            session.execCommand(cmd);//执行命令
            
            result=processStdout(session.getStdout(),DEFAULTCHART);
            //如果为得到标准输出为空，说明脚本执行出错了或者脚本没有返回结果
            if(result.isEmpty()){
            	log.info("执行命令："+cmd+"------得到标准输出为空");
                result=processStdout(session.getStderr(),DEFAULTCHART);
            }else{
            	log.info("执行命令："+cmd+"------成功");
            }
            session.close();
        } catch (IOException e) {
            log.error("执行命令："+cmd+"  "+"------失败"+System.lineSeparator()+e.getMessage());
            e.printStackTrace();
        } catch (IllegalStateException e) {
			log.error("打开会话失败"+System.lineSeparator()+e.getMessage());
		}
        return result;
	}
	

	/**
	 * 解析脚本执行返回的结果集
	* @author zml
	* @param in
	* @param charset
	* @return
	* @date 2019年8月7日 下午4:09:54
	* @return String
	 */
	private static String processStdout(InputStream in, String charset){
        InputStream  stdout = new StreamGobbler(in);
        StringBuffer buffer = new StringBuffer();;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout,charset));
           // System.out.println(br.readLine());
            String line=null;
            while((line=br.readLine()) != null){
                buffer.append(line+System.lineSeparator());
            }
            br.close();
        } catch (UnsupportedEncodingException e) {
        	log.info("解析脚本执行返回的结果集------解析出错"+System.lineSeparator()+e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
        	log.error("解析脚本执行返回的结果集------解析出错："+System.lineSeparator()+e.getMessage());
            e.printStackTrace();
        }
        return buffer.toString();
    }
	
	/**
	 * 从服务器下载文件
	* @author zml
	* @param remoteFile
	* @param localTargetDirectory
	* @date 2019年8月7日 下午4:08:50
	* @return void
	 */
	public static void getFile(ServerInfo sInfo,String remoteFile, String localTargetDirectory) {
		if(connection==null) {
			connection=getConnection(sInfo);
		}
        try {
            SCPClient client = new SCPClient(connection);
            client.get(remoteFile, localTargetDirectory);
            log.info("从地址:"+sInfo.getHost()+"文件路径:"+remoteFile+"下载文件------成功");
        } catch (IOException ex) {
        	log.error("从地址:"+sInfo.getHost()+"文件路径:"+remoteFile+"下载文件------发生异常"+System.lineSeparator()+ex.getMessage());
        	ex.printStackTrace();
        }catch (IllegalStateException e) {
			log.error("地址:"+sInfo.getHost()+"打开会话失败"+System.lineSeparator()+e.getMessage());
		}
        
    }

	/**
	 * 上传文件到服务器
	* @author zml
	* @param localFileDirectory
	* @param remoteTargetDirectory
	* @date 2019年8月7日 下午4:11:40
	* @return void
	 */
    public static void putFile(ServerInfo sInfo,String localFile, String remoteTargetDirectory) {
    	if(connection==null) {
			connection=getConnection(sInfo);
		}
        try {
            SCPClient client = new SCPClient(connection);
            client.put(localFile, remoteTargetDirectory);
            log.info("向地址:"+sInfo.getHost()+"文件路径:"+remoteTargetDirectory+"上传文件------成功");
        } catch (IOException ex) {
        	log.error("向地址:"+sInfo.getHost()+"文件路径:"+remoteTargetDirectory+"上传文件------发生异常"+System.lineSeparator()+ex.getMessage());
        }catch (IllegalStateException ex) {
			log.error("地址:"+sInfo.getHost()+"打开会话失败"+System.lineSeparator()+ex.getMessage());
		}
    }
	
	public static void closeConnection(String host) {
		if(connection==null) {
			log.info("连接已经关闭，无需重复关闭");
		}else {
			connection.close();
			connection=null;
			log.info("连接"+host+"-----关闭成功");
		}
	} 
	
}
