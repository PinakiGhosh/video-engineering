package com.ott.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.ott.db.InMemoryDB;

public class ExceptionFreeProcess {

	public static int process(ProcessBuilder pb) {
		pb.directory(new File(InMemoryDB.getInstance().getKey(Keys.process_dir_clear)));
		pb.redirectErrorStream(true);
		Process p = null;
		String line = null;
		int status = 1;
		try {
			p = pb.start();
			InputStream in = p.getInputStream();
			InputStreamReader inr = new InputStreamReader(in);
			BufferedReader reader = new BufferedReader(inr);
			while ((line = reader.readLine ()) != null) {
			    System.out.println ("Stdout: " + line);
			}
			status = p.waitFor();
			if (status == 0) {
				System.out.println("Successful: " + status);
			} else {
				System.out.println("Failure: " + status);
			}
			
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return status;
	}
	
}
