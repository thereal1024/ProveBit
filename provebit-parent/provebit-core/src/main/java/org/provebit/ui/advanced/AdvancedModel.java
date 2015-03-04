package org.provebit.ui.advanced;

import java.util.Observable;

public class AdvancedModel extends Observable {
	
	public AdvancedModel() {
		
	}
	
	private void notifyChange() {
		setChanged();
		notifyObservers();
	}
}