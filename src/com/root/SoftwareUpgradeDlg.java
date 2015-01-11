package com.root;

import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JTextField;

import org.apache.commons.net.tftp.TFTPServer;

import java.awt.Component;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.net.InetAddress;

public class SoftwareUpgradeDlg extends JDialog
{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	private final JPanel		contentPanel		= new JPanel();
	private JTextField			tfSoftwareFilename;
	private JTextField			tfFirmwareFilename;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			SoftwareUpgradeDlg dialog = new SoftwareUpgradeDlg(null, InetAddress.getLocalHost(), "public",
					"private", null);
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
	 */
	public SoftwareUpgradeDlg(final Component parent, final InetAddress deviceIp, final String readCommunity,
			final String writeCommunity, final TFTPServer server)
	{
		System.out.println((server != null) ? "Using built-in TFTP Server" : "Using external TFTP Server");
		setIconImage(Toolkit.getDefaultToolkit().getImage(SoftwareUpgradeDlg.class.getResource("/pictures/icon.png")));
		setTitle("SR1 Controller - Software Upgrade");
		setBounds(100, 100, 450, 111);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] { 0, 0, 0, 0, 0 };
		gbl_contentPanel.rowHeights = new int[] { 0, 0, 0 };
		gbl_contentPanel.columnWeights = new double[] { 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_contentPanel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblSoftware = new JLabel("Software:");
			GridBagConstraints gbc_lblSoftware = new GridBagConstraints();
			gbc_lblSoftware.fill = GridBagConstraints.VERTICAL;
			gbc_lblSoftware.insets = new Insets(0, 0, 5, 5);
			gbc_lblSoftware.anchor = GridBagConstraints.WEST;
			gbc_lblSoftware.gridx = 0;
			gbc_lblSoftware.gridy = 0;
			contentPanel.add(lblSoftware, gbc_lblSoftware);
		}
		{
			tfSoftwareFilename = new JTextField();
			tfSoftwareFilename.setEditable(false);
			GridBagConstraints gbc_tfSoftwareFilename = new GridBagConstraints();
			gbc_tfSoftwareFilename.insets = new Insets(0, 0, 5, 5);
			gbc_tfSoftwareFilename.fill = GridBagConstraints.BOTH;
			gbc_tfSoftwareFilename.gridx = 1;
			gbc_tfSoftwareFilename.gridy = 0;
			contentPanel.add(tfSoftwareFilename, gbc_tfSoftwareFilename);
			tfSoftwareFilename.setColumns(10);
		}
		{
			JButton btnBrowseSoftwareFilename = new JButton("Browse");
			btnBrowseSoftwareFilename.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event)
				{
					if (server != null)
					{
						TftpOpenFileDialog dlg = new TftpOpenFileDialog("asw", new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e)
							{
								tfSoftwareFilename.setText(e.getActionCommand());
							}
						});
						dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
						dlg.setAlwaysOnTop(true);
						dlg.setModal(true);
						dlg.setVisible(true);
					}
					else
					{
						JFileChooser chooser = new JFileChooser(Param.getValue("TFTPFolder", "TFTP"));
						chooser.setFileFilter(new FileNameExtensionFilter("Upgrade Files (*.asw)", "*.asw"));
						chooser.setDialogType(JFileChooser.OPEN_DIALOG);
						if (chooser.showSaveDialog(SoftwareUpgradeDlg.this) == JFileChooser.APPROVE_OPTION)
						{
							tfSoftwareFilename.setText(chooser.getSelectedFile().getName());
							Param.setValue("TFTPFolder", chooser.getSelectedFile().getPath());
						}
					}
				}
			});
			GridBagConstraints gbc_btnBrowseSoftwareFilename = new GridBagConstraints();
			gbc_btnBrowseSoftwareFilename.fill = GridBagConstraints.VERTICAL;
			gbc_btnBrowseSoftwareFilename.insets = new Insets(0, 0, 5, 5);
			gbc_btnBrowseSoftwareFilename.gridx = 2;
			gbc_btnBrowseSoftwareFilename.gridy = 0;
			contentPanel.add(btnBrowseSoftwareFilename, gbc_btnBrowseSoftwareFilename);
		}
		{
			JButton btnUpgradeSoftware = new JButton("Upgrade");
			btnUpgradeSoftware.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event)
				{
					new SwingProgressBar(parent, "Software Upgrade", "Initializing ...", Toolkit.getDefaultToolkit()
							.getImage(SoftwareUpgradeDlg.class.getResource("/pictures/icon.png"))) {

						@Override
						protected Object doInBackground() throws Exception
						{
							try
							{
								DeviceInterface device = DeviceInterface.getInstance();
								device.startSoftwareUpgrade(tfSoftwareFilename.getText(), this);
								
								JOptionPane.showMessageDialog(parent, "Upgrade Completed Successfully", "Software Upgrade", JOptionPane.INFORMATION_MESSAGE);
							}
							catch (Exception e)
							{
								JOptionPane.showMessageDialog(parent, "Upgrade Failed", "Software Upgrade", JOptionPane.ERROR_MESSAGE);
								e.printStackTrace();
							}

							return null;
						}
					};

					dispose();
					parent.setVisible(false);
				}
			});
			GridBagConstraints gbc_btnUpgradeSoftware = new GridBagConstraints();
			gbc_btnUpgradeSoftware.fill = GridBagConstraints.VERTICAL;
			gbc_btnUpgradeSoftware.insets = new Insets(0, 0, 5, 0);
			gbc_btnUpgradeSoftware.gridx = 3;
			gbc_btnUpgradeSoftware.gridy = 0;
			contentPanel.add(btnUpgradeSoftware, gbc_btnUpgradeSoftware);
		}
		{
			JLabel lblFirmware = new JLabel("Firmware:");
			GridBagConstraints gbc_lblFirmware = new GridBagConstraints();
			gbc_lblFirmware.fill = GridBagConstraints.VERTICAL;
			gbc_lblFirmware.anchor = GridBagConstraints.WEST;
			gbc_lblFirmware.insets = new Insets(0, 0, 0, 5);
			gbc_lblFirmware.gridx = 0;
			gbc_lblFirmware.gridy = 1;
			contentPanel.add(lblFirmware, gbc_lblFirmware);
		}
		{
			tfFirmwareFilename = new JTextField();
			tfFirmwareFilename.setEditable(false);
			GridBagConstraints gbc_tfFirmwareFilename = new GridBagConstraints();
			gbc_tfFirmwareFilename.insets = new Insets(0, 0, 0, 5);
			gbc_tfFirmwareFilename.fill = GridBagConstraints.BOTH;
			gbc_tfFirmwareFilename.gridx = 1;
			gbc_tfFirmwareFilename.gridy = 1;
			contentPanel.add(tfFirmwareFilename, gbc_tfFirmwareFilename);
			tfFirmwareFilename.setColumns(10);
		}
		{
			JButton btnBrowseFirmwareFilename = new JButton("Browse");
			btnBrowseFirmwareFilename.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event)
				{
					if (server != null)
					{
						TftpOpenFileDialog dlg = new TftpOpenFileDialog("afp", new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e)
							{
								tfFirmwareFilename.setText(e.getActionCommand());
							}
						});

						dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
						dlg.setAlwaysOnTop(true);
						dlg.setModal(true);
						dlg.setVisible(true);
					}
					else
					{
						JFileChooser chooser = new JFileChooser(Param.getValue("TFTPFolder", "TFTP"));
						chooser.setFileFilter(new FileNameExtensionFilter("Upgrade Files (*.afp)", "*.afp"));
						chooser.setDialogType(JFileChooser.OPEN_DIALOG);
						if (chooser.showSaveDialog(SoftwareUpgradeDlg.this) == JFileChooser.APPROVE_OPTION)
						{
							tfFirmwareFilename.setText(chooser.getSelectedFile().getName());
							Param.setValue("TFTPFolder", chooser.getSelectedFile().getPath());
						}
					}
				}
			});
			GridBagConstraints gbc_btnBrowseFirmwareFilename = new GridBagConstraints();
			gbc_btnBrowseFirmwareFilename.fill = GridBagConstraints.VERTICAL;
			gbc_btnBrowseFirmwareFilename.insets = new Insets(0, 0, 0, 5);
			gbc_btnBrowseFirmwareFilename.gridx = 2;
			gbc_btnBrowseFirmwareFilename.gridy = 1;
			contentPanel.add(btnBrowseFirmwareFilename, gbc_btnBrowseFirmwareFilename);
		}
		{
			JButton btnUpgradeFirmware = new JButton("Upgrade");
			btnUpgradeFirmware.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event)
				{
					new SwingProgressBar(parent, "Firmware Upgrade", "Initializing ...", Toolkit.getDefaultToolkit()
							.getImage(SoftwareUpgradeDlg.class.getResource("/pictures/icon.png"))) {

						@Override
						protected Object doInBackground() throws Exception
						{
							try
							{
								DeviceInterface device = DeviceInterface.getInstance();
								device.startFirmwareUpgrade(tfFirmwareFilename.getText(), this);
								JOptionPane.showMessageDialog(parent, "Upgrade Completed Successfully", "Firmware Upgrade", JOptionPane.INFORMATION_MESSAGE);
							}
							catch (Exception e)
							{
								JOptionPane.showMessageDialog(parent, "Upgrade Failed", "Firmware Upgrade", JOptionPane.ERROR_MESSAGE);
								e.printStackTrace();
							}

							return null;
						}
					};

					dispose();
					parent.setVisible(false);
				}
			});
			GridBagConstraints gbc_btnUpgradeFirmware = new GridBagConstraints();
			gbc_btnUpgradeFirmware.fill = GridBagConstraints.VERTICAL;
			gbc_btnUpgradeFirmware.gridx = 3;
			gbc_btnUpgradeFirmware.gridy = 1;
			contentPanel.add(btnUpgradeFirmware, gbc_btnUpgradeFirmware);
		}
	}

}
