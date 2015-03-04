package org.provebit.daemon;

import java.io.File;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.provebit.merkle.Merkle;

public class DirectoryMonitor implements FileAlterationListener {
	public enum MonitorEvent{FCREATE, FDELETE, FCHANGE, DCREATE, DDELETE, DCHANGE};
	private Merkle tree;
	private int changes;
	protected Log log;
	
	/**
	 * Constructor, configures the tree that will represent the argument file/directory
	 * @param dir - directory to watch
	 * @param recursive - whether or not to enable recursive directory changes
	 */
	public DirectoryMonitor(Merkle mTree) {
		tree = mTree;
		changes = 0;
		log = new Log();
	}
	
	@Override
	public void onStart(FileAlterationObserver observer) {
		if (!tree.exists()) {
			tree.makeTree();
		}
	}

	@Override
	public void onDirectoryCreate(File directory) {
		updateTree(MonitorEvent.DCREATE, directory);
	}

	@Override
	public void onDirectoryChange(File directory) {
		updateTree(MonitorEvent.DCHANGE, directory);
	}

	@Override
	public void onDirectoryDelete(File directory) {
		updateTree(MonitorEvent.DDELETE, directory);
	}

	@Override
	public void onFileCreate(File file) {
		updateTree(MonitorEvent.FCREATE, file);
	}

	@Override
	public void onFileChange(File file) {
		updateTree(MonitorEvent.FCHANGE, file);
	}

	@Override
	public void onFileDelete(File file) {
		updateTree(MonitorEvent.FDELETE, file);
	}

	@Override
	public void onStop(FileAlterationObserver observer) {
		// Do nothing
	}
	
	/**
     * Get the current merkle tree
     * @return Currently constructed merkle tree
     */
    public Merkle getTree() {
        return tree;
    }
    
    /**
     * Get number of changes detected since daemon launched
     * @return number of changes since launch
     */
    public int getChanges() {
        return changes;
    }

    /**
     * Update wrapper that makes sure the detected change is relevant before
     * spending the time to reconstruct the merkle tree
     */
	private void updateTree(MonitorEvent event, File file) {
		if (tree.isRecursive() || file.getParent().compareTo(tree.getDir().getAbsolutePath()) == 0) {
			logEvent(event, file);
			reconstructTree();
		}
	}
	
	/**
	 * Helper function that adds the event to the local log
	 * @param event - Event that occurred
	 * @param file - File related to event
	 */
	private void logEvent(MonitorEvent event, File file) {
		log.addEntry(event.toString() + " : " + file.getAbsolutePath());
	}
	
	/**
	 * Reconstructs the merkle tree and increments the change counter
	 */
	private void reconstructTree() {
		changes++;
		System.out.println("Old root: " + Hex.encodeHexString(tree.getRootHash()));
		tree.makeTree();
		System.out.println("New root: " + Hex.encodeHexString(tree.getRootHash()));
	}
}