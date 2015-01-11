package com.root;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class TftpOpenFileDialog extends JDialog
{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	private final JPanel		contentPanel		= new JPanel();
	private JTable				table;
	private DefaultTableModel	model				= new DefaultTableModel() {

														/**
		 * 
		 */
														private static final long	serialVersionUID	= 1L;

														@Override
														public boolean isCellEditable(int row, int column)
														{
															return false;
														}
													};

	/**
	 * Create the dialog.
	 */
	public TftpOpenFileDialog(final String extension, final ActionListener listener)
	{
		setTitle("TFTP Open File Dialog");
		setIconImage(Toolkit.getDefaultToolkit().getImage(TftpOpenFileDialog.class.getResource("/pictures/icon.png")));
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JScrollPane scrollPane = new JScrollPane();
			contentPanel.add(scrollPane);
			{
				table = new JTable();
				table.setModel(model);
				scrollPane.setViewportView(table);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);

				okButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e)
					{
						int row = table.getSelectedRow();

						if (row >= 0)
						{
							String filename = model.getValueAt(row, 0).toString();
							listener.actionPerformed(new ActionEvent(TftpOpenFileDialog.this, 0, filename));
						}

						dispose();
					}
				});
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);

				cancelButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e)
					{
						dispose();
					}
				});
			}
		}

		model.addColumn("Filename");

		File folder = new File("TFTP");
		String[] filenames = folder.list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name)
			{
				return name.endsWith("." + extension);
			}
		});

		for (String filename : filenames)
		{
			model.addRow(new Object[] { filename });
		}
	}

}
