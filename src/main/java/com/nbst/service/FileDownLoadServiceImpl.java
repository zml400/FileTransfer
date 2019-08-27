package com.nbst.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import com.nbst.entity.Result;

/**
* @author zml
* @date 2019年8月22日 下午1:26:10
*/
public class FileDownLoadServiceImpl implements IFileDownLoadService{

	
	/**
	 * 文件下载
	* @author zhangml
	* @param remoteFilePath
	* @param response
	* @return
	* @date 2019年8月22日 下午2:04:49
	 */
	@Override
	public Result fileDownLoad(String remoteFilePath, HttpServletResponse response) {
		// TODO Auto-generated method stub
		//1、将文件服务器中的文件下载到项目目录downLoad中
		//2、将downLoad中的文件下载到用户电脑
		
		
		return null;
	}

	/**
	 * 单文件下载
	* @author zhangml
	* @param filePath
	* @param filename
	* @param response
	* @return
	* @date 2019年8月22日 下午2:00:51
	 */
	@Override
	public Result singleFileDownLoad(String filePath,String filename,HttpServletResponse response) {
	    File file = new File(filePath + "/" + filename);
	    if(file.exists()){ //判断文件父目录是否存在
	    	try {
			filename = java.net.URLEncoder.encode(filename, "UTF-8");
			filename = new String(filename.getBytes(), "iso-8859-1");
	        response.setContentType("application/force-download");
	        response.setHeader("Content-Disposition", "attachment;fileName=" + filename);
	        
	        byte[] buffer = new byte[1024];
	        FileInputStream fis = null; //文件输入流
	        BufferedInputStream bis = null;
	        
	        OutputStream os = null; //输出流
	       
	            os = response.getOutputStream();
	            fis = new FileInputStream(file); 
	            bis = new BufferedInputStream(fis);
	            int i = bis.read(buffer);
	            while(i != -1){
	                os.write(buffer);
	                i = bis.read(buffer);
	            }
	            bis.close();
	            fis.close();
	            
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        System.out.println("----------file download" + filename);
	    }
	    return Result.success("success");
	}

	/**
	 * 多文件下载
	* @author zhangml
	* @param files
	* @param response
	* @return
	* @date 2019年8月22日 下午2:01:04
	 */
	@Override
	public Result mutilFileDownLoad(List<File> files, HttpServletResponse response) {
		try {
			// List<File> 作为参数传进来，就是把多个文件的路径放到一个list里面
			// 创建一个临时压缩文件
			String zipFilename =System.getProperty("user.dir") + "/document/downLoad/tempFile.zip";
			File file = new File(zipFilename);
			file.createNewFile();
			if (!file.exists()) {
				file.createNewFile();
			}
			response.reset();
			// response.getWriter()
			// 创建文件输出流
			FileOutputStream fous = new FileOutputStream(file);
			ZipOutputStream zipOut = new ZipOutputStream(fous);
			zipFile(files, zipOut);
			zipOut.close();
			fous.close();
			downloadZip(file, response);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Result.success("success");
	}
	
	/**
	 * 把接收的全部文件打成压缩包
	* @author zhangml
	* @param files
	* @param outputStream
	* @date 2019年8月22日 下午2:04:32
	 */
	public static void zipFile(List<File> files, ZipOutputStream outputStream) {
		int size = files.size();
		for (int i = 0; i < size; i++) {
			File file = (File) files.get(i);
			zipFile(file, outputStream);
		}
	}

	/**
	 * 根据输入的文件与输出流对文件进行打包
	* @author zhangml
	* @param inputFile
	* @param ouputStream
	* @date 2019年8月22日 下午2:04:16
	 */
	public static void zipFile(File inputFile, ZipOutputStream ouputStream) {
		try {
			if (inputFile.exists()) {
				if (inputFile.isFile()) {
					FileInputStream IN = new FileInputStream(inputFile);
					BufferedInputStream bins = new BufferedInputStream(IN, 512);
					ZipEntry entry = new ZipEntry(inputFile.getName());
					ouputStream.putNextEntry(entry);
					// 向压缩文件中输出数据
					int nNumber;
					byte[] buffer = new byte[512];
					while ((nNumber = bins.read(buffer)) != -1) {
						ouputStream.write(buffer, 0, nNumber);
					}
					// 关闭创建的流对象
					bins.close();
					IN.close();
				} else {
					try {
						File[] files = inputFile.listFiles();
						for (int i = 0; i < files.length; i++) {
							zipFile(files[i], ouputStream);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 *下载压缩包 
	* @author zhangml
	* @param file
	* @param response
	* @return
	* @date 2019年8月22日 下午2:06:05
	 */
	public static String downloadZip(File file, HttpServletResponse response) {
		if (file.exists() == false) {
			System.out.println("压缩文件：" + file + "不存在.");
		} else {
			try {
				// 以流的形式下载文件。
				InputStream fis = new BufferedInputStream(new FileInputStream(file.getPath()));
				byte[] buffer = new byte[fis.available()];
				fis.read(buffer);
				fis.close();
				// 清空response
				response.reset();

				OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
				response.setContentType("application/octet-stream");

				// 如果输出的是中文名的文件，在此处就要用URLEncoder.encode方法进行处理
				response.setHeader("Content-Disposition",
						"attachment;filename=" + new String(file.getName().getBytes("GB2312"), "ISO8859-1"));
				toClient.write(buffer);
				toClient.flush();
				toClient.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				try {
					File f = new File(file.getPath());
					f.delete();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return "success";
	}

}
