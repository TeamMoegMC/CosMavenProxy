/**
 * KWebserver
 * Copyright (C) 2021  khjxiaogu
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.tssap.twrcosmaven;

import java.io.File;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;


import com.khjxiaogu.webserver.Utils;
import com.khjxiaogu.webserver.loging.SimpleLogger;
import com.khjxiaogu.webserver.web.CallBack;
import com.khjxiaogu.webserver.web.lowlayer.Request;
import com.khjxiaogu.webserver.web.lowlayer.Response;

// TODO: Auto-generated Javadoc
/**
 * Class FilePageService. 文件系统页面服务，根据文件路径发送网页 /将自动导向到当前目录下/index.html
 *
 * @author khjxiaogu file: FilePageService.java time: 2020年5月8日
 */
public class FileSystemService implements CallBack {

	/**
	 * The dest.<br>
	 * 成员 dest.
	 */
	CosFS fs=new CosFS("maven-1301510336","ap-guangzhou");
	/**
	 * The format.<br>
	 * 成员 format.
	 */
	private final SimpleDateFormat format = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss 'GMT'");
	private String data="Basic "+Base64.getEncoder().encode("username:password".getBytes());
	/**
	 * Instantiates a new FilePageService with a root directory.<br>
	 * 新建一个FilePageService类，设置根目录。<br>
	 */
	public FileSystemService() {
		format.setTimeZone(TimeZone.getTimeZone("GMT"));
		format.setDateFormatSymbols(DateFormatSymbols.getInstance(Locale.ENGLISH));
	}

	/**
	 * The logger.<br>
	 * 成员 logger.
	 */
	SimpleLogger logger = new SimpleLogger("页面");

	@Override
	public void call(Request req, Response res) {
		

		if(req.getMethod().equalsIgnoreCase("GET")) {
			res.setHeader("Location",fs.getPath2(req.getCurrentPath()));
			res.write(302);
		}else if(req.getMethod().equals("PUT")) {
			try {
				fs.upload(req.getCurrentPath(),Utils.readAll(req.getBody()));
				res.write(200);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				res.write(500,""+e.getMessage());
			}
			
		}
	}


	/**
	 * Constant INSECURE_URI.<br>
	 * 常量 INSECURE_URI.
	 */
	private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");

	/**
	 * Sanitize uri.<br>
	 * 清理并安全化URI，转为操作系统相关URI
	 *
	 * @param uri the uri<br>
	 * @return return sanitized uri <br>
	 *         返回 string
	 */
	private static String sanitizeUri(String uri) {
		/*try {
			uri=Paths.get(new URI(uri)).normalize().toUri().toString();
		} catch (URISyntaxException e) {
		}*/
		if (uri == null || uri.isEmpty() || uri.charAt(0) != '/')
			return "/";

		// Convert file separators.
		uri = uri.replace('/', File.separatorChar);

		// Simplistic dumb security check.
		// You will have to do something serious in the production environment.
		if (uri.contains(File.separator + '.') || uri.contains('.' + File.separator) || uri.charAt(0) == '.'
		        || uri.charAt(uri.length() - 1) == '.' || FileSystemService.INSECURE_URI.matcher(uri).matches())
			return "/";

		// Convert to absolute path.
		return uri;
	}
}
