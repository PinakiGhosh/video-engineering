package com.ott.db;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class InMemoryDB {
	private Map<String,String> db;
	
	private static InMemoryDB _instance = null;
	private static final Object _lock = new Object();
	
	public static synchronized InMemoryDB getInstance() {
		if (null == _instance) {
			synchronized (_lock) {
				_instance = new InMemoryDB();
			}
		}
		return _instance;
	}
	
	public InMemoryDB() {
		/*ServerSocket server = null;
		try {
			server = new ServerSocket(8911 , 10);
            new TinyServer(server).start();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		db = new ConcurrentHashMap<String,String>();
	}
	
	
	public String getKey(String key) {
		return this.db.get(key);
	}
	
	public void setKey(String key, String value) {
		System.out.println("Key : " + key + " / value : " +value);
		this.db.put(key, value);
	}
}
