package com.ott.transcode;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.ott.db.InMemoryDB;
import com.ott.main.Starter;
import com.ott.utils.Keys;
import com.ott.utils.TTYPE;

public class Transcoder {
	
	TTYPE t;
	
	public Transcoder ( ) {
		this.t = TTYPE.valueOf(InMemoryDB.getInstance().getKey(Keys.ttype));
	}
	// http://forum.videohelp.com/threads/277807-Useful-FFmpeg-Syntax-Examples
	public void run() {
		String fileName = InMemoryDB.getInstance().getKey(Keys.new_mezz_file);
		String outputDir = InMemoryDB.getInstance().getKey(Keys.process_dir);
		String ffMpegBinary = InMemoryDB.getInstance().getKey(Keys.ffMpeg_binary);
		ProcessBuilder pb = null;
		Process p;
		String line = null;
		try {
			switch (t) {
			case HLS: 	/*
						./ffmpeg -v 9 -loglevel 99 -re -i sourcefile.avi -an \
						-c:v libx264 -b:v 128k -vpre ipod320 \
						-flags -global_header -map 0 -f segment -segment_time 4 \
						-segment_list test.m3u8 -segment_format mpegts stream%05d.ts
			 			*/
						//(ffMpegBinary);
						//(" -i "+fileName);
						//(" -ab 64 ");
						//(" -acodec mp3 ");
						//-an diable audio
						//(" -ar 44100 ");
						//(" -aspect 16:9 ");
						//(" -b 200 ");
						//-deinterlace
						//(" -f mpegts "); //h264 avi mov mp4 vob wav 
						//-vcodec copy h264 mpeg2video xvid 
						//(" -y "); // over write output files
						//(" -hls_list_size 0");
						//(outputDir+File.separator+"playlist.m3u8");
						//System.out.println(ffMpegOptionsHLS.toString());
						//pb = new ProcessBuilder(ffMpegOptionsHLS);
						pb = new ProcessBuilder(ffMpegBinary,"-i",fileName,"-hls_list_size","0",outputDir+File.separator+"playlist.m3u8");
						pb.directory(new File(InMemoryDB.getInstance().getKey(Keys.process_dir)));
				break;
			case SS:	pb = new ProcessBuilder("");
				break;
			case DASH:	pb = new ProcessBuilder("");
				break;
			default:
				break;
			}
			pb.redirectErrorStream(true);
			p = pb.start();
			InputStream in = p.getInputStream();
			InputStreamReader inr = new InputStreamReader(in);
			BufferedReader reader = new BufferedReader(inr);
			while ((line = reader.readLine ()) != null) {
			    System.out.println ("Stdout: " + line);
			}
			int status = p.waitFor();
			if (status == 0) {
				System.out.println("Successful: " + status);
				testPlay();
			} else {
				System.out.println("Failure: " + status);
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private static void testPlay() {
		System.out.println("Testing the newly encoded video by playing it using ffplay");
		String outputDir = InMemoryDB.getInstance().getKey(Keys.process_dir);
		ProcessBuilder pb = new ProcessBuilder("cmd.exe","/c","start",InMemoryDB.getInstance().getKey(Keys.ffplay_binary),"-i", outputDir+File.separator+"playlist.m3u8");
		pb.directory(new File(InMemoryDB.getInstance().getKey(Keys.process_dir)));
		Process p = null;
		String line = null;
		try {
			p = pb.start();
			InputStream in = p.getInputStream();
			InputStreamReader inr = new InputStreamReader(in);
			BufferedReader reader = new BufferedReader(inr);
			while ((line = reader.readLine ()) != null) {
			    System.out.println ("Stdout: " + line);
			}
			int status = p.waitFor();
			if (status == 0) {
				System.out.println("Successful: " + status);
			} else {
				System.out.println("Failure: " + status);
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
