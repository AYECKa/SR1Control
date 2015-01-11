package com.root;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;

public class Gauge extends JPanel
{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	private static JTextField	tfMinValue			= new JTextField("0", 10);
	private static JTextField	tfMaxValue			= new JTextField("30", 10);
	private static JTextField	tfValue				= new JTextField("0", 10);
	private static JTextField	tfRedValue			= new JTextField("10", 10);
	private static JTextField	tfYellowValue		= new JTextField("20", 10);
	private static JTextField	tfGreenValue		= new JTextField("30", 10);
	private static JTextField	tfGridValue			= new JTextField("10", 10);
	private static JButton		buttonApply			= new JButton("Apply");
	private static Gauge		gauge				= null;
	private static boolean		noColor				= true;

	public static void main(String[] args)
	{
		if (args.length > 0)
		{
			if (args[0].toLowerCase().equalsIgnoreCase("traffic"))
			{
				gauge = new Gauge("Traffic [Mbps]", 200, 0, 100);
				tfMinValue.setText("0");
				tfMaxValue.setText("100");
			}
			else if (args[0].toLowerCase().equalsIgnoreCase("power"))
			{
				gauge = new Gauge("Power [dBm]", 200, -70, -20);
				gauge.setRange(Color.RED, -70, -60);
				gauge.setRange(Color.YELLOW, -60, -50);
				gauge.setRange(Color.GREEN, -50, -20);
				tfMinValue.setText("-70");
				tfMaxValue.setText("-20");
				tfValue.setText("-70");
				noColor = false;
			}
		}

		if (gauge == null)
		{
			gauge = new Gauge("Es/No", 200, 0, 30);
			gauge.setRange(Color.GREEN, 20, 30);
			gauge.setRange(Color.YELLOW, 10, 20);
			gauge.setRange(Color.RED, 0, 10);
			noColor = false;
		}

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(gauge, BorderLayout.CENTER);

		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setPreferredSize(new Dimension(200, 200));
		panel.setBorder(new EtchedBorder());

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(1, 1, 1, 1);
		c.anchor = GridBagConstraints.WEST;
		c.gridy = 0;
		c.gridx = 0;
		panel.add(new JLabel("Min Value:"), c);
		c.gridx = 1;
		panel.add(tfMinValue, c);
		c.gridy++;
		c.gridx = 0;
		panel.add(new JLabel("Max Value:"), c);
		c.gridx = 1;
		panel.add(tfMaxValue, c);
		c.gridy++;
		c.gridx = 0;
		panel.add(new JLabel("Value:"), c);
		c.gridx = 1;
		panel.add(tfValue, c);
		c.gridy++;
		c.gridx = 0;
		panel.add(new JLabel("Red Value:"), c);
		c.gridx = 1;
		panel.add(tfRedValue, c);
		c.gridy++;
		c.gridx = 0;
		panel.add(new JLabel("Yello Value:"), c);
		c.gridx = 1;
		panel.add(tfYellowValue, c);
		c.gridy++;
		c.gridx = 0;
		panel.add(new JLabel("Green Value:"), c);
		c.gridx = 1;
		panel.add(tfGreenValue, c);
		c.gridy++;
		c.gridx = 0;
		panel.add(new JLabel("Grid Value:"), c);
		c.gridx = 1;
		panel.add(tfGridValue, c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.BOTH;
		panel.add(buttonApply, c);

		Font font = new Font("Tahoma", Font.PLAIN, 12);

		for (Component component : panel.getComponents())
		{
			component.setFont(font);

			if (component instanceof JTextField)
			{
				JTextField tf = (JTextField) component;
				tf.setHorizontalAlignment(JTextField.RIGHT);
			}
		}

		buttonApply.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				double minValue = Double.parseDouble(tfMinValue.getText());
				double maxValue = Double.parseDouble(tfMaxValue.getText());
				double value = Double.parseDouble(tfValue.getText());
				int gridValue = Integer.parseInt(tfGridValue.getText());
				gauge.clearRanges();
				gauge.SetMinMaxValue(minValue, maxValue);
				if (noColor == false)
				{
					double redValue = Double.parseDouble(tfRedValue.getText());
					double yellowValue = Double.parseDouble(tfYellowValue.getText());
					double greenValue = Double.parseDouble(tfGreenValue.getText());

					gauge.setRange(Color.RED, minValue, redValue);
					gauge.setRange(Color.YELLOW, redValue, yellowValue);
					gauge.setRange(Color.GREEN, yellowValue, greenValue);
				}
				gauge.setValue(value);
				gauge.setGrid(gridValue);
			}
		});

		frame.getContentPane().add(panel, BorderLayout.EAST);
		frame.pack();
		frame.setVisible(true);
	}

	class Range
	{
		public Color	color;
		public double	from;
		public double	to;

		public Range(Color color, double from, double to)
		{
			this.color = color;
			this.from = from;
			this.to = to;
		}
	}

	private Vector<Range>	ranges		= new Vector<Range>();
	private double			minValue	= 0;
	private double			maxValue	= 0;
	private double			value		= 0;
	private int				size		= 0;
	private String			title		= "";
	private int				grid		= 10;

	public Gauge(String title, int size, double minValue, double maxValue)
	{
		setPreferredSize(new Dimension(size, size));
		this.value = minValue;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.size = size;
		this.title = title;
	}

	public void SetMinMaxValue(double minValue, double maxValue)
	{
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.repaint();
	}

	public void setRange(Color color, double from, double to)
	{
		if (from >= to) return;
		if (from > maxValue) return;
		if (from < minValue) from = minValue;
		if (to > maxValue) to = maxValue;
		ranges.add(new Range(color, from, to));
	}

	public void setValue(double value)
	{
		this.value = value;
		this.repaint();
	}

	public double getValue()
	{
		return value;
	}

	public void clearRanges()
	{
		ranges.clear();
	}

	public void setGrid(int grid)
	{
		this.grid = grid;
		this.repaint();
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		Graphics2D g2d = (Graphics2D) g;

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Color bgColor = UIManager.getColor("Panel.background");

		g2d.setColor(bgColor);
		g2d.fillRect(0, 0, size, size);

		for (Range range : ranges)
		{
			g2d.setColor(range.color);

			int startAngle = new Double(270 - ((range.to - minValue) / (maxValue - minValue)) * 270 - 45).intValue();
			int angleSweep = new Double((range.to - range.from) / (maxValue - minValue) * 270).intValue();

			g2d.fillArc(0, 0, size, size, startAngle, angleSweep);
		}

		// if (ranges.size() == 0)
		{
			g2d.setColor(Color.BLACK);
			g2d.drawArc(0, 0, size, size, -45, 270);
			g2d.drawArc(10, 10, size - 20, size - 20, -45, 270);
		}

		g2d.setColor(bgColor);
		g2d.fillArc(10, 10, size - 20, size - 20, -46, 272);

		g2d.setColor(Color.BLUE);
		g2d.fillArc(size / 2 - 5, size / 2 - 5, 10, 10, 0, 360);

		double displayValue = value;
		if (displayValue < minValue) displayValue = minValue;
		if (displayValue > maxValue) displayValue = maxValue;
		double angle = 270 - (displayValue - minValue) / (maxValue - minValue) * 270 - 45;
		double x = size / 2 + Math.cos(Math.toRadians(angle)) * size / 2;
		double y = size / 2 - Math.sin(Math.toRadians(angle)) * size / 2;

		g2d.drawLine(size / 2, size / 2, new Double(x).intValue(), new Double(y).intValue());

		g2d.setColor(Color.GRAY);

		double step = (maxValue - minValue) / grid;

		double gridValue = minValue;

		String fontName = Param.getValue("FontName", "Tahoma");

		g2d.setFont(new Font(fontName, Font.PLAIN, new Integer(Param.getValue("GaugeTickFontSize", "10"))));
		FontMetrics fm = g2d.getFontMetrics();

		while (gridValue <= maxValue)
		{
			angle = 270 - (gridValue - minValue) / (maxValue - minValue) * 270 - 45;
			x = size / 2 + Math.cos(Math.toRadians(angle)) * size / 2;
			y = size / 2 - Math.sin(Math.toRadians(angle)) * size / 2;

			double xx = size / 2 + Math.cos(Math.toRadians(angle)) * size * 0.40;
			double yy = size / 2 - Math.sin(Math.toRadians(angle)) * size * 0.40;

			g2d.drawLine(new Double(xx).intValue(), new Double(yy).intValue(), new Double(x).intValue(),
					new Double(y).intValue());

			String text = String.format("%.1f", gridValue);
			int width = fm.stringWidth(text);
			int height = fm.getHeight();

			int textX = new Double(xx).intValue() + 1;
			int textY = new Double(yy).intValue() + 1;

			if (xx == x) textX = size / 2 - width / 2;
			else if (xx < x) textX -= (width + 2);

			if (yy > y) textY += (height);

			g2d.drawString(text, textX, textY);

			gridValue = gridValue + step;
		}

		g2d.setColor(Color.BLACK);
		g2d.setFont(new Font(fontName, Font.BOLD, new Integer(Param.getValue("GaugeValueFontSize", "12"))));
		fm = g2d.getFontMetrics();
		int width = fm.stringWidth(title);
		int height = fm.getHeight();
		g2d.drawString(title, size / 2 - width / 2, size - height * 2);

		String valueString = String.format("%.1f", value);
		width = fm.stringWidth(valueString);
		g2d.drawString(valueString, size / 2 - width / 2, size - height * 3);
	}

	boolean isRed()
	{
		for (Range range : ranges)
		{
			if (range.color == Color.RED)
			{
				if (value < range.to) return true;
			}
		}

		return false;
	}

	public void setGaugeSize(int size)
	{
		this.size = size;
		setPreferredSize(new Dimension(size, size));
	}
}
