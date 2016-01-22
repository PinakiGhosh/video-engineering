package com.ott.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.ott.catalog.CatalogGenerator;
import com.ott.collection.FolderWatcher;
import com.ott.db.InMemoryDB;
import com.ott.drm.CoverWithDRM;
import com.ott.publish.Publisher;
import com.ott.transcode.Transcoder;
import com.ott.utils.Keys;
import com.ott.validator.Validator;

public class Starter {

	public static void main(String[] args) {
		System.out.println("START");
		// prepare
		init();
		cleanup();
		Transcoder t = transcode();
		Validator v = validate();
		CoverWithDRM d = drm();
		Publisher p = publish();
		CatalogGenerator c = catalog();
		
		// start process thread
		collect(t,v,d,p,c);
		test();
		System.out.println("END");
	}

	public static void init () {
		System.out.println("Initializing the in memory DB");
		InMemoryDB db = InMemoryDB.getInstance();
		db.setKey(Keys.ffMpeg_binary, "D:\\video-engineering\\ffmpeg\\bin\\ffmpeg.exe");
		db.setKey(Keys.ffplay_binary, "D:\\video-engineering\\ffmpeg\\bin\\ffplay.exe");
		db.setKey(Keys.ffprobe_binary, "D:\\video-engineering\\ffmpeg\\bin\\ffprobe.exe");
		db.setKey(Keys.drop_folder, "D:\\video-engineering\\drop_folder");
		db.setKey(Keys.process_dir, "D:\\video-engineering\\process_dir");
		db.setKey(Keys.resource_folder, "D:\\video-engineering\\resources");
		db.setKey(Keys.ttype, "HLS");
	}

	public static void cleanup() {
		System.out.println("Cleaing the drop folder contents");
		File dropFolder = new File(InMemoryDB.getInstance().getKey(Keys.drop_folder));
		if (dropFolder.exists()) {
			for (File f : dropFolder.listFiles()){
				f.delete();
			}
		}
		System.out.println("Cleaing the process folder contents");
		File processFolder = new File(InMemoryDB.getInstance().getKey(Keys.process_dir));
		if (processFolder.exists()) {
			for (File f : processFolder.listFiles()){
				f.delete();
			}
		}
	}
	
	public static void test() {
		System.out.println("Initiating the file drop test");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Cleaing the drop folder contents");
		File dropFolder = new File(InMemoryDB.getInstance().getKey(Keys.drop_folder)+File.separator+"live.wmv");
		if (dropFolder.exists()) {
			for (File f : dropFolder.listFiles()){
				f.delete();
			}
		}
		System.out.println("Copying the test video from resource folder to drop folder");
		File resourceFolder = new File(InMemoryDB.getInstance().getKey(Keys.resource_folder)+File.separator+"live.wmv");
		if (resourceFolder.exists()) {
			try {
				copyFileUsingFileStreams(resourceFolder,dropFolder);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void copyFileUsingFileStreams(File source, File dest)
			throws IOException {
		InputStream input = null;
		OutputStream output = null;
		try {
			input = new FileInputStream(source);
			output = new FileOutputStream(dest);
			byte[] buf = new byte[1024];
			int bytesRead;
			while ((bytesRead = input.read(buf)) > 0) {
				output.write(buf, 0, bytesRead);
			}
		} finally {
			input.close();
			output.close();
		}
	}
	
	
	private static void collect(Transcoder t, Validator v, CoverWithDRM d, Publisher p, CatalogGenerator c) {
		FolderWatcher fw = new FolderWatcher(t,v,d,p,c);
		Thread singleThread = new Thread(fw);
		singleThread.start();
	}
	
	private static Validator validate() {
		return new Validator();
	}
	
	private static Transcoder transcode() {
		return new Transcoder();
	}
	
	private static CoverWithDRM drm() {
		return new CoverWithDRM();
	}
	
	private static Publisher publish() {
		return new Publisher();
	}
	
	private static CatalogGenerator catalog() {
		return new CatalogGenerator();
	}
}
