package com.root;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextArea;

// TODO: Auto-generated Javadoc
/**
 * The Class SwingProgressBar.
 */
public abstract class SwingProgressBar extends SwingWorker<Object, Object>
{

	/** The Constant interval. */
	final static int	interval	= 1000;

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 * @throws ClassNotFoundException
	 *             the class not found exception
	 * @throws InstantiationException
	 *             the instantiation exception
	 * @throws IllegalAccessException
	 *             the illegal access exception
	 * @throws UnsupportedLookAndFeelException
	 *             the unsupported look and feel exception
	 */
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, UnsupportedLookAndFeelException
	{
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		new SwingProgressBar(null, "Swing Progress Bar", "Initializing...", null) {

			@Override
			protected Object doInBackground() throws Exception
			{
				System.out.println("Doing Some Stuff!");
				for (int i = 0; i < 10; i++)
				{
					Thread.sleep(1000);
					setText("Counter = " + i);
				}
				System.out.println("Done stuff!");
				return null;
			}
		};

		System.out.println("Done!");
	}

	/** The frame. */
	JDialog			frame			= null;

	/** The i. */
	int				i;

	/** The pb. */
	JProgressBar	pb;

	/** The start time. */
	Calendar		startTime;

	/** The text area. */
	JTextArea		textArea		= null;

	/** The timer. */
	Timer			timer;

	/** The window adapter. */
	WindowAdapter	windowAdapter	= null;

	/**
	 * Instantiates a new swing progress bar.
	 * 
	 * @param parent
	 *            the parent
	 * @param title
	 *            the title
	 * @param text
	 *            the text
	 */
	public SwingProgressBar(Component parent, String title, String text, Image image)
	{
		frame = new JDialog();
		frame.setTitle(title);
		frame.setLocationRelativeTo(parent);
		frame.setAlwaysOnTop(true);

		try
		{
			frame.setIconImage(image);
		}
		catch (Exception e1)
		{
		}

		pb = new JProgressBar(0, 20);
		pb.setValue(0);
		pb.setIndeterminate(true);
		pb.setPreferredSize(new Dimension(300, 15));

		JPanel panel = new JPanel();
		panel.add(pb);

		JPanel panel1 = new JPanel();
		panel1.setLayout(new BorderLayout());
		panel1.add(panel, BorderLayout.CENTER);
		panel1.setBorder(new EmptyBorder(0, 0, 0, 0));
		frame.setContentPane(panel1);

		final JLabel label = new JLabel("00:00");
		panel1.add(label, BorderLayout.EAST);

		if (text != null)
		{
			textArea = new JTextArea();
			textArea.setEditable(false);
			textArea.setRows(4);
			textArea.setText(text);
			textArea.setBorder(new BevelBorder(BevelBorder.LOWERED));
			textArea.setFont(label.getFont());
			textArea.setBackground(label.getBackground());
			textArea.setWrapStyleWord(true);
			textArea.setLineWrap(true);
			panel1.add(textArea, BorderLayout.NORTH);
		}

		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		frame.setResizable(false);
		frame.addWindowListener(new WindowAdapter() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent
			 * )
			 */
			@Override
			public void windowClosing(WindowEvent e)
			{
				stop();
				if (windowAdapter != null) windowAdapter.windowClosing(e);
				super.windowClosing(e);
			}
		});

		// Create a timer.
		timer = new Timer(interval, new ActionListener() {
			public void actionPerformed(ActionEvent evt)
			{
				i = (i + 1) % 20;
				pb.setValue(i);
				Calendar now = Calendar.getInstance();
				now.setTimeInMillis(now.getTimeInMillis() - startTime.getTimeInMillis());
				SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
				label.setText(sdf.format(now.getTime()));

			}
		});

		start();
	}

	/**
	 * Instantiates a new swing progress bar.
	 * 
	 * @param title
	 *            the title
	 */
	public SwingProgressBar(String title)
	{
		this(null, title, null, null);
	}

	/**
	 * Instantiates a new swing progress bar.
	 * 
	 * @param title
	 *            the title
	 * @param text
	 *            the text
	 */
	public SwingProgressBar(String title, String text)
	{
		this(null, title, text, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected void done()
	{
		super.done();
		stop();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	public void finalize()
	{
		stop();
	}

	/**
	 * Sets the text.
	 * 
	 * @param text
	 *            the new text
	 */
	public void setText(String text)
	{
		if (textArea != null) textArea.setText(text);
	}

	/**
	 * Start.
	 */
	private void start()
	{
		startTime = Calendar.getInstance();
		timer.start();
		execute();
	}

	/**
	 * Stop.
	 */
	public synchronized void stop()
	{
		if (frame != null)
		{
			timer.stop();
			frame.dispose();
			frame = null;
		}

		cancel(true);
	}

	public void setModal()
	{
		frame.setModal(true);
	}
}
