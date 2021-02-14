package frameutils.frame.panels;

import frameutils.frame.panels.arch.ParentPanel;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import net.miginfocom.swing.MigLayout;
import resources.IconsPathConfigurator;

public class PlayStopPausePanel extends ParentPanel {
	private static final long serialVersionUID = -3789652661940094712L;
	
	Runnable playAction = () -> {};
	Runnable stopAction = () -> {};
	Runnable pauseAction = () -> {};
	
	boolean isPlaying = false;
	boolean isPaused = false;
	boolean isStopped = false;
	
	private JButton pauseButton;
	private JButton stopButton;
	private JButton playButton;
	
	public PlayStopPausePanel() {
		this(() -> {},() -> {},() -> {});
	}
	
	public PlayStopPausePanel(Runnable playAction, Runnable stopAction, Runnable pauseAction) {
		this.playAction = playAction;
		this.stopAction = stopAction;
		this.pauseAction = pauseAction;
		
		isPlaying = false;  
		isPaused = false;   
		isStopped = true;  
	
		super.init();
	}
	
	@Override
	public void initGui() {
		setLayout(new MigLayout("fill, insets 1, hidemode 2", "[::33px,grow][::33px,grow][::33px,grow]", "[::9px,grow]"));
		
		playButton = new JButton("");
		playButton.setIcon(new ImageIcon(IconsPathConfigurator.ICON_PLAY));
		add(playButton, "cell 0 0,alignx center,aligny center");
		
		pauseButton = new JButton("");
		pauseButton.setIcon(new ImageIcon(IconsPathConfigurator.ICON_PAUSE));
		add(pauseButton, "cell 1 0,alignx center,aligny center");
		
		stopButton = new JButton("");
		stopButton.setIcon(new ImageIcon(IconsPathConfigurator.ICON_STOP));
		add(stopButton, "cell 2 0,alignx center,aligny center");
		
		initButtons();
	}
	
	public void initButtons() {
		super.styleButtons(null, null, true);
		setButtonsEnabled(true, false, false);
		setPlayStopPause(false, false, true);
	}
	
	@Override
	public void addHandlers() {
		playButton.addActionListener((e) -> {
			if(isStopped || isPaused) {
				setButtonsEnabled(false, true, true);
				setPlayStopPause(true, false, false);
				playAction.run();
			}
		});
		
		pauseButton.addActionListener((e) -> {
			if(isPlaying) {
				setButtonsEnabled(true, false, true);
				setPlayStopPause(false, true, false);
				pauseAction.run();
			}
		});
		
		stopButton.addActionListener((e) -> {
			if(isPlaying) {
				setButtonsEnabled(true, false, false);
				setPlayStopPause(false, false, true);
				stopAction.run();
			}
		});
	}

	public void setPlayStopPause(boolean playing, boolean paused, boolean stopped) {
		isPaused = paused;
		isPlaying = playing;
		isStopped = stopped;
	}
	
	public void setButtonsEnabled(boolean playEnabled, boolean pauseEnabled, boolean stopEnabled) {
		playButton.setEnabled(playEnabled);
		stopButton.setEnabled(pauseEnabled);
		pauseButton.setEnabled(stopEnabled);
	}
	
	public void setPauseVisible(boolean pauseVisible) {
		pauseButton.setVisible(pauseVisible);
	}

	public boolean isPlaying() {
		return isPlaying;
	}

	public boolean isPaused() {
		return isPaused;
	}

	public boolean isStopped() {
		return isStopped;
	}

	public JButton getPauseButton() {
		return pauseButton;
	}

	public JButton getStopButton() {
		return stopButton;
	}

	public JButton getPlayButton() {
		return playButton;
	}
	
}
