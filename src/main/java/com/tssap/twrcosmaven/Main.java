package com.tssap.twrcosmaven;

import com.khjxiaogu.webserver.builder.BasicWebServerBuilder;

public class Main {

	public static void main(String[] args) throws NumberFormatException, InterruptedException {
		BasicWebServerBuilder.build().createHostRoot()
		.createContext("", new FileSystemService())
		.complete().compile().serverHttp("127.0.0.1", Integer.parseInt(System.getProperty("port"))).info("service started").await();
		
	}

}
