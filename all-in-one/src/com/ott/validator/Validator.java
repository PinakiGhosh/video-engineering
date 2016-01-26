package com.ott.validator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.ott.db.InMemoryDB;
import com.ott.utils.Keys;

public class Validator{

	public void run() {
		System.out.println("Validating video file with ffprobe");
		String file = InMemoryDB.getInstance().getKey(Keys.new_mezz_file);
		/**
		 * verify keyframes after each encoding
   			ffprobe -show_frames -print_format compact out.mp4 | less
		 */
		ProcessBuilder pb = new ProcessBuilder(InMemoryDB.getInstance().getKey(Keys.ffprobe_binary),"-v","error","-show_format","-show_streams", file);
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
