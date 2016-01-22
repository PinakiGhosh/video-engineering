package com.ott.collection;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import com.ott.catalog.CatalogGenerator;
import com.ott.db.InMemoryDB;
import com.ott.drm.CoverWithDRM;
import com.ott.publish.Publisher;
import com.ott.transcode.Transcoder;
import com.ott.utils.Keys;
import com.ott.validator.Validator;

public class FolderWatcher implements Runnable{

	private WatchService watcher;
	private Path dir;
	
	private Transcoder t;
	private Validator v;
	private CoverWithDRM d;
	private Publisher p;
	private CatalogGenerator c;
	
	public FolderWatcher (Transcoder t, Validator v, CoverWithDRM d, Publisher p, CatalogGenerator c){
		this.t = t;
		this.v = v;
		this.d = d;
		this.p = p;
		this.c = c;
		
		try {
			watcher = FileSystems.getDefault().newWatchService();
			dir = Paths.get(InMemoryDB.getInstance().getKey(Keys.drop_folder));
			dir.register(watcher,  ENTRY_CREATE,  ENTRY_DELETE,    ENTRY_MODIFY);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		
		while(true) {
		    // wait for key to be signaled
		    WatchKey key;
		    try {
		        key = watcher.take();
		    } catch (InterruptedException x) {
		        x.printStackTrace();
		    	continue;
		    }
		    for (WatchEvent<?> event: key.pollEvents()) {
		        WatchEvent.Kind<?> kind = event.kind();

		        if (kind == OVERFLOW) {
		            continue;
		        }
		        // new file
		        if (kind == ENTRY_CREATE) {
		        	// The filename is the context of the event.
			        WatchEvent<Path> ev = (WatchEvent<Path>)event;
			        Path filename = ev.context();
			        // Resolve the filename against the directory.
		            // If the filename is "test" and the directory is "foo",
		            // the resolved name is "test/foo".
		            Path child = dir.resolve(filename);
		            System.out.println("New file detected: " +child.toFile().getAbsolutePath());
		            InMemoryDB.getInstance().setKey(Keys.new_mezz_file, child.toFile().getAbsolutePath());
		            v.run();
		            t.run();
		            d.run();
		            p.run();
		            c.run();
		        }
		    }
		    // Reset the key -- this step is critical if you want to
		    // receive further watch events.  If the key is no longer valid,
		    // the directory is inaccessible so exit the loop.
		    boolean valid = key.reset();
		    if (!valid) {
		        break;
		    }
		}
	}
	
}
