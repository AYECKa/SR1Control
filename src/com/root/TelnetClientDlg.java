package com.root;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;
import javax.swing.JTextPane;

import org.apache.commons.net.telnet.EchoOptionHandler;
import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.apache.commons.net.telnet.SuppressGAOptionHandler;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.TelnetNotificationHandler;
import org.apache.commons.net.telnet.TerminalTypeOptionHandler;
import javax.swing.JScrollPane;

public class TelnetClientDlg extends JDialog
{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	private final JPanel		contentPanel		= new JPanel();
	private TelnetClient		tc					= new TelnetClient();
	private Thread				reader				= null;
	private OutputStream		outstr				= null;
	private JTextPane			textPane			= new JTextPane();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			TelnetClientDlg dialog = new TelnetClientDlg("192.168.2.30");
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 * 
	 * @param string
	 */
	public TelnetClientDlg(String deviceIp) throws Exception
	{
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(Main.class.getResource("/pictures/icon.png")));
		this.setTitle("Telnet - " + deviceIp);

		initTelnet(deviceIp);

		setBounds(100, 100, 800, 600);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			contentPanel.add(new JScrollPane(textPane), BorderLayout.CENTER);
		}

		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e)
			{
				if ((tc != null) && (tc.isConnected()))
				{
					try
					{
						reader.interrupt();
						reader.join(1000);
					}
					catch (Exception ex)
					{
					}
				}

				super.windowClosing(e);
			}

		});
	}

	private void initTelnet(String deviceIp) throws Exception
	{
		DefaultCaret caret = (DefaultCaret) textPane.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		TerminalTypeOptionHandler ttopt = new TerminalTypeOptionHandler("VT100", false, false, true, false);
		EchoOptionHandler echoopt = new EchoOptionHandler(true, false, true, false);
		SuppressGAOptionHandler gaopt = new SuppressGAOptionHandler(true, true, true, true);

		try
		{
			tc.addOptionHandler(ttopt);
			tc.addOptionHandler(echoopt);
			tc.addOptionHandler(gaopt);
		}
		catch (InvalidTelnetOptionException e)
		{
			System.err.println("Error registering option handlers: " + e.getMessage());
		}

		tc.connect(deviceIp);
		tc.registerNotifHandler(new TelnetNotificationHandler() {

			@Override
			public void receivedNegotiation(int negotiation_code, int option_code)
			{
				String command = null;
				if (negotiation_code == TelnetNotificationHandler.RECEIVED_DO)
				{
					command = "DO";
				}
				else if (negotiation_code == TelnetNotificationHandler.RECEIVED_DONT)
				{
					command = "DONT";
				}
				else if (negotiation_code == TelnetNotificationHandler.RECEIVED_WILL)
				{
					command = "WILL";
				}
				else if (negotiation_code == TelnetNotificationHandler.RECEIVED_WONT)
				{
					command = "WONT";
				}
				System.out.println("Received " + command + " for option code " + option_code);
			}
		});

		reader = new Thread(new Runnable() {

			@Override
			public void run()
			{
				System.out.println("Telnet Client Input Thread Started!");

				InputStream instr = tc.getInputStream();

				try
				{
					byte[] buff = new byte[4096];
					int ret_read = 0;

					do
					{
						ret_read = instr.read(buff);

						if (ret_read > 0)
						{
							byte[] newBuff = new byte[ret_read];
							int j = 0;

							for (int i = 0; i < ret_read; i++)
							{
								if (buff[i] == 10) continue;

								newBuff[j++] = buff[i];
							}

							textPane.setText(textPane.getText() + new String(newBuff, 0, j));
							// textPane.setCaretPosition(textPane.getText().length());
						}
					}
					while (ret_read >= 0);
				}
				catch (IOException e)
				{
					System.err.println("Exception while reading socket:" + e.getMessage());
				}

				try
				{
					outstr.close();
					tc.disconnect();
				}
				catch (IOException e)
				{
					System.err.println("Exception while closing telnet:" + e.getMessage());
				}

				System.out.println("Telnet Client Input Thread Ended!");
			}
		});

		reader.start();

		outstr = tc.getOutputStream();

		textPane.addKeyListener(new KeyAdapter() {

			@Override
			public void keyTyped(KeyEvent e)
			{
				try
				{
					char c = e.getKeyChar();

					if (e.getModifiersEx() == KeyEvent.CTRL_DOWN_MASK)
					{
						e.consume();
						return;
					}

					if (c == 10)
					{
						outstr.write(new byte[] { 13, (byte) c });
					}
					else
					{
						outstr.write(new byte[] { (byte) c });
					}

					outstr.flush();
					e.consume();
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}

			@Override
			public void keyPressed(KeyEvent e)
			{
				try
				{
					char c = e.getKeyChar();

					if ((e.getModifiersEx() == KeyEvent.CTRL_DOWN_MASK) && (c == 22))
					{
						// CTRL-V
						Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
						String data = (String) clipboard.getData(DataFlavor.stringFlavor);
						System.out.println("data1 = " + data);
						for (int i = 0; i < data.length(); i++)
						{
							outstr.write(new byte[] { (byte) data.charAt(i) });
						}
						outstr.flush();
						e.consume();
						return;
					}
				}
				catch (Exception ex)
				{
				}

				super.keyPressed(e);
			}

		});
	}

}
