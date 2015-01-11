package com.root;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import java.awt.GridBagLayout;

import javax.swing.JLabel;

import java.awt.GridBagConstraints;

import javax.swing.JTextField;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.UIManager;

import org.apache.commons.net.tftp.TFTPServer;

import java.awt.Toolkit;
import java.io.File;
import java.net.InetAddress;

public class SettingsDlg extends JDialog
{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	private final JPanel		contentPanel		= new JPanel();
	private JTextField			tfDeviceIp;
	private JTextField			tfReadCommunity;
	private JTextField			tfWriteCommunity;
	private ActionListener		listener			= null;
	private JTextField			textFieldMibFile;

	static class Settings
	{
		public static final String	DeviceAddress	= "DeviceAddress";
		public static final String	ReadCommunity	= "ReadCommunity";
		public static final String	WriteCommunity	= "WriteCommunity";
		public static final String	MaxEsno			= "MaxEsno";
		public static final String	MinEsno			= "MinEsno";
		public static final String	RedEsno			= "RedEsno";
		public static final String	YellowEsno		= "YellowEsno";
		public static final String	AveragingLength	= "AveragingLength";
	}

	/**
	 * Create the dialog.
	 */
	public SettingsDlg(final TFTPServer server)
	{
		setIconImage(Toolkit.getDefaultToolkit().getImage(SettingsDlg.class.getResource("/pictures/icon.png")));
		setTitle("SR1 Controller Settings");
		setBounds(100, 100, 370, 203);
		setResizable(false);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Settings:",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] { 0, 0, 0 };
		gbl_contentPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
		gbl_contentPanel.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_contentPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblDeviceIpAddress = new JLabel("Device IP Address:");
			GridBagConstraints gbc_lblDeviceIpAddress = new GridBagConstraints();
			gbc_lblDeviceIpAddress.insets = new Insets(0, 0, 5, 5);
			gbc_lblDeviceIpAddress.anchor = GridBagConstraints.WEST;
			gbc_lblDeviceIpAddress.gridx = 0;
			gbc_lblDeviceIpAddress.gridy = 0;
			contentPanel.add(lblDeviceIpAddress, gbc_lblDeviceIpAddress);
		}
		{
			tfDeviceIp = new JTextField(Param.getValue(Settings.DeviceAddress, "192.168.10.99"));
			GridBagConstraints gbc_tfDeviceIp = new GridBagConstraints();
			gbc_tfDeviceIp.insets = new Insets(0, 0, 5, 0);
			gbc_tfDeviceIp.fill = GridBagConstraints.HORIZONTAL;
			gbc_tfDeviceIp.gridx = 1;
			gbc_tfDeviceIp.gridy = 0;
			contentPanel.add(tfDeviceIp, gbc_tfDeviceIp);
			tfDeviceIp.setColumns(20);
		}
		{
			JLabel lblReadCommunity = new JLabel("Read Community:");
			GridBagConstraints gbc_lblReadCommunity = new GridBagConstraints();
			gbc_lblReadCommunity.anchor = GridBagConstraints.WEST;
			gbc_lblReadCommunity.insets = new Insets(0, 0, 5, 5);
			gbc_lblReadCommunity.gridx = 0;
			gbc_lblReadCommunity.gridy = 1;
			contentPanel.add(lblReadCommunity, gbc_lblReadCommunity);
		}
		{
			tfReadCommunity = new JTextField(Param.getValue(Settings.ReadCommunity, "public"));
			tfReadCommunity.setText("public");
			GridBagConstraints gbc_tfReadCommunity = new GridBagConstraints();
			gbc_tfReadCommunity.insets = new Insets(0, 0, 5, 0);
			gbc_tfReadCommunity.fill = GridBagConstraints.HORIZONTAL;
			gbc_tfReadCommunity.gridx = 1;
			gbc_tfReadCommunity.gridy = 1;
			contentPanel.add(tfReadCommunity, gbc_tfReadCommunity);
			tfReadCommunity.setColumns(10);
		}
		{
			JLabel lblWriteCommunity = new JLabel("Write Community:");
			GridBagConstraints gbc_lblWriteCommunity = new GridBagConstraints();
			gbc_lblWriteCommunity.anchor = GridBagConstraints.WEST;
			gbc_lblWriteCommunity.insets = new Insets(0, 0, 5, 5);
			gbc_lblWriteCommunity.gridx = 0;
			gbc_lblWriteCommunity.gridy = 2;
			contentPanel.add(lblWriteCommunity, gbc_lblWriteCommunity);
		}
		{
			tfWriteCommunity = new JTextField(Param.getValue(Settings.WriteCommunity, "private"));
			tfWriteCommunity.setText("private");
			GridBagConstraints gbc_tfWriteCommunity = new GridBagConstraints();
			gbc_tfWriteCommunity.insets = new Insets(0, 0, 5, 0);
			gbc_tfWriteCommunity.fill = GridBagConstraints.HORIZONTAL;
			gbc_tfWriteCommunity.gridx = 1;
			gbc_tfWriteCommunity.gridy = 2;
			contentPanel.add(tfWriteCommunity, gbc_tfWriteCommunity);
			tfWriteCommunity.setColumns(10);
		}
		{
			JLabel lblMibFile = new JLabel("MIB File:");
			GridBagConstraints gbc_lblMibFile = new GridBagConstraints();
			gbc_lblMibFile.anchor = GridBagConstraints.WEST;
			gbc_lblMibFile.insets = new Insets(0, 0, 5, 5);
			gbc_lblMibFile.gridx = 0;
			gbc_lblMibFile.gridy = 3;
			contentPanel.add(lblMibFile, gbc_lblMibFile);
		}
		{
			textFieldMibFile = new JTextField();
			textFieldMibFile.setEditable(false);
			GridBagConstraints gbc_textFieldMibFile = new GridBagConstraints();
			gbc_textFieldMibFile.insets = new Insets(0, 0, 5, 0);
			gbc_textFieldMibFile.fill = GridBagConstraints.HORIZONTAL;
			gbc_textFieldMibFile.gridx = 1;
			gbc_textFieldMibFile.gridy = 3;
			contentPanel.add(textFieldMibFile, gbc_textFieldMibFile);
			textFieldMibFile.setColumns(10);
			textFieldMibFile.setText(Param.getValue("MIB", "SR1c.mib"));
		}
		{
			JButton btnBrowse = new JButton("Browse");
			btnBrowse.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					JFileChooser chooser = new JFileChooser();
					chooser.setFileFilter(new FileNameExtensionFilter("MIB Files (*.mib)", "mib"));
					chooser.setDialogType(JFileChooser.OPEN_DIALOG);
					chooser.setCurrentDirectory(new File(Param.getValue("MibFolder", System.getProperty("user.dir"))));
					
					if (chooser.showOpenDialog(SettingsDlg.this) == JFileChooser.APPROVE_OPTION)
					{
						File selectedFile = chooser.getSelectedFile();
						String absolutePath = selectedFile.getAbsolutePath();
						String folder = absolutePath.substring(0, absolutePath.lastIndexOf(File.separator));
						Param.setValue("MibFolder", folder);
						textFieldMibFile.setText(selectedFile.getName());
						Param.setValue("MIB", selectedFile.getName());
						MibParser.reload();
					}
				}
			});
			GridBagConstraints gbc_btnBrowse = new GridBagConstraints();
			gbc_btnBrowse.fill = GridBagConstraints.HORIZONTAL;
			gbc_btnBrowse.gridx = 1;
			gbc_btnBrowse.gridy = 4;
			contentPanel.add(btnBrowse, gbc_btnBrowse);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				okButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0)
					{
						try
						{
							if (listener != null) listener.actionPerformed(new ActionEvent(this, 0, "OK"));

							Param.setValue(Settings.DeviceAddress, tfDeviceIp.getText());
							Param.setValue(Settings.ReadCommunity, tfReadCommunity.getText());
							Param.setValue(Settings.WriteCommunity, tfWriteCommunity.getText());
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}

						setVisible(false);
					}
				});
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				cancelButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e)
					{
						setVisible(false);
					}
				});
				buttonPane.add(cancelButton);
			}
		}
		{
			JPanel panelOperations = new JPanel();
			panelOperations.setBorder(new TitledBorder(null, "Operations:", TitledBorder.LEADING, TitledBorder.TOP,
					null, null));
			getContentPane().add(panelOperations, BorderLayout.EAST);
			GridBagLayout gbl_panelOperations = new GridBagLayout();
			gbl_panelOperations.columnWidths = new int[] { 121, 0 };
			gbl_panelOperations.rowHeights = new int[] { 23, 23, 23, 0, 0 };
			gbl_panelOperations.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
			gbl_panelOperations.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
			panelOperations.setLayout(gbl_panelOperations);
			{
				JButton btnTelnet = new JButton("Telnet");
				btnTelnet.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0)
					{
						try
						{
							TelnetClientDlg client = new TelnetClientDlg(tfDeviceIp.getText());
							client.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
							client.setAlwaysOnTop(true);
							client.setModal(true);
							client.setVisible(true);
						}
						catch (Exception e)
						{
							JOptionPane.showMessageDialog(SettingsDlg.this,
									"Unable to start telnet session:\n\n" + e.getMessage(), "Telnet Session",
									JOptionPane.ERROR_MESSAGE);
						}
					}
				});
				GridBagConstraints gbc_btnTelnet = new GridBagConstraints();
				gbc_btnTelnet.fill = GridBagConstraints.HORIZONTAL;
				gbc_btnTelnet.insets = new Insets(0, 0, 5, 0);
				gbc_btnTelnet.gridx = 0;
				gbc_btnTelnet.gridy = 0;
				panelOperations.add(btnTelnet, gbc_btnTelnet);
			}
			{
				JButton btnSoftwareUpgrade = new JButton("Software Upgrade");
				GridBagConstraints gbc_btnSoftwareUpgrade = new GridBagConstraints();
				gbc_btnSoftwareUpgrade.fill = GridBagConstraints.HORIZONTAL;
				gbc_btnSoftwareUpgrade.insets = new Insets(0, 0, 5, 0);
				gbc_btnSoftwareUpgrade.gridx = 0;
				gbc_btnSoftwareUpgrade.gridy = 1;
				panelOperations.add(btnSoftwareUpgrade, gbc_btnSoftwareUpgrade);
				btnSoftwareUpgrade.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent event)
					{
						try
						{
							SoftwareUpgradeDlg dlg = new SoftwareUpgradeDlg(SettingsDlg.this, InetAddress
									.getByName(tfDeviceIp.getText()), tfReadCommunity.getText(), tfWriteCommunity
									.getText(), server);
							dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
							dlg.setModal(true);
							dlg.setAlwaysOnTop(true);
							dlg.setVisible(true);
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				});
			}
			{
				JButton btnSupportDump = new JButton("Support Dump");
				GridBagConstraints gbc_btnSupportDump = new GridBagConstraints();
				gbc_btnSupportDump.insets = new Insets(0, 0, 5, 0);
				gbc_btnSupportDump.fill = GridBagConstraints.HORIZONTAL;
				gbc_btnSupportDump.gridx = 0;
				gbc_btnSupportDump.gridy = 2;
				panelOperations.add(btnSupportDump, gbc_btnSupportDump);
				{
					JButton btnColdReset = new JButton("Cold Reset");
					btnColdReset.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e)
						{
							try
							{
								DeviceInterface.getInstance().coldReset();
							}
							catch (Exception ex)
							{
								ex.printStackTrace();
							}
						}
					});
					GridBagConstraints gbc_btnColdReset = new GridBagConstraints();
					gbc_btnColdReset.fill = GridBagConstraints.HORIZONTAL;
					gbc_btnColdReset.gridx = 0;
					gbc_btnColdReset.gridy = 3;
					panelOperations.add(btnColdReset, gbc_btnColdReset);
				}
				btnSupportDump.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent event)
					{
						SupportDump dump = new SupportDump();
						dump.dump(tfDeviceIp.getText(), tfReadCommunity.getText(), tfWriteCommunity.getText(), server);
					}
				});
			}
		}
	}

	public void addActionListener(ActionListener listener)
	{
		this.listener = listener;
	}

	public String getDeviceAddress()
	{
		return tfDeviceIp.getText();
	}

	public String getReadCommunity()
	{
		return tfReadCommunity.getText();
	}

	public String getWriteCommunity()
	{
		return tfWriteCommunity.getText();
	}
}
