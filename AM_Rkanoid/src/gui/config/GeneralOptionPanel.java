package gui.config;

import utils.config.GeneralConfig;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Font;
import java.awt.Color;
import java.awt.SystemColor;
import javax.swing.JSeparator;
import javax.swing.JSlider;

import om.Difficulty;

import javax.swing.JComboBox;

public class GeneralOptionPanel extends AbstractOptionPanel {
	private static final long serialVersionUID = -8750945005935379183L;
	
	public static volatile int instances = 0;

	private JLabel lblDifficulty;
	private JLabel lblEffectsVolume;
	private JLabel lblMusicVolume;
	private JSlider sliderBaseVolume;
	private JSlider sliderEffectsVolume;
	private JComboBox<Difficulty> comboDifficulty;

	public GeneralOptionPanel(GeneralConfig configuration) {
		super(configuration);
		
		instances ++;
		
		setForeground(Color.LIGHT_GRAY);
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 120, 0, 0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{40, 0, 0, 30, 30, 30, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, 1.0, 1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblGeneralOptions = new JLabel("General Options");
		lblGeneralOptions.setForeground(SystemColor.windowText);
		lblGeneralOptions.setFont(new Font("Vrinda", Font.BOLD, 20));
		GridBagConstraints gbc_lblGeneralOptions = new GridBagConstraints();
		gbc_lblGeneralOptions.anchor = GridBagConstraints.SOUTH;
		gbc_lblGeneralOptions.gridwidth = 4;
		gbc_lblGeneralOptions.insets = new Insets(0, 0, 5, 5);
		gbc_lblGeneralOptions.gridx = 1;
		gbc_lblGeneralOptions.gridy = 0;
		add(lblGeneralOptions, gbc_lblGeneralOptions);
		
		JSeparator separator = new JSeparator();
		separator.setBackground(SystemColor.activeCaptionBorder);
		separator.setForeground(SystemColor.activeCaptionBorder);
		GridBagConstraints gbc_separator = new GridBagConstraints();
		gbc_separator.fill = GridBagConstraints.HORIZONTAL;
		gbc_separator.gridwidth = 4;
		gbc_separator.insets = new Insets(0, 0, 5, 5);
		gbc_separator.gridx = 1;
		gbc_separator.gridy = 1;
		add(separator, gbc_separator);
		
		lblMusicVolume = new JLabel("Music Volume");
		lblMusicVolume.setForeground(SystemColor.controlDkShadow);
		GridBagConstraints gbc_lblMusicVolume = new GridBagConstraints();
		gbc_lblMusicVolume.insets = new Insets(0, 0, 5, 5);
		gbc_lblMusicVolume.gridx = 1;
		gbc_lblMusicVolume.gridy = 3;
		add(lblMusicVolume, gbc_lblMusicVolume);
		
		sliderBaseVolume = new JSlider(1, 100);
		GridBagConstraints gbc_sliderBaseVolume = new GridBagConstraints();
		gbc_sliderBaseVolume.gridwidth = 3;
		gbc_sliderBaseVolume.fill = GridBagConstraints.HORIZONTAL;
		gbc_sliderBaseVolume.insets = new Insets(0, 0, 5, 5);
		gbc_sliderBaseVolume.gridx = 2;
		gbc_sliderBaseVolume.gridy = 3;
		add(sliderBaseVolume, gbc_sliderBaseVolume);
		
		lblEffectsVolume = new JLabel("Effects Volume");
		lblEffectsVolume.setForeground(SystemColor.controlDkShadow);
		GridBagConstraints gbc_lblEffectsVolume = new GridBagConstraints();
		gbc_lblEffectsVolume.insets = new Insets(0, 0, 5, 5);
		gbc_lblEffectsVolume.gridx = 1;
		gbc_lblEffectsVolume.gridy = 4;
		add(lblEffectsVolume, gbc_lblEffectsVolume);
		
		sliderEffectsVolume = new JSlider();
		GridBagConstraints gbc_sliderEffectsVolume = new GridBagConstraints();
		gbc_sliderEffectsVolume.gridwidth = 3;
		gbc_sliderEffectsVolume.fill = GridBagConstraints.HORIZONTAL;
		gbc_sliderEffectsVolume.insets = new Insets(0, 0, 5, 5);
		gbc_sliderEffectsVolume.gridx = 2;
		gbc_sliderEffectsVolume.gridy = 4;
		add(sliderEffectsVolume, gbc_sliderEffectsVolume);
		
		lblDifficulty = new JLabel("Difficulty");
		lblDifficulty.setForeground(SystemColor.controlDkShadow);
		GridBagConstraints gbc_lblDifficulty = new GridBagConstraints();
		gbc_lblDifficulty.insets = new Insets(0, 0, 5, 5);
		gbc_lblDifficulty.gridx = 1;
		gbc_lblDifficulty.gridy = 5;
		add(lblDifficulty, gbc_lblDifficulty);
		
		comboDifficulty = new JComboBox<Difficulty>(Difficulty.values());
		GridBagConstraints gbc_comboDifficulty = new GridBagConstraints();
		gbc_comboDifficulty.gridwidth = 3;
		gbc_comboDifficulty.insets = new Insets(0, 0, 5, 5);
		gbc_comboDifficulty.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboDifficulty.gridx = 2;
		gbc_comboDifficulty.gridy = 5;
		add(comboDifficulty, gbc_comboDifficulty);
	}

	public GeneralConfig getConfiguration() {
		return (GeneralConfig) config;
	}

	public void setConfiguration(GeneralConfig configuration) {
		this.config = configuration;
	}
	
	@Override
	public void reload() {
		super.reload();
		initializeFromConfig();
	}

	@Override
	public boolean save() {
		config.setProperty(GeneralConfig.KEY_BASE_VOLUME, intToFloatPercentage(sliderBaseVolume.getValue(), 100), true);
		config.setProperty(GeneralConfig.KEY_EFFECTS_VOLUME, intToFloatPercentage(sliderEffectsVolume.getValue(), 100), true);
		config.setProperty(GeneralConfig.KEY_DIFFICULTY, ((Difficulty)comboDifficulty.getSelectedItem()).name(), true);
		
		super.showOutcomePopUp(true);
		
		return true;
	}

	@Override
	public void discard() {
		reload();
		super.discard();
		instances --;
	}

	@Override
	public void initializeFromConfig() {
		if(config == null) {
			System.err.println("Configuration is NULL");
			return;
		}
		
		sliderBaseVolume.setValue(floatToIntPercentage(config.loadFloat(GeneralConfig.KEY_BASE_VOLUME), 100));
		sliderEffectsVolume.setValue(floatToIntPercentage(config.loadFloat(GeneralConfig.KEY_EFFECTS_VOLUME), 100));
		comboDifficulty.setSelectedItem(Difficulty.valueOf(config.loadString(GeneralConfig.KEY_DIFFICULTY)));
	}
	
	public float intToFloatPercentage(int value, int rangeMax) {
		return ((float)value / (float) rangeMax);
	}

	public int floatToIntPercentage(float value, int rangeMax) {
		return (int) (value * rangeMax);
	}

}
