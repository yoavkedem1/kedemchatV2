package kchat.client;

import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.UnsupportedLookAndFeelException;
//manages all client ui
public class ClientUI {
	private UIManager m_manager;
	private Frame m_frame;
	private Client m_client;
	private int m_id;
	private int m_width, m_height;

	public ClientUI(Client client, int id, int width, int height) {
		m_client = client;
		m_manager = UIManager.getDefault();
		m_width = width;
		m_height = height;
		m_id = id;
		m_frame = new Frame();

		
		
		new javax.swing.Timer(200, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateManager();
			}
		}).start();
	}
	
	private void updateManager() {
		if (m_manager != null) {
			m_manager.update(m_frame);
			m_frame.revalidate();
		}
	}

	public void open() {
		m_frame.setVisible(true);

	}

	public void hide() {
		m_frame.setVisible(false);
	}

	public void close() {
		m_frame.close();
		m_frame.dispose();
	}

	public void setManager(UIManager manager) {
		if (m_manager != null) {
			m_manager.onClose(m_frame);
		}
		m_manager = manager;
		if (m_frame != null) {
			m_manager.onStart(m_frame);
		}
	}
	

	public UIManager getManager() {
		return m_manager;
	}

	@SuppressWarnings("all")
	private class Frame extends JFrame {
		public Frame() {
			super("Kedem Chat");
			try {
				javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
					| UnsupportedLookAndFeelException e) {
				e.printStackTrace();
			}
			getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
			getContentPane().setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
			getContentPane().setFont(new Font("Verdana", Font.PLAIN, 14));
			
			setSize(new Dimension(m_width,m_height));
			
			ComponentAdapter adapter = new ComponentAdapter() {
				public void componentResized(ComponentEvent c) {
					if (m_manager != null) {
						m_manager.update(Frame.this);
					}
				}
				
				public void componentMoved(ComponentEvent c) {
					if (m_manager != null) {
						m_manager.update(Frame.this);
					}
				}
			};
			this.addComponentListener(adapter);
			
			if (m_id == 0)
				this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
			
			if (m_id == 1) {
				JFrame that = m_client.getUI(0).m_frame;
				setLocation(that.getX() - 286 + getWidth()/2, that.getY());
			} else if (m_id == 0) {
				this.setLocationRelativeTo(null);
			}
		}

		public void setSize(int x, int y) {
			super.setSize(x, y);
			if (m_manager != null) {
				m_manager.update(Frame.this);
			}
		}

		public void setSize(Dimension dim) {
			super.setSize(dim);
			if (m_manager != null) {
				m_manager.update(Frame.this);
			}
		}

		public void setPreferredSize(Dimension dim) {
			super.setPreferredSize(dim);
			if (m_manager != null) {
				m_manager.update(Frame.this);
			}

		}

		private void close() {
		}
	}

	public JFrame getFrame() {
		return m_frame;
	}
}
