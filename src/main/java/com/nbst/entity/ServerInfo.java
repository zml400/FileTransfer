package com.nbst.entity;

import lombok.Data;

/**
 * @author zhangml
 *
 */

@Data
public class ServerInfo {
	private String host;
	private Integer port;
	private String userName;
	private String userPwd;
}
