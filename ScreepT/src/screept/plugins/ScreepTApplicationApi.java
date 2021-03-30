package screept.plugins;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JMenu;

import maingUI.MainGUI;
import maingUI.MainGuiUtils;
import plugin.api.AbstractPluginApplicationApi;
import plugin.api.PluginJMenuCreator;
import plugin.external.arch.IPlugin;
import plugin.external.arch.IPluginCrypter;
import plugin.external.arch.IPluginParentExecutor.FunctionType;
import plugin.external.arch.IPluginTextEditor;
import resources.GeneralConfig;
import various.common.light.files.FileVarious;
import various.common.light.gui.MenuSetter;
import various.common.light.utility.string.StringWorker;

public class ScreepTApplicationApi extends AbstractPluginApplicationApi {

	@SuppressWarnings("unused")
	private MainGUI maingui;
	private MainGuiUtils utils;

	public ScreepTApplicationApi() {
		this.maingui = MainGUI.mainAPP;
		this.utils = MainGUI.mainGuiUtils;
	}

	public static JMenu getPluginsMenu(FunctionType type, Object param, IPlugin plugin) {
		JMenu newOccurrence = new PluginJMenuCreator()
				.getPluginMenu(param, type, plugin, false);

		newOccurrence.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
		MenuSetter.setFont(newOccurrence, GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
		if(!StringWorker.isEmpty(plugin.getTooltip()))
			newOccurrence.setToolTipText(plugin.getTooltip());

		return newOccurrence;
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

	@Override
	public String[] getFilesToLoad(IPlugin plugin) {
		List<String> fileList = new ArrayList<>();

		if(utils.isTextEditorActive() && plugin instanceof IPluginTextEditor) {
			fileList.add(FileVarious.getCanonicalPathSafe(
					MainGUI.textEditorPanel.getCurrentTabEditor().loadedTabObj.loadedFile));
		} else if(utils.isCrypterActive() && plugin instanceof IPluginCrypter) {
			fileList = FileVarious.getStringListFromFiles(Arrays.asList(MainGUI.crypterPanel.panelVDragNDropPane.getTreeNodes()));
		}

		return fileList.toArray(new String[0]);
	}

}
