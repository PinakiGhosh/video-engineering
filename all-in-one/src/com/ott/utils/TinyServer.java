package com.ott.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import com.ott.db.InMemoryDB;

public class TinyServer extends Thread {

	private ServerSocket server;
	
	public TinyServer(ServerSocket server) {
		System.out.println("creating a tcp server");
		this.server = server;
	}

	public void run() {
		String line = null;
		Socket conn = null;
		try {
			System.out.println("Listening for connection on port " + server.getLocalPort());
			while (true) {
				conn = server.accept();
				// get socket writing and reading streams
				PrintWriter out = new PrintWriter(conn.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(
						conn.getInputStream()));
				// Now start reading input from client
				while ((line = in.readLine()) != null) {
					// reply with message, after doing logic
					System.out.println(line);
					System.out.println(InMemoryDB.getInstance().getKey(line));
					out.println(InMemoryDB.getInstance().getKey(line));
				}
				line = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			try {
				conn.close();
				server.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		}
	}
}
