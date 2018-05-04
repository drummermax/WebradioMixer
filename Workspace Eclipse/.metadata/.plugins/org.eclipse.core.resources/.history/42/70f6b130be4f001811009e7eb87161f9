package main;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiMessage;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import audio.MixingDesk;
import midi.MidiController;

public class GUI extends JFrame implements MouseListener {

	private static final long serialVersionUID = 1L;

	private JPanel panel;

	private JButton button_updateLines, button_mute, button_quit;

	private JComboBox<String> comboBox_lineInMicrophone1, comboBox_lineInMicrophone2, comboBox_lineInMairlistChannel1,
			comboBox_lineInMairlistChannel2, comboBox_lineInMairlistPFL, comboBox_lineInMairlistCartwall,
			comboBox_lineInStdOut, comboBox_lineInMairlistMasterRecord, comboBox_lineOutMairlistMaster,
			comboBox_lineOutMonitor, comboBox_lineOutPhones;

	private JTextField textfield_samplerate, textfield_bits, textfield_faderSampleFrequency;

	private JLabel label_lineInMicrophone1, label_lineInMicrophone2, label_lineInMairlistChannel1,
			label_lineInMairlistChannel2, label_lineInMairlistPFL, label_lineInMairlistCartwall, label_lineInStdOut,
			label_lineInMairlistMasterRecord, label_lineOutMairlistMaster, label_lineOutMonitor, label_lineOutPhones;

	public GUI() {
		this.setSize(800, 360);

		panel = new JPanel();
		panel.setLayout(null);
		panel.setBackground(Color.WHITE);
		panel.setBounds(0, 20, 800, 480);
		add(panel);

		button_quit = new JButton("Quit");
		button_quit.setVerticalTextPosition(AbstractButton.CENTER);
		button_quit.setHorizontalTextPosition(AbstractButton.LEADING);
		button_quit.setMnemonic(KeyEvent.VK_Q);
		button_quit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Threads beenden
				close();
			}
		});
		button_quit.setBounds(550, 125, 200, 40);
		panel.add(button_quit);

		button_updateLines = new JButton("Update Lines");
		button_updateLines.setVerticalTextPosition(AbstractButton.CENTER);
		button_updateLines.setHorizontalTextPosition(AbstractButton.LEADING);
		button_updateLines.setMnemonic(KeyEvent.VK_U);
		button_updateLines.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] newConfig = {
						Integer.parseInt(comboBox_lineInMicrophone1.getSelectedItem().toString().split("\\:")[0]),
						Integer.parseInt(comboBox_lineInMicrophone2.getSelectedItem().toString().split("\\:")[0]),
						Integer.parseInt(comboBox_lineInMairlistChannel1.getSelectedItem().toString().split("\\:")[0]),
						Integer.parseInt(comboBox_lineInMairlistChannel2.getSelectedItem().toString().split("\\:")[0]),
						Integer.parseInt(comboBox_lineInMairlistPFL.getSelectedItem().toString().split("\\:")[0]),
						Integer.parseInt(comboBox_lineInMairlistCartwall.getSelectedItem().toString().split("\\:")[0]),
						Integer.parseInt(comboBox_lineInStdOut.getSelectedItem().toString().split("\\:")[0]),
						Integer.parseInt(comboBox_lineInMairlistMasterRecord.getSelectedItem().toString().split("\\:")[0]),
						Integer.parseInt(comboBox_lineOutMairlistMaster.getSelectedItem().toString().split("\\:")[0]),
						Integer.parseInt(comboBox_lineOutMonitor.getSelectedItem().toString().split("\\:")[0]),
						Integer.parseInt(comboBox_lineOutPhones.getSelectedItem().toString().split("\\:")[0]) };
				Filemanager.getInstance().setConfig(newConfig);

				MixingDesk.getInstance().updateLines();
			}
		});
		button_updateLines.setBounds(550, 175, 200, 40);
		panel.add(button_updateLines);

		button_mute = new JButton("Mute");
		button_mute.setVerticalTextPosition(AbstractButton.CENTER);
		button_mute.setHorizontalTextPosition(AbstractButton.LEADING);
		button_mute.setMnemonic(KeyEvent.VK_M);
		button_mute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MixingDesk.getInstance().getMairlistMaster().setVolume(0);
				MixingDesk.getInstance().getMonitor().setVolume(0);
				MixingDesk.getInstance().getPhone().setVolume(0);
			}
		});
		button_mute.setBounds(550, 225, 200, 40);
		panel.add(button_mute);

		textfield_samplerate = new JTextField("Samplerate: " + MixingDesk.getInstance().getSamplerate());
		textfield_samplerate.setForeground(Color.BLACK);
		textfield_samplerate.setBackground(Color.WHITE);
		textfield_samplerate.setBounds(550, 25, 150, 25);
		textfield_samplerate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					MixingDesk.getInstance().setSamplerate(Integer.parseInt(textfield_samplerate.getText()));
				} catch (NumberFormatException eNumberFirmatException) {
				}
				textfield_samplerate.setText("Samplerate: " + MixingDesk.getInstance().getSamplerate());
			}
		});
		textfield_samplerate.addMouseListener(this);
		panel.add(textfield_samplerate);

		textfield_bits = new JTextField("Bits: " + MixingDesk.getInstance().getNumberOfBits());
		textfield_bits.setForeground(Color.BLACK);
		textfield_bits.setBackground(Color.WHITE);
		textfield_bits.setBounds(550, 50, 200, 25);
		textfield_bits.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					MixingDesk.getInstance().setNumberOfBits(Integer.parseInt(textfield_bits.getText()));
				} catch (NumberFormatException eNumberFirmatException) {
				}
				textfield_bits.setText("Bits: " + MixingDesk.getInstance().getNumberOfBits());
			}
		});
		textfield_bits.addMouseListener(this);
		panel.add(textfield_bits);

		textfield_faderSampleFrequency = new JTextField("Freq. Faderabtastung: " + MidiController.getInstance().getFaderSampleFrequency());
		textfield_faderSampleFrequency.setForeground(Color.BLACK);
		textfield_faderSampleFrequency.setBackground(Color.WHITE);
		textfield_faderSampleFrequency.setBounds(550, 75, 200, 25);
		textfield_faderSampleFrequency.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					MidiController.getInstance().setFaderSampleFrequency(Integer.parseInt(textfield_faderSampleFrequency.getText()));
				} catch (NumberFormatException eNumberFirmatException) {
				}
				textfield_faderSampleFrequency.setText("Freq. Faderabtastung: " + MidiController.getInstance().getFaderSampleFrequency());
			}
		});
		textfield_faderSampleFrequency.addMouseListener(this);
		panel.add(textfield_faderSampleFrequency);

		comboBox_lineInMicrophone1 = new JComboBox<String>(getInputLinesStringArray());
		comboBox_lineInMicrophone1.setSelectedIndex(
				getComboBoxIndexByLineNumberInput((int) Filemanager.getInstance().variables.get("microphone 1")));
		comboBox_lineInMicrophone1.setBounds(200, 0, 300, 25);
		comboBox_lineInMicrophone1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					MixingDesk.getInstance().getMicrophone1().setMixerInfosIndex(
							Integer.parseInt(comboBox_lineInMicrophone1.getSelectedItem().toString().split("\\:")[0]));
				} catch (NumberFormatException eNumberFirmatException) {
				}

			}
		});
		panel.add(comboBox_lineInMicrophone1);

		comboBox_lineInMicrophone2 = new JComboBox<String>(getInputLinesStringArray());
		comboBox_lineInMicrophone2.setSelectedIndex(
				getComboBoxIndexByLineNumberInput((int) Filemanager.getInstance().variables.get("microphone 2")));
		comboBox_lineInMicrophone2.setBounds(200, 25, 300, 25);
		comboBox_lineInMicrophone2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					MixingDesk.getInstance().getMicrophone2().setMixerInfosIndex(
							Integer.parseInt(comboBox_lineInMicrophone2.getSelectedItem().toString().split("\\:")[0]));
				} catch (NumberFormatException eNumberFirmatException) {
				}

			}
		});
		panel.add(comboBox_lineInMicrophone2);

		comboBox_lineInMairlistChannel1 = new JComboBox<String>(getInputLinesStringArray());
		comboBox_lineInMairlistChannel1.setSelectedIndex(
				getComboBoxIndexByLineNumberInput((int) Filemanager.getInstance().variables.get("mairlist channel 1")));
		comboBox_lineInMairlistChannel1.setBounds(200, 50, 300, 25);
		comboBox_lineInMairlistChannel1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					MixingDesk.getInstance().getMairlistChannel1().setMixerInfosIndex(Integer
							.parseInt(comboBox_lineInMairlistChannel1.getSelectedItem().toString().split("\\:")[0]));
				} catch (NumberFormatException eNumberFirmatException) {
				}

			}
		});
		panel.add(comboBox_lineInMairlistChannel1);

		comboBox_lineInMairlistChannel2 = new JComboBox<String>(getInputLinesStringArray());
		comboBox_lineInMairlistChannel2.setSelectedIndex(
				getComboBoxIndexByLineNumberInput((int) Filemanager.getInstance().variables.get("mairlist channel 2")));
		comboBox_lineInMairlistChannel2.setBounds(200, 75, 300, 25);
		comboBox_lineInMairlistChannel2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					MixingDesk.getInstance().getMairlistChannel2().setMixerInfosIndex(Integer
							.parseInt(comboBox_lineInMairlistChannel2.getSelectedItem().toString().split("\\:")[0]));
				} catch (NumberFormatException eNumberFirmatException) {
				}

			}
		});
		panel.add(comboBox_lineInMairlistChannel2);

		comboBox_lineInMairlistPFL = new JComboBox<String>(getInputLinesStringArray());
		comboBox_lineInMairlistPFL.setSelectedIndex(
				getComboBoxIndexByLineNumberInput((int) Filemanager.getInstance().variables.get("mairlist pfl")));
		comboBox_lineInMairlistPFL.setBounds(200, 100, 300, 25);
		comboBox_lineInMairlistPFL.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					MixingDesk.getInstance().getMairlistPFL().setMixerInfosIndex(
							Integer.parseInt(comboBox_lineInMairlistPFL.getSelectedItem().toString().split("\\:")[0]));
				} catch (NumberFormatException eNumberFirmatException) {
				}

			}
		});
		panel.add(comboBox_lineInMairlistPFL);

		comboBox_lineInMairlistCartwall = new JComboBox<String>(getInputLinesStringArray());
		comboBox_lineInMairlistCartwall.setSelectedIndex(
				getComboBoxIndexByLineNumberInput((int) Filemanager.getInstance().variables.get("mairlist cartwall")));
		comboBox_lineInMairlistCartwall.setBounds(200, 125, 300, 25);
		comboBox_lineInMairlistCartwall.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					MixingDesk.getInstance().getMairlistCartwall().setMixerInfosIndex(Integer
							.parseInt(comboBox_lineInMairlistCartwall.getSelectedItem().toString().split("\\:")[0]));
				} catch (NumberFormatException eNumberFirmatException) {
				}

			}
		});
		panel.add(comboBox_lineInMairlistCartwall);

		comboBox_lineInStdOut = new JComboBox<String>(getInputLinesStringArray());
		comboBox_lineInStdOut.setSelectedIndex(
				getComboBoxIndexByLineNumberInput((int) Filemanager.getInstance().variables.get("stdout")));
		comboBox_lineInStdOut.setBounds(200, 150, 300, 25);
		comboBox_lineInStdOut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					MixingDesk.getInstance().getStdOut().setMixerInfosIndex(
							Integer.parseInt(comboBox_lineInStdOut.getSelectedItem().toString().split("\\:")[0]));
				} catch (NumberFormatException eNumberFirmatException) {
				}

			}
		});
		panel.add(comboBox_lineInStdOut);

		comboBox_lineInMairlistMasterRecord = new JComboBox<String>(getInputLinesStringArray());
		comboBox_lineInMairlistMasterRecord.setSelectedIndex(
				getComboBoxIndexByLineNumberInput((int) Filemanager.getInstance().variables.get("mairlist master record")));
		comboBox_lineInMairlistMasterRecord.setBounds(200, 175, 300, 25);
		comboBox_lineInMairlistMasterRecord.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					MixingDesk.getInstance().getStdOut().setMixerInfosIndex(
							Integer.parseInt(comboBox_lineInMairlistMasterRecord.getSelectedItem().toString().split("\\:")[0]));
				} catch (NumberFormatException eNumberFirmatException) {
				}

			}
		});
		panel.add(comboBox_lineInMairlistMasterRecord);

		comboBox_lineOutMairlistMaster = new JComboBox<String>(getOutputLinesStringArray());
		comboBox_lineOutMairlistMaster.setSelectedIndex(
				getComboBoxIndexByLineNumberOutput((int) Filemanager.getInstance().variables.get("mairlist master")));
		comboBox_lineOutMairlistMaster.setBounds(200, 225, 300, 25);
		comboBox_lineOutMairlistMaster.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					MixingDesk.getInstance().getMairlistMaster().setMixerInfosIndex(Integer
							.parseInt(comboBox_lineOutMairlistMaster.getSelectedItem().toString().split("\\:")[0]));
				} catch (NumberFormatException eNumberFirmatException) {
				}

			}
		});
		panel.add(comboBox_lineOutMairlistMaster);

		comboBox_lineOutMonitor = new JComboBox<String>(getOutputLinesStringArray());
		comboBox_lineOutMonitor.setSelectedIndex(
				getComboBoxIndexByLineNumberOutput((int) Filemanager.getInstance().variables.get("monitor")));
		comboBox_lineOutMonitor.setBounds(200, 250, 300, 25);
		comboBox_lineOutMonitor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					MixingDesk.getInstance().getMonitor().setMixerInfosIndex(
							Integer.parseInt(comboBox_lineOutMonitor.getSelectedItem().toString().split("\\:")[0]));
				} catch (NumberFormatException eNumberFirmatException) {
				}

			}
		});
		panel.add(comboBox_lineOutMonitor);

		comboBox_lineOutPhones = new JComboBox<String>(getOutputLinesStringArray());
		comboBox_lineOutPhones.setSelectedIndex(
				getComboBoxIndexByLineNumberOutput((int) Filemanager.getInstance().variables.get("phones")));
		comboBox_lineOutPhones.setBounds(200, 275, 300, 25);
		comboBox_lineOutPhones.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					MixingDesk.getInstance().getPhone().setMixerInfosIndex(
							Integer.parseInt(comboBox_lineOutPhones.getSelectedItem().toString().split("\\:")[0]));
				} catch (NumberFormatException eNumberFirmatException) {
				}

			}
		});
		panel.add(comboBox_lineOutPhones);

		label_lineInMicrophone1 = new JLabel("Microphone 1");
		label_lineInMicrophone1.setBounds(25, 0, 150, 25);
		panel.add(label_lineInMicrophone1);

		label_lineInMicrophone2 = new JLabel("Microphone 2");
		label_lineInMicrophone2.setBounds(25, 25, 150, 25);
		panel.add(label_lineInMicrophone2);

		label_lineInMairlistChannel1 = new JLabel("mAirList Channel 1");
		label_lineInMairlistChannel1.setBounds(25, 50, 150, 25);
		panel.add(label_lineInMairlistChannel1);

		label_lineInMairlistChannel2 = new JLabel("mAirList Channel 2");
		label_lineInMairlistChannel2.setBounds(25, 75, 150, 25);
		panel.add(label_lineInMairlistChannel2);

		label_lineInMairlistPFL = new JLabel("mAirList PFL");
		label_lineInMairlistPFL.setBounds(25, 100, 150, 25);
		panel.add(label_lineInMairlistPFL);

		label_lineInMairlistCartwall = new JLabel("mAirList Cartwall");
		label_lineInMairlistCartwall.setBounds(25, 125, 150, 25);
		panel.add(label_lineInMairlistCartwall);

		label_lineInStdOut = new JLabel("Standardwiedergabegerät");
		label_lineInStdOut.setBounds(25, 150, 150, 25);
		panel.add(label_lineInStdOut);

		label_lineInMairlistMasterRecord = new JLabel("mAirList Master Record");
		label_lineInMairlistMasterRecord.setBounds(25, 175, 150, 25);
		panel.add(label_lineInMairlistMasterRecord);

		label_lineOutMairlistMaster = new JLabel("mAirList Master Out");
		label_lineOutMairlistMaster.setBounds(25, 225, 150, 25);
		panel.add(label_lineOutMairlistMaster);

		label_lineOutMonitor = new JLabel("Monitor out");
		label_lineOutMonitor.setBounds(25, 250, 150, 25);
		panel.add(label_lineOutMonitor);

		label_lineOutPhones = new JLabel("Phones out");
		label_lineOutPhones.setBounds(25, 275, 150, 25);
		panel.add(label_lineOutPhones);

		// this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.setTitle("WebradioMixer");
		this.setResizable(false);
		this.setLayout(null);
		this.getContentPane().setBackground(Color.WHITE);
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.setVisible(true);
	}

	public void close() {
		System.exit(0);
		this.dispose();
	}

	public String[] getInputLinesStringArray() {
		Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();

		String[] mixerInfosString = new String[mixerInfos.length];

		int numberOfInputLines = 0;

		for (int i = 0; i < mixerInfos.length; i++) {
			mixerInfosString[i] = i + ": " + mixerInfos[i].getName() + " " + mixerInfos[i].getDescription();

			// System.out.println(mixerInfosString[i]);

			if (mixerInfosString[i].contains("Capture") && !mixerInfosString[i].contains("Primärer")) {
				numberOfInputLines++;
			}
		}

		String[] linesInput = new String[numberOfInputLines];

		int indexInputLine = 0;

		for (int i = 0; i < mixerInfos.length; i++) {
			if (mixerInfosString[i].contains("Capture") && !mixerInfosString[i].contains("Primärer")) {
				linesInput[indexInputLine] = mixerInfosString[i].split("\\(")[0];
				indexInputLine++;
			}
		}

		return linesInput;
	}

	public String[] getOutputLinesStringArray() {
		Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();

		String[] mixerInfosString = new String[mixerInfos.length];

		int numberOfOutputLines = 0;

		for (int i = 0; i < mixerInfos.length; i++) {
			mixerInfosString[i] = i + ": " + mixerInfos[i].getName() + " " + mixerInfos[i].getDescription();

			if (mixerInfosString[i].contains("Playback") && !mixerInfosString[i].contains("Primärer")) {
				numberOfOutputLines++;
			}
		}

		String[] linesOutput = new String[numberOfOutputLines];

		int indexOutputLine = 0;

		for (int i = 0; i < mixerInfos.length; i++) {
			if (mixerInfosString[i].contains("Playback") && !mixerInfosString[i].contains("Primärer")) {
				linesOutput[indexOutputLine] = mixerInfosString[i].split("\\(")[0];
				indexOutputLine++;
			}
		}

		return linesOutput;
	}

	private int getComboBoxIndexByLineNumberInput(int lineNumber) {
		String[] linesInput = getInputLinesStringArray();

		for (int i = 0; i < linesInput.length; i++) {
			if (Integer.parseInt(linesInput[i].split("\\:")[0]) == lineNumber) {
				return i;
			}
		}

		return 0;
	}

	private int getComboBoxIndexByLineNumberOutput(int lineNumber) {
		String[] linesOutput = getOutputLinesStringArray();

		for (int i = 0; i < linesOutput.length; i++) {
			if (Integer.parseInt(linesOutput[i].split("\\:")[0]) == lineNumber) {
				return i;
			}
		}

		return 0;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == textfield_samplerate) {
			textfield_samplerate.setText("");
		} else if (e.getSource() == textfield_bits) {
			textfield_bits.setText("");
		} else if (e.getSource() == textfield_faderSampleFrequency) {
			textfield_faderSampleFrequency.setText("");
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

}
