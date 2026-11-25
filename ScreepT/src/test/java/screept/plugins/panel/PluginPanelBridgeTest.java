package screept.plugins.panel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ini4j.Wini;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import gui.commons.tabs.TabImplementors;
import initializer.configs.impl.INItializer;
import plugin.external.arch.IPluginPanelMode;
import plugin.external.arch.PluginPanel;
import plugin.external.arch.PluginAbstractParent;
import screept.gui.GuiTestSupport;
import screept.gui.GuiTestSupport.Fixture;

/** A plugin downloaded after the feature becomes the viewer of its extensions; the ones already there are only recorded */
public class PluginPanelBridgeTest {

	@ClassRule
	public static TemporaryFolder classTmp = new TemporaryFolder();

	private static Fixture fx;
	private Map<String, String> savedOverrides;

	@BeforeClass
	public static void open() throws Exception {
		Assume.assumeFalse(java.awt.GraphicsEnvironment.isHeadless());
		fx = GuiTestSupport.open(classTmp.newFolder("bridge"));
	}

	@AfterClass
	public static void close() throws Exception {
		if (fx != null) {
			fx.close();
		}
	}

	@Before
	public void reset() {
		savedOverrides = TabImplementors.getOverrides();
		TabImplementors.setOverrides(new LinkedHashMap<String, String>());
		fx.conf.getTextEditorOpt().pluginViewersSeen = "";
		fx.conf.getTextEditorOpt().viewerAssociations = "";
		PluginPanelBridge.install(fx.conf);
	}

	@After
	public void restore() {
		TabImplementors.setOverrides(savedOverrides);
	}

	/** A plugin with panel mode (the id of a viewer comes from the simple name of its class) */
	public static class FakeViewer extends PluginAbstractParent implements IPluginPanelMode {
		private final String[] extensions;

		FakeViewer(String... extensions) {
			this.extensions = extensions;
		}

		@Override
		public String getVersion() {
			return "1.0.0";
		}

		@Override
		public String[] getPanelExtensions() {
			return extensions;
		}

		@Override
		public PluginPanel createPanel(File file) {
			return null;
		}
	}

	public static class Pdfviewer extends FakeViewer {
		public Pdfviewer() {
			super("pdf");
		}
	}

	public static class Markdownviewer extends FakeViewer {
		public Markdownviewer() {
			super("MD", "markdown", " ");
		}
	}

	private static List<IPluginPanelMode> list(IPluginPanelMode... viewers) {
		return new ArrayList<IPluginPanelMode>(Arrays.asList(viewers));
	}

	private static String ini(String key) throws Exception {
		return new Wini(fx.iniFile).get(INItializer.TEXT_EDITOR_OPT, key);
	}

	@Test
	public void firstRunOnlyRecordsTheViewersAlreadyThere() throws Exception {
		Pdfviewer pdf = new Pdfviewer();
		Markdownviewer markdown = new Markdownviewer();
		PluginPanelBridge.associateNewViewers(list(pdf, markdown));
		assertTrue("the already installed viewers are not made default", TabImplementors.getOverrides().isEmpty());
		assertTrue(ini("pluginViewersSeen").contains("plugin:pdfviewer|") && ini("pluginViewersSeen").contains("plugin:markdownviewer|"));
		PluginPanelBridge.associateNewViewers(list(pdf, markdown));
		assertTrue(TabImplementors.getOverrides().isEmpty());
	}

	@Test
	public void aPluginThatAppearsLaterBecomesTheViewerOfItsExtensions() throws Exception {
		Pdfviewer pdf = new Pdfviewer();
		PluginPanelBridge.associateNewViewers(list(pdf));
		Markdownviewer markdown = new Markdownviewer();
		PluginPanelBridge.associateNewViewers(list(pdf, markdown));

		assertEquals("plugin:markdownviewer", TabImplementors.getOverrides().get("md"));
		assertEquals("plugin:markdownviewer", TabImplementors.getOverrides().get("markdown"));
		assertFalse("the other viewer is untouched", TabImplementors.getOverrides().containsKey("pdf"));
		assertEquals("plugin:markdownviewer", TabImplementors.effectiveIdFor("md"));
		// and it is saved, to be loaded at the next start
		String saved = ini("viewerAssociations");
		assertTrue(saved, saved.contains("md=plugin:markdownviewer") && saved.contains("markdown=plugin:markdownviewer"));
		assertEquals(saved, fx.conf.getTextEditorOpt().viewerAssociations);

		// the user takes the extension back: it stays so (the plugin is not new any more)
		TabImplementors.associate("md", TabImplementors.TEXT_ID);
		PluginPanelBridge.associateNewViewers(list(pdf, markdown));
		assertFalse(TabImplementors.getOverrides().containsKey("md"));
	}

	@Test
	public void anExtensionTheUserAlreadyChoseIsLeftAlone() throws Exception {
		PluginPanelBridge.associateNewViewers(list(new Pdfviewer()));
		TabImplementors.associate("md", "plugin:somethingelse");
		PluginPanelBridge.associateNewViewers(list(new Pdfviewer(), new Markdownviewer()));
		assertEquals("plugin:somethingelse", TabImplementors.getOverrides().get("md"));
		assertEquals("plugin:markdownviewer", TabImplementors.getOverrides().get("markdown"));
	}

	@Test
	public void noViewersFoundYetStartsNothing() throws Exception {
		PluginPanelBridge.associateNewViewers(list());
		assertEquals("", fx.conf.getTextEditorOpt().pluginViewersSeen);
		assertTrue(TabImplementors.getOverrides().isEmpty());
	}
}
