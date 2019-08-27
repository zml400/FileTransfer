package com.nbst.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.nbst.entity.ServerInfo;

public class FileUtil {
	/**
	 * 判断绝对路径是文件还是文件夹 ， 0：文件，1：文件夹
	* @author zhangml
	* @param destPath
	* @return
	* @date 2019年8月12日 上午10:37:55
	 */
	
	
	private static String filePath = System.getProperty("user.dir").replace("bin", "webapps")+"/document/FileRecord.txt";
	
	
	public static Integer isFileOrDir(String destPath) {
		 File file = new File(destPath);
		 if(file.isDirectory()){
	            return 1;
	     }
		 if(file.isFile()) {
			 return 0;
		 }
		 return -1;
	}
	
	/**
	 * 获取路径下的所有文件/文件夹的绝对路径
	* @author zhangml
	* @param directoryPath 需要遍历的文件夹路径
	* @param isAddDirectory 是否将子文件夹的路径也添加到list集合中
	* @return
	* @date 2019年8月12日 上午10:38:26
	 */
    public static List<String> getAllFilePath(String directoryPath,boolean isAddDirectory) {
        List<String> list = new ArrayList<String>();
        File baseFile = new File(directoryPath);
        if (baseFile.isFile() || !baseFile.exists()) {
            return list;
        }
        File[] files = baseFile.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                if(isAddDirectory){
                    list.add(file.getAbsolutePath());
                }
                list.addAll(getAllFilePath(file.getAbsolutePath(),isAddDirectory));
            } else {
                list.add(file.getAbsolutePath());
            }
        }
        return list;
    }
	
    /**
     * 判断文件的更新时间和记录中的更新时间是否相等，若相等返回-1，否则返回之前文件的版本号，若不存在文件返回-2
     * 
    * @author zhangml
    * @param filName
    * @param updateTime
    * @return
    * @date 2019年8月12日 下午12:16:56
     */
    public static Integer updateTimeIsEqual(String filName,Long updateTime){
    	
    	//获取文件FileRecord中的内容
		File file = new File(filePath);
		if (!file.exists()) {
			System.out.println("文件不存在");
			try {
				file.createNewFile();
			} catch (IOException e) {
				System.out.println("文件FileRecord.txt创建失败");
			}
			return -2;
		}
		String fileString = readToString(filePath);
		String[] recordStrings=fileString.split(System.lineSeparator());
		for(int i=0;i<recordStrings.length;i++) {
			String[] str=recordStrings[i].split("#");
			if(str[0].equals(filName)) {
				if(updateTime>Long.valueOf(str[1])) { //更新时间不同,且时间最新,返回版本号
					return Integer.valueOf(str[2]);
				}else {  //更新时间相同,返回-1
					return -1;
				}
			}
		}
		return -2;
	}
    
    /**
     * 判断文件记录中是否有该文件的记录，strs：有，null：没有
    * @author zhangml
    * @param clientFilePath
    * @return
    * @date 2019年8月16日 上午10:34:32
     */
    public static String[] isExsitRecord(String fileName) {
		//查找日志文件
		String[] recordStrings=FileUtil.readToString(filePath).split(System.lineSeparator());
		for(String singleRecord:recordStrings) {
			String[] strs=singleRecord.split("#");
			if(strs[0].equals(fileName)) {   //记录中存在该文件
				return strs;
			}
		}
    	return null;
    }
    
    
	/**
	 * 将文件记录写入FileRecord,(追加或者覆盖写入)
	* @author zhangml
	* @param filName
	* @param updateTime
	* @param VNum
	* @return 返回
	* @date 2019年8月12日 上午10:46:29
	 */
	public static Boolean writeToFileRecord(String fileName,Long submitTime,Integer VNum,String clientHost,String md5String,Boolean flag) {
		try {
			FileWriter fw=null;
			String content=fileName+"#"+submitTime+"#"+VNum+"#"+clientHost+"#"+md5String;
			if(flag==true){ //追加写入
		        fw = new FileWriter(filePath,flag);    	
		        content+=System.lineSeparator();
		    }else {       //覆盖写入,先将文件内容读出，修改，再写入
		        String fileString = readToString(filePath);
		        fw = new FileWriter(filePath,flag);
				String[] recordStrings=fileString.split(System.lineSeparator());
				for(int i=0;i<recordStrings.length;i++) {
					String[] str=recordStrings[i].split("#");
					if(str[0].equals(fileName)) {
						recordStrings[i] =content ;
						break;
					}
				}
				content=stringArrayToString(recordStrings);
			}
			fw.write(content);
		    fw.close();  
		} catch (IOException e) {
			System.out.println("文件写入失败！" + e);
			return false;
		}
		return true;
		
	}
	
	/**
	 * 读取文件内容以字符串形式返回
	* @author zhangml
	* @param fileName
	* @date 2019年8月12日 上午11:00:18
	 */
	public static String readToString(String filePath) {  
        String encoding = "UTF-8";  
        File file = new File(filePath);  
        Long filelength = file.length();  
        byte[] filecontent = new byte[filelength.intValue()];  
        try {  
            FileInputStream in = new FileInputStream(file);  
            in.read(filecontent);  
            in.close();  
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        try {  
            return new String(filecontent, encoding);  
        } catch (UnsupportedEncodingException e) {  
            System.err.println("The OS does not support " + encoding);  
            e.printStackTrace();  
            return null;  
        }  
    }
    
    /**
     * 将字符串数组转换成字符串
    * @author zhangml
    * @param strs
    * @return
    * @date 2019年8月12日 下午12:05:11
     */
    public static String stringArrayToString(String[] strs) {
    	String result="";
    	for(int i=0;i<strs.length;i++) {
    		result+=strs[i];
    		result+=System.lineSeparator();
    	}
		return result;
	}

}
