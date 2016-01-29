package com.ott.transcode;

import java.io.File;

import com.ott.db.InMemoryDB;
import com.ott.utils.ExceptionFreeProcess;
import com.ott.utils.Keys;

public class Transcoder {
	
	public void run() {
		String fileName = InMemoryDB.getInstance().getKey(Keys.new_mezz_file);
		String outputDir = InMemoryDB.getInstance().getKey(Keys.process_dir_clear);
		String ffMpegBinary = InMemoryDB.getInstance().getKey(Keys.ffMpeg_binary);
		String mp4boxBinary = InMemoryDB.getInstance().getKey(Keys.mp4box_binary);
		ProcessBuilder pb = null;
		// HLS
		/**
		 Single variant:
		 ffmpeg -i source.mp4 -map 0 -codec:v libx264 -codec:a libfaac -f ssegment -segment_list playlist.m3u8 -segment_list_flags +live -segment_time 10 out%03d.ts
		 
		 Multi variant
		 ffmpeg -y -i source.mp4 -threads 0 -f mpegts 
				-acodec libfaac -ab 64k -ar 44100 -vcodec libx264 -vprofile baseline 
			    -x264opts "fps=12:keyint=36:bitrate=200"  -s 416x234  bbb.mpegts/p1.ts 
			  	-acodec libfaac -ab 64k -ar 44100 -vcodec libx264 -vprofile baseline 
			    -x264opts "fps=12:keyint=36:bitrate=400"  -s 480x270  bbb.mpegts/p2.ts 
			  	-acodec libfaac -ab 64k -ar 44100 -vcodec libx264 -vprofile baseline 
			    -x264opts "fps=24:keyint=72:bitrate=600"  -s 640x360  bbb.mpegts/p3.ts 
			  	-acodec libfaac -ab 64k -ar 44100 -vcodec libx264 -vprofile baseline 
			    -x264opts "fps=24:keyint=72:bitrate=1200" -s 640x360  bbb.mpegts/p4.ts 
			  	-acodec libfaac -ab 64k -ar 44100 -vcodec libx264 -vprofile main     
			    -x264opts "fps=24:keyint=72:bitrate=1800" -s 960x540  bbb.mpegts/p5.ts 
			  	-acodec libfaac -ab 64k -ar 44100 -vcodec libx264 -vprofile main     
			    -x264opts "fps=24:keyint=72:bitrate=2500" -s 960x540  bbb.mpegts/p6.ts 
			  	-acodec libfaac -ab 64k -ar 44100 -vcodec libx264 -vprofile main     
			    -x264opts "fps=24:keyint=72:bitrate=4500" -s 1280x720 bbb.mpegts/p7.ts
		 
		 m3u8-segmenter --input ../bbb.mpegts/$p.ts --duration 6 --output-prefix $p.seg/$p --m3u8-file $p.m3u8 --url-prefix ""
		 
		 */
		pb = new ProcessBuilder(ffMpegBinary,"-i",fileName,"-hls_list_size","0",outputDir+File.separator+Keys.playlist+Keys.HLS); // "playlist.m3u8"
		if (ExceptionFreeProcess.process(pb) == 0)
				testffPlay(outputDir+File.separator+"playlist.m3u8");
			
		// SS
		/**
		 	$ ffmpeg -y -i bunny.mov \
			-an -vcodec libx264 -g 100 -keyint_min 100 \
			-x264opts pic-struct:no-scenecut -movflags frag_keyframe \
			-b 200k -s 320x240 \
			bunny_200k.ismv
			
			$ ffmpeg -y -i bunny.mov \
			-acodec libfaac -ac 2 -ab 64k \
			-vcodec libx264 -g 100 -keyint_min 100 \
			-x264opts pic-struct:no-scenecut -movflags frag_keyframe \
			-b 400k -s 640x480 \
			bunny_400k.ismv
		 
		 Audio demux and encoding:
		 ffmpeg -analyzeduration 2147480000 -i bunny.mov -ar 48000 -ac 2 -y audio.wav 
			neroAacEnc -cbr 128000 -lc -if audio.wav -of audio.mp4
			 
		 Video demux and encoding:
		 ffmpeg -i bunny.mov -r 25 -s 854x480 -aspect 16:9 -f yuv4mpegpipe -pix_fmt yuv420p -vsync 1 -g 100 -keyint_min 100 -movflags frag_keyframe -y video.y4m
			x264 --pass 1 --fps 25 --bitrate 2000 --no-scenecut --stats ./x264_2pass.log -o /dev/null video.y4m
			x264 --pass 2 --fps 25 --bitrate 2000 --no-scenecut --stats ./x264_2pass.log -o video.h264 video.y4m
			ffmpeg -i video.h264 -i audio.mp4 -vcodec copy -acodec copy -y bunny_2000.ismv
			
			ffmpeg -i bunny.mov -r 25 -s 480x272 -aspect 16:9 -f yuv4mpegpipe -pix_fmt yuv420p -vsync 1 -g 100 -keyint_min 100 -movflags frag_keyframe -y video.y4m
			x264 --pass 1 --fps 25 --bitrate 894 --no-scenecut --stats ./x264_2pass.log -o /dev/null video.y4m
			x264 --pass 2 --fps 25 --bitrate 894 --no-scenecut --stats ./x264_2pass.log -o video.h264 video.y4m
			ffmpeg -i video.h264 -vcodec copy -y bunny_894.ismv
			
			ffmpeg -i bunny.mov -r 25 -s 288x160 -aspect 16:9 -f yuv4mpegpipe -pix_fmt yuv420p -vsync 1 -g 100 -keyint_min 100 -movflags frag_keyframe -y video.y4m
			x264 --pass 1 --fps 25 --bitrate 400 --no-scenecut --stats ./x264_2pass.log -o /dev/null video.y4m
			x264 --pass 2 --fps 25 --bitrate 400 --no-scenecut --stats ./x264_2pass.log -o video.h264 video.y4m
			ffmpeg -i video.h264 -vcodec copy -y bunny_400.ismv
		 
		 manifest:
		 ismindex -n bunny bunny_400.ismv bunny_894.ismv bunny_2000.ismv
		 or
		 mp4split -o bunny.ism bunny.ismv
		 
		 Splitting as static files
		 ismindex -split bunny.ismv
		 
		 */
		pb = new ProcessBuilder(ffMpegBinary,"-i",fileName,"-movflags","frag_keyframe",outputDir+File.separator+Keys.playlist+Keys.SS_binary); // "playlist.ismv"
		if (ExceptionFreeProcess.process(pb) == 0)
			testffPlay(outputDir+File.separator+"playlist.ismv");
		// DASH
		/**
		 Video Streams:

			VP9_DASH_PARAMS="-tile-columns 4 -frame-parallel 1"
			
			ffmpeg -i input_video.y4m -c:v libvpx-vp9 -s 160x90 -b:v 250k -keyint_min 150 -g 150 ${VP9_DASH_PARAMS} -an -f webm -dash 1 video_160x90_250k.webm
			
			ffmpeg -i input_video.y4m -c:v libvpx-vp9 -s 320x180 -b:v 500k -keyint_min 150 -g 150 ${VP9_DASH_PARAMS} -an -f webm -dash 1 video_320x180_500k.webm
			
			ffmpeg -i input_video.y4m -c:v libvpx-vp9 -s 640x360 -b:v 750k -keyint_min 150 -g 150 ${VP9_DASH_PARAMS} -an -f webm -dash 1 video_640x360_750k.webm
			
			ffmpeg -i input_video.y4m -c:v libvpx-vp9 -s 640x360 -b:v 1000k -keyint_min 150 -g 150 ${VP9_DASH_PARAMS} -an -f webm -dash 1 video_640x360_1000k.webm
			
			ffmpeg -i input_video.y4m -c:v libvpx-vp9 -s 1280x720 -b:v 1500k -keyint_min 150 -g 150 ${VP9_DASH_PARAMS} -an -f webm -dash 1 video_1280x720_500k.webm
		 Audio Stream:

			ffmpeg -i input_audio.wav -c:a libvorbis -b:a 128k -vn -f webm -dash 1 audio_128k.webm
		 
		 Create the WebM DASH Manifest	
		 
		 	ffmpeg \
			 -f webm_dash_manifest -i video_160x90_250k.webm \
			 -f webm_dash_manifest -i video_320x180_500k.webm \
			 -f webm_dash_manifest -i video_640x360_750k.webm \
			 -f webm_dash_manifest -i video_640x360_1000k.webm \
			 -f webm_dash_manifest -i video_1280x720_500k.webm \
			 -f webm_dash_manifest -i audio_128k.webm \
			 -c copy -map 0 -map 1 -map 2 -map 3 -map 4 -map 5 \
			 -f webm_dash_manifest \
			 -adaptation_sets "id=0,streams=0,1,2,3,4 id=1,streams=5" \
			 manifest.mpd
			 
			 mp4box -dash 3000 -rap -profile dashavc264:onDemand out.mp4#audio out.mp4#video
		 */
		//pb = new ProcessBuilder(ffMpegBinary,"-i",fileName,"-c:v","libvpx","-keyint_min","150","-f","webm","-dash","1",outputDir+File.separator+"video.webm");
		pb = new ProcessBuilder(ffMpegBinary,"-i",fileName,"-c:v","libx264",outputDir+File.separator+Keys.playlist+Keys.DASH_binary); //"playlist.mp4"
		if (ExceptionFreeProcess.process(pb) == 0) {
			
			pb = new ProcessBuilder(mp4boxBinary,"-dash","3000","-rap","-profile","dashavc264:onDemand",outputDir+File.separator+Keys.playlist+Keys.DASH_binary); //"playlist.mp4"
			if (ExceptionFreeProcess.process(pb) == 0) {
				testOsmo(outputDir+File.separator+Keys.playlist+"_dash"+Keys.DASH); //"playlist_dash.mpd"
			}
		}
	}
	
	private static void testffPlay(String inputFile) {
		System.out.println("Testing the newly encoded video by playing it using ffplay");
		ProcessBuilder pb = new ProcessBuilder("cmd.exe","/c","start",InMemoryDB.getInstance().getKey(Keys.ffplay_binary),"-i", inputFile);
		ExceptionFreeProcess.process(pb);
	}
	
	private static void testOsmo(String inputFile) {
		System.out.println("Testing the newly encoded video by playing it using ffplay");
		ProcessBuilder pb = new ProcessBuilder("cmd.exe","/c","start",InMemoryDB.getInstance().getKey(Keys.osmo_binary), inputFile);
		ExceptionFreeProcess.process(pb);
	}
}
