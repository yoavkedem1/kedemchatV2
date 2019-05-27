package kchat.client;

import javax.swing.JFrame;

public interface UIManager {
	public static UIManager getDefault() {
		return null;
	}
	
	/**
	 * Called upon setting the UIManager as the manager for a given frame
	 * @param frame the parent frame
	 */
	void onStart(JFrame frame);

	/**
	 * Called upon removing the UIManager from being
	 *  the manager for a given frame
	 * @param frame the parent frame
	 */
	void onClose(JFrame frame);
	
	/**
	 * Called once every second
	 * @param frame the parent frame
	 */
	void update(JFrame frame);
}
