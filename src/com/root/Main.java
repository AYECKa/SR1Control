package com.root;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.apache.commons.net.tftp.TFTPServer;
import org.apache.commons.net.tftp.TFTPServer.ServerMode;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import java.awt.Toolkit;
import javax.swing.border.BevelBorder;
import javax.swing.SpringLayout;

public class Main extends JFrame
{

	/**
	 * 
	 */
	private static final long		serialVersionUID	= 1L;
	private JPanel					contentPane;
	private Gauge					gEsno				= null;
	private Gauge					gPowerLevel			= null;
	private Gauge					gTraffic			= null;
	private SettingsDlg				dlg					= null;
	private SwingWorker<Void, Void>	worker				= null;
	private JLabel					softwareVersion		= new JLabel("Software Version: N/A");
	private JLabel					fpgaVersion			= new JLabel("FPGA Version: N/A");
	private JLabel					hardwareVersion		= new JLabel("Hardware Version: N/A");
	private TFTPServer				server				= null;
	private ManualResetEvent		event				= new ManualResetEvent(false);
	private JLabel					labelNotLocked		= new JLabel("NOT LOCKED!");
	private static String			OS					= System.getProperty("os.name");

	public static boolean isLinux()
	{
		return (OS.toLowerCase().indexOf("linux") >= 0);

	}

	public static boolean isWindows()
	{
		return (OS.toLowerCase().indexOf("win") >= 0);

	}

	public static boolean isMac()
	{
		return (OS.toLowerCase().indexOf("mac") >= 0);
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		try
		{
			if (args.length == 0)
			{
				RollingFileAppender appender = new RollingFileAppender(new PatternLayout("%d %-5p: %m%n"),
						"Sr1Control.log", true);
				appender.setImmediateFlush(true);
				appender.setMaxBackupIndex(3);
				appender.setMaxFileSize("10MB");
				appender.activateOptions();

				Logger.getRootLogger().addAppender(appender);

				System.setOut(new PrintStream(new ByteArrayOutputStream()) {

					@Override
					public void print(String message)
					{
						Logger.getRootLogger().info(message);
					}

					@Override
					public void println(String message)
					{
						Logger.getRootLogger().info(message);
					}
				});

				System.setErr(new PrintStream(new ByteArrayOutputStream()) {

					@Override
					public void print(String message)
					{
						Logger.getRootLogger().error(message);
					}

					@Override
					public void println(String message)
					{
						Logger.getRootLogger().error(message);
					}
				});
			}

			System.out.println("SR1 Controller Log Started");

			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

			EventQueue.invokeLater(new Runnable() {
				public void run()
				{
					try
					{
						Main frame = new Main();
						frame.setVisible(true);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			});
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Create the frame.
	 */
	public Main() throws Exception
	{
		int tftpPort = new Integer(Param.getValue("TFTPPort", "69"));
		boolean socketOccupied = false;

		try
		{
			System.out.println("OS is: " + OS);

			String command = "";
			int indexOfPort = 0;

			if (isWindows())
			{
				String path = System.getenv("WINDIR") + "\\system32";
				command = path + "\\netstat.exe -na";
				indexOfPort = 1;
			}
			else if (isLinux())
			{
				command = "netstat -na | grep udp";
				indexOfPort = 3;
			}

			if (command.length() > 0)
			{
				// Check TFTP server existence
				Process p = Runtime.getRuntime().exec(command);
				// p.waitFor();

				BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
				while (socketOccupied == false)
				{
					String line = reader.readLine();

					if (line == null) break;

					if (line.toLowerCase().indexOf("udp") >= 0)
					{
						String[] split = line.split(" ");

						Vector<String> vec = new Vector<String>();

						for (String s : split)
						{
							if (s.trim().length() == 0) continue;

							vec.add(s.trim());
						}

						if (vec.size() > indexOfPort)
						{
							String hostAndPort = vec.get(indexOfPort);
							int lastIndexOfColons = hostAndPort.lastIndexOf(':');
							if (lastIndexOfColons > 0)
							{
								int port = new Integer(hostAndPort.substring(lastIndexOfColons + 1));
								if (port == tftpPort)
								{
									socketOccupied = true;
									break;
								}
							}
						}
					}
				}

				reader.close();

				p.waitFor();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		if (socketOccupied)
		{
			JOptionPane.showMessageDialog(this, "TFTP Server not started => Port Occupied",
					"SR1 Controller - TFTP Server Startup", JOptionPane.WARNING_MESSAGE);
		}
		else
		{
			try
			{
				File file = new File("TFTP");
				if (file.exists() == false) file.mkdir();
				server = new TFTPServer(file, file, tftpPort, ServerMode.GET_ONLY, null, null);

				System.out.println("TFTP Server Started!");
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		dlg = new SettingsDlg(server);
		dlg.setAlwaysOnTop(true);
		dlg.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		dlg.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				settingsChanged();
			}
		});

		int gaugeSize = new Integer(Param.getValue("GaugeSize", "200"));
		setTitle("SR1 Controller (" + Version.VersionInfo + ") - " + dlg.getDeviceAddress());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 706, 285);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		int minWidth = new Integer(Param.getValue("MinWidth", "600"));
		int minHeight = new Integer(Param.getValue("MinHeight", "250"));
		setMinimumSize(new Dimension(minWidth, minHeight));

		double minEsno = new Double(Param.getValue("MinEsno", "0"));
		double maxEsno = new Double(Param.getValue("MaxEsno", "30"));
		double redEsno = new Double(Param.getValue("RedEsno", "3"));
		double yellowEsno = new Double(Param.getValue("YellowEsno", "12"));

		double minPowerLevel = new Double(Param.getValue("MinPowerLevel", "-70"));
		double maxPowerLevel = new Double(Param.getValue("MaxPowerLevel", "-20"));
		double redPowerLevel = new Double(Param.getValue("RedPowerLevel", "-60"));
		double yellowPowerLevel = new Double(Param.getValue("YellowPowerLevel", "-50"));

		double minBitRate = new Double(Param.getValue("MinBitRate", "0"));
		double maxBitRate = new Double(Param.getValue("MaxBitRate", "100"));
		SpringLayout sl_contentPane = new SpringLayout();
		contentPane.setLayout(sl_contentPane);

		JPanel bottomPanel = new JPanel();
		sl_contentPane.putConstraint(SpringLayout.NORTH, bottomPanel, 5, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, bottomPanel, 5, SpringLayout.WEST, contentPane);
		bottomPanel.setBorder(null);
		bottomPanel.setLayout(new BorderLayout());
		contentPane.add(bottomPanel);

		JPanel panelInfo = new JPanel();
		panelInfo.setBorder(null);
		panelInfo.setLayout(new FlowLayout(FlowLayout.LEFT));
		bottomPanel.add(panelInfo, BorderLayout.CENTER);

		panelInfo.add(softwareVersion);
		panelInfo.add(fpgaVersion);
		panelInfo.add(hardwareVersion);
		final JButton buttonAlbert = new JButton(new ImageIcon(Main.class.getResource("/pictures/einstein.png")));
		sl_contentPane.putConstraint(SpringLayout.NORTH, buttonAlbert, 5, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, buttonAlbert, 5, SpringLayout.EAST, contentPane);
		contentPane.add(buttonAlbert);
		buttonAlbert.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		buttonAlbert.setPreferredSize(new Dimension(64, 64));
		gEsno = new Gauge("Es/No [dB]", gaugeSize, minEsno, maxEsno);
		sl_contentPane.putConstraint(SpringLayout.NORTH, gEsno, 5, SpringLayout.SOUTH, bottomPanel);
		sl_contentPane.putConstraint(SpringLayout.WEST, gEsno, 5, SpringLayout.WEST, contentPane);
		contentPane.add(gEsno);
		gPowerLevel = new Gauge("Power [dBm]", gaugeSize, minPowerLevel, maxPowerLevel);
		sl_contentPane.putConstraint(SpringLayout.NORTH, gPowerLevel, 5, SpringLayout.SOUTH, bottomPanel);
		sl_contentPane.putConstraint(SpringLayout.WEST, gPowerLevel, 5, SpringLayout.EAST, gEsno);
		contentPane.add(gPowerLevel);
		gTraffic = new Gauge("Traffic [Mbps]", gaugeSize, minBitRate, maxBitRate);
		sl_contentPane.putConstraint(SpringLayout.NORTH, gTraffic, 5, SpringLayout.SOUTH, bottomPanel);
		sl_contentPane.putConstraint(SpringLayout.WEST, gTraffic, 5, SpringLayout.EAST, gPowerLevel);
		contentPane.add(gTraffic);

		labelNotLocked.setForeground(Color.RED);
		labelNotLocked.setFont(new Font("Tahoma", Font.BOLD, 50));
		labelNotLocked.setVisible(false);
		sl_contentPane.putConstraint(SpringLayout.NORTH, labelNotLocked, 5, SpringLayout.SOUTH, bottomPanel);
		sl_contentPane.putConstraint(SpringLayout.WEST, labelNotLocked, 5, SpringLayout.WEST, contentPane);
		contentPane.add(labelNotLocked);

		gPowerLevel.setRange(Color.RED, minPowerLevel, redPowerLevel);
		gPowerLevel.setRange(Color.YELLOW, redPowerLevel, yellowPowerLevel);
		gPowerLevel.setRange(Color.GREEN, yellowPowerLevel, maxPowerLevel);
		gEsno.setRange(Color.RED, minEsno, redEsno);
		gEsno.setRange(Color.YELLOW, redEsno, yellowEsno);
		gEsno.setRange(Color.GREEN, yellowEsno, maxEsno);
		buttonAlbert.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				dlg.setVisible(true);
			}
		});

		Font font = new Font("Tahoma", Font.PLAIN, 14);

		for (Component component : panelInfo.getComponents())
		{
			if (component instanceof JLabel)
			{
				component.setFont(font);
			}
		}

		this.setIconImage(Toolkit.getDefaultToolkit().getImage(Main.class.getResource("/pictures/icon.png")));

		worker = new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() throws Exception
			{
				DeviceInterface deviceInterface = createDeviceInterface();

				int pollingInterval = new Integer(Param.getValue("PollingIntervalInSeconds", "1")) * 1000;

				while (true)
				{
					Thread.sleep(pollingInterval);

					if (event.isSignalled() || (deviceInterface == null))
					{
						event.reset();
						deviceInterface = createDeviceInterface();
					}

					try
					{
						if (deviceInterface != null)
						{
							double esno = deviceInterface.getEsno();
							double linkMargin = deviceInterface.getLinkMargin();
							double powerLevel = deviceInterface.getPowerLevel();
							double bitRate = deviceInterface.getBitRate();
							boolean isLocked = deviceInterface.isLocked();

							updateGauges(esno, linkMargin, powerLevel, bitRate, isLocked);
							updateVersions(deviceInterface);
						}
					}
					catch (Exception e)
					{
						// e.printStackTrace();
					}
				}

				// return null;
			}
		};

		worker.execute();

		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e)
			{
				if (server != null)
				{
					try
					{
						System.out.println("Closing TFTP server");
						server.shutdown();
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}

				super.windowClosing(e);
			}

			@Override
			public void windowIconified(WindowEvent e)
			{
				Main.this.setVisible(false);
			}
		});

		addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e)
			{
				int width = getWidth();
				int effectiveWidth = width - 80;
				gEsno.setGaugeSize(effectiveWidth / 3);
				gPowerLevel.setGaugeSize(effectiveWidth / 3);
				gTraffic.setGaugeSize(effectiveWidth / 3);
				super.componentResized(e);
				Param.setValue("Width", String.valueOf(width));
				Param.setValue("GaugeSize", String.valueOf(effectiveWidth));
				Param.setValue("Height", String.valueOf(getHeight()));
			}
		});

		setupSystemTray();
	}

	private void deiconfy()
	{
		int state = getExtendedState();

		if ((state & JFrame.ICONIFIED) == JFrame.ICONIFIED)
		{
			setVisible(true);
			state &= ~JFrame.ICONIFIED;
			setExtendedState(state);
		}
	}

	private void setupSystemTray()
	{
		if (SystemTray.isSupported() == false) return;

		TrayIcon trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().getImage(
				Main.class.getResource("/pictures/icon.png")));

		trayIcon.setImageAutoSize(true);
		trayIcon.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event)
			{
				deiconfy();
			}
		});

		SystemTray tray = SystemTray.getSystemTray();

		try
		{
			tray.add(trayIcon);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	protected DeviceInterface createDeviceInterface()
	{
		try
		{
			DeviceInterface deviceInterface = DeviceInterface.createInstance(
					InetAddress.getByName(dlg.getDeviceAddress()), dlg.getReadCommunity(), dlg.getWriteCommunity(),
					server);

			deviceInterface.setAveragingLength(new Integer(Param.getValue("BitRateAverageLength", "10")));

			updateVersions(deviceInterface);

			return deviceInterface;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	private void updateVersions(DeviceInterface deviceInterface)
	{
		softwareVersion.setText("Software Version: " + deviceInterface.getSoftwareVersion());
		fpgaVersion.setText("FPGA Version: " + deviceInterface.getFpgaVersion());
		hardwareVersion.setText("Hardware Version: " + deviceInterface.getHardwareVersion());
	}

	protected void settingsChanged()
	{
		setTitle("SR1 Controller - " + dlg.getDeviceAddress());
		event.set();
	}

	protected void updateGauges(double esno, double linkMargin, double powerLevel, double bitRate, boolean isLocked)
	{
		if (isLocked)
		{
			gEsno.setValue(esno);
			gPowerLevel.setValue(powerLevel);
			gTraffic.setValue(bitRate);

			if (gEsno.isRed() || gPowerLevel.isRed()) deiconfy();
		}
		else
		{
			deiconfy();
		}

		gEsno.setVisible(isLocked);
		gPowerLevel.setVisible(isLocked);
		gTraffic.setVisible(isLocked);

		labelNotLocked.setVisible(!isLocked);
	}
}
