package com.tssap.twrcosmaven;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.khjxiaogu.webserver.loging.SimpleLogger;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.transfer.Download;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.transfer.TransferManagerConfiguration;
import com.qcloud.cos.transfer.Upload;

public class CosFS{

	private COSClient cosClient;
	private TransferManager transferManager;
	final String bucketName;
	final String regionName;
	SimpleLogger logger=new SimpleLogger("CloudFS");
	// .cssg-methods-pragma

	public CosFS(String bucket, String region) {
		super();
		bucketName = bucket;
		this.regionName = region;
		initClient();
	}

	private void initClient() {
		String secretId = "COS_SECRETID";
		String secretKey = "COS_SECRETKEY";
		COSCredentials cred = new BasicCOSCredentials(System.getProperty(secretId), System.getProperty(secretKey));
		Region region = new Region(regionName);
		ClientConfig clientConfig = new ClientConfig(region);
		this.cosClient = new COSClient(cred, clientConfig);
		ExecutorService threadPool = Executors.newFixedThreadPool(4);
		transferManager = new TransferManager(cosClient, threadPool);
		TransferManagerConfiguration transferManagerConfiguration = new TransferManagerConfiguration();
		transferManagerConfiguration.setMultipartUploadThreshold(10 * 1024 * 1024);
		transferManagerConfiguration.setMinimumUploadPartSize(10 * 1024 * 1024);
		transferManager.setConfiguration(transferManagerConfiguration);
	}
	public String upload(String path, byte[] ba) {
		ObjectMetadata om = new ObjectMetadata();
		om.setContentLength(ba.length);
		
		PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, path, new ByteArrayInputStream(ba), om);
		putObjectRequest.setTrafficLimit(64 * 1024 * 1024);
		logger.info("uploading "+path);
		Upload upload = transferManager.upload(putObjectRequest);
		//upload.addProgressListener(null);
		try {
			upload.waitForCompletion();
		} catch (CosClientException | InterruptedException e) {
			logger.printFullStackTrace(e);
		}
		return getPath(path);
	}
	public String upload(String path, InputStream ba,int len) {
		ObjectMetadata om = new ObjectMetadata();
		om.setContentLength(len);
		
		PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, path,ba, om);
		putObjectRequest.setTrafficLimit(64 * 1024 * 1024);
		logger.info("uploading "+path);
		Upload upload = transferManager.upload(putObjectRequest);
		try {
			upload.waitForCompletion();
		} catch (CosClientException | InterruptedException e) {
			logger.printFullStackTrace(e);
		}
		return getPath(path);
	}

	public String upload(String path, File f) {
		PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, path, f);
		putObjectRequest.setTrafficLimit(64 * 1024 * 1024);
		Upload upload = transferManager.upload(putObjectRequest);
		try {
			upload.waitForCompletion();
		} catch (CosClientException | InterruptedException e) {
			logger.printFullStackTrace(e);
		}
		return getPath(path);
	}

	public String getPath(String path) {
		return "https://" + bucketName + ".cos." + regionName + ".myqcloud.com/" + path;
	}
	public String getPath2(String path) {
		return "https://" + bucketName + ".cos." + regionName + ".myqcloud.com" + path;
	}
	public void download(String path, File f) {
		GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, path);
		try {
			Download download = transferManager.download(getObjectRequest, f);
			download.waitForCompletion();
		} catch (Exception e) {
			logger.printFullStackTrace(e);
		}
	}

	public void delete(String path) {
		cosClient.deleteObject(bucketName, path);
	}

	public void close() {
		transferManager.shutdownNow(true);
	}

}
