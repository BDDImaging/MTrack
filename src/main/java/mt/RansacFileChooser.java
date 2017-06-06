package mt;

import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import ij.ImageJ;
import ij.ImagePlus;
import ij.io.Opener;

import mt.Tracking;
import mt.listeners.InteractiveRANSAC;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import ransacBatch.BatchRANSAC;

public class RansacFileChooser extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5915579156379418824L;
	/**
	 * 
	 */
	
	JPanel panelCont = new JPanel();
	JPanel panelIntro = new JPanel();
	JFileChooser chooserA;
	boolean wasDone = false;
	boolean isFinished = false;
	JButton Track;
	String choosertitleA;
	boolean Batchmoderun = false;
	File[] AllMovies;

	public RansacFileChooser() {

		final JFrame frame = new JFrame("Welcome to the Ransac Part of MTV tracker");

		Track = new JButton("Choose file");

		panelCont.add(panelIntro, "1");
		/* Instantiation */
		final GridBagLayout layout = new GridBagLayout();
		final GridBagConstraints c = new GridBagConstraints();

		panelIntro.setLayout(layout);

		final Label LoadtrackText = new Label("Input the .txt file generated by the MTV tracker");

		LoadtrackText.setBackground(new Color(1, 0, 1));
		LoadtrackText.setForeground(new Color(255, 255, 255));
		final Checkbox Batchmode = new Checkbox("Run in Batch Mode", Batchmoderun);

		/* Location */

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1.5;
		++c.gridy;
		c.insets = new Insets(10, 10, 10, 0);
		panelIntro.add(Batchmode, c);

		++c.gridy;
		c.insets = new Insets(10, 10, 10, 0);
		panelIntro.add(LoadtrackText, c);

		++c.gridy;
		c.insets = new Insets(10, 10, 10, 0);
		panelIntro.add(Track, c);

		panelIntro.setVisible(true);
		Track.addActionListener(new OpenTrackListener(frame));
		Batchmode.addItemListener(new RansacRuninBatchListener(frame));
		frame.addWindowListener(new FrameListener(frame));
		frame.add(panelCont, BorderLayout.CENTER);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);

	}

	protected class RansacRuninBatchListener implements ItemListener {

		final Frame parent;

		public RansacRuninBatchListener(Frame parent) {

			this.parent = parent;

		}

		@Override
		public void itemStateChanged(ItemEvent e) {

			close(parent);

			panelIntro.removeAll();

			/* Instantiation */
			final GridBagLayout layout = new GridBagLayout();
			final GridBagConstraints c = new GridBagConstraints();

			panelIntro.setLayout(layout);

			final JFrame frame = new JFrame("Welcome to Ransac Rate Analyzer (Batch Mode)");
			Batchmoderun = true;

			JButton Done = new JButton("Exit");

			final Label LoadDirectoryText = new Label("Using Fiji Prefs we execute the program for all tif files");

			LoadDirectoryText.setBackground(new Color(1, 0, 1));
			LoadDirectoryText.setForeground(new Color(255, 255, 255));

			JButton Measurebatch = new JButton("Select directory of txt trajectory files and obtain RANSAC fits");

			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 0;
			c.gridy = 0;
			c.weightx = 1;
			c.weighty = 1.5;

			++c.gridy;
			c.insets = new Insets(10, 10, 10, 0);
			panelIntro.add(LoadDirectoryText, c);

			++c.gridy;
			c.insets = new Insets(10, 10, 10, 0);
			panelIntro.add(Measurebatch, c);
			++c.gridy;
			c.insets = new Insets(10, 10, 10, 0);
			panelIntro.add(Done, c);
			Measurebatch.addActionListener(new MeasurebatchListener(frame));
			Done.addActionListener(new DoneButtonListener(frame, true));
			panelIntro.validate();
			panelIntro.repaint();
			frame.addWindowListener(new FrameListener(frame));
			frame.add(panelCont, BorderLayout.CENTER);

			frame.pack();
			frame.setVisible(true);

		}

	}

	protected class MeasurebatchListener implements ActionListener {

		final Frame parent;

		public MeasurebatchListener(Frame parent) {

			this.parent = parent;

		}

		@Override
		public void actionPerformed(final ActionEvent arg0) {

			int result;

			chooserA = new JFileChooser();

			chooserA.setCurrentDirectory(new java.io.File("."));
			chooserA.setDialogTitle(choosertitleA);
			chooserA.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			//
			// disable the "All files" option.
			//
			chooserA.setAcceptAllFileFilterUsed(false);
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Rate Files", "txt");

			chooserA.setFileFilter(filter);
			chooserA.showOpenDialog(parent);

			AllMovies = chooserA.getSelectedFile().listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File pathname, String filename) {

					return (filename.endsWith(".txt") && !filename.contains("Rates") && !filename.contains("Average"));
				}
			});
			
			
			for (int i = 0; i < AllMovies.length ; ++i){
				
				
				new BatchRANSAC(Tracking.loadMT((AllMovies[i])),
						AllMovies[i]).run(null);
				
				
			}
			LengthDistribution.GetLengthDistribution(AllMovies);

		}

	}

	protected class FrameListener extends WindowAdapter {
		final Frame parent;

		public FrameListener(Frame parent) {
			super();
			this.parent = parent;
		}

		@Override
		public void windowClosing(WindowEvent e) {
			close(parent);
		}
	}

	protected final void close(final Frame parent) {
		if (parent != null)
			parent.dispose();

		isFinished = true;
	}

	protected class OpenTrackListener implements ActionListener {

		final Frame parent;

		public OpenTrackListener(Frame parent) {

			this.parent = parent;

		}

		@Override
		public void actionPerformed(final ActionEvent arg0) {

			int result;

			chooserA = new JFileChooser();
			chooserA.setCurrentDirectory(new java.io.File("."));
			chooserA.setDialogTitle(choosertitleA);
			chooserA.setFileSelectionMode(JFileChooser.FILES_ONLY);
			//
			// disable the "All files" option.
			//
			chooserA.setAcceptAllFileFilterUsed(false);
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Files", "txt");

			chooserA.setFileFilter(filter);
			//
			if (chooserA.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
				System.out.println("getCurrentDirectory(): " + chooserA.getCurrentDirectory());
				System.out.println("getSelectedFile() : " + chooserA.getSelectedFile());
			} else {
				System.out.println("No Selection ");
			}

			Done(parent);
		}

	}

	protected void Done(final Frame parent) {

		wasDone = true;

		if (!Batchmoderun)
			new InteractiveRANSAC(Tracking.loadMT(new File(chooserA.getSelectedFile().getPath())),
					chooserA.getSelectedFile()).run(null);
		close(parent);

	}

	protected class DoneButtonListener implements ActionListener {
		final Frame parent;
		final boolean Done;

		public DoneButtonListener(Frame parent, final boolean Done) {
			this.parent = parent;
			this.Done = Done;
		}

		@Override
		public void actionPerformed(final ActionEvent arg0) {
			wasDone = Done;

			if (!Batchmoderun)
				new InteractiveRANSAC(Tracking.loadMT(new File(chooserA.getSelectedFile().getPath())),
						chooserA.getSelectedFile()).run(null);
			close(parent);
		}
	}

	public Dimension getPreferredSize() {
		return new Dimension(800, 300);
	}



}
