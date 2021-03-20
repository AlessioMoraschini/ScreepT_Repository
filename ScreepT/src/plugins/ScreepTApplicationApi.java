package plugins;

import java.io.File;
import java.util.List;

import maingUI.MainGUI;
import maingUI.MainGuiUtils;
import plugin.api.AbstractPluginApplicationApi;
import plugin.external.arch.IPlugin;
import resources.GeneralConfig;

public class ScreepTApplicationApi extends AbstractPluginApplicationApi {

	private MainGUI maingui;
	private MainGuiUtils utils;

	public ScreepTApplicationApi() {
		this.maingui = MainGUI.mainAPP;
		this.utils = MainGUI.mainGuiUtils;
	}

	@Override
	public String loadText(String text) {
		return null;
	}

	@Override
	public String loadFiles(List<File> files) {
		utils.openFilesInTextEditor(files.toArray(new File[0]), ()->{});
		return null;
	}

	@Override
	public String getPluginFolderPath(Class<? extends IPlugin> plugin) {
		return GeneralConfig.EXTERNAL_PLUGINS;
	}

}
