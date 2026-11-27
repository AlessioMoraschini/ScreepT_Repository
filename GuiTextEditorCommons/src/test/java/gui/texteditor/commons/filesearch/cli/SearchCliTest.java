/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2026
 *
 *  AM-Design-Development - (Alessio Moraschini)
 * =========================================================================================
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package gui.texteditor.commons.filesearch.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import gui.texteditor.commons.filesearch.content.FileContentSearch;
import various.common.light.files.om.FileContentSearchDTO;
import various.common.light.files.om.SearchResult;

/** The command line search: options, output format, exit codes, same results as the engine used by the window */
public class SearchCliTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private File root;

	@Before
	public void corpus() throws Exception {
		root = tmp.newFolder("corpus");
		write("src/OrderService.java", "class OrderService {\n\tRepository repository;\n\tvoid save() { repository.save(order); }\n}\n");
		write("src/PaymentService.java", "class PaymentService {\n\t// Payment 123 failed: gateway timeout\n\tString order = \"ORDER\";\n}\n");
		write("docs/notes.txt", "nothing to see here\nreorder the list\n");
		write("logs/app.log", "2026-11-01 INFO ok\r\n2026-11-01 ERROR Payment 42 failed: gateway timeout\r\n");
		Files.write(new File(root, "logs/blob.bin").toPath(), new byte[] { 'o', 'r', 'd', 'e', 'r', 0, 0, 1 });
	}

	private void write(String rel, String text) throws Exception {
		File f = new File(root, rel);
		f.getParentFile().mkdirs();
		Files.write(f.toPath(), text.getBytes(StandardCharsets.UTF_8));
	}

	private static final class Run {
		int code;
		List<String> lines;
		String err;
	}

	private Run run(String... args) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();
		Run r = new Run();
		r.code = SearchCli.run(args, out, new PrintStream(err, true));
		String text = new String(out.toByteArray(), StandardCharsets.UTF_8);
		r.lines = new ArrayList<>();
		for (String line : text.split("\\r?\\n")) {
			if (!line.isEmpty())
				r.lines.add(line.replace(root.getPath() + File.separator, "").replace(File.separatorChar, '/'));
		}
		Collections.sort(r.lines);
		r.err = new String(err.toByteArray(), StandardCharsets.UTF_8);
		return r;
	}

	@Test
	public void plainTextIsCaseSensitiveWithPathLineColumnText() {
		Run r = run("gateway timeout", root.getPath());
		assertEquals(0, r.code);
		assertEquals(Arrays.asList(
				"logs/app.log:2:37:2026-11-01 ERROR Payment 42 failed: gateway timeout",
				"src/PaymentService.java:2:25:\t// Payment 123 failed: gateway timeout"), r.lines);
		assertEquals(1, run("Gateway Timeout", root.getPath()).code);
	}

	@Test
	public void ignoreCaseWholeWordAndRegex() {
		assertEquals(Arrays.asList("src/OrderService.java:3:32:\tvoid save() { repository.save(order); }",
				"src/PaymentService.java:3:9:\tString order = \"ORDER\";"), run("-i", "-w", "order", root.getPath()).lines);
		// "reorder" is not a whole word match, and the binary file is skipped
		assertEquals(Arrays.asList("logs/app.log:2:18:2026-11-01 ERROR Payment 42 failed: gateway timeout",
				"src/PaymentService.java:2:5:\t// Payment 123 failed: gateway timeout"), run("-e", "Payment \\d+ failed", root.getPath()).lines);
	}

	@Test
	public void countFilesOnlyGlobsAndSort() {
		assertEquals(Arrays.asList("src/OrderService.java:2"), run("-c", "repository", root.getPath()).lines);
		assertEquals(Arrays.asList("logs/app.log", "src/PaymentService.java"), run("-l", "timeout", root.getPath()).lines);
		assertEquals(Arrays.asList("logs/app.log"), run("-l", "-g", "*.log", "timeout", root.getPath()).lines);
		assertEquals(Arrays.asList("src/PaymentService.java"), run("-l", "-g", "!*.log", "timeout", root.getPath()).lines);
		Run sorted = run("--sort", "-l", "-i", "order", root.getPath());
		assertEquals(Arrays.asList("docs/notes.txt", "src/OrderService.java", "src/PaymentService.java"), sorted.lines);
	}

	@Test
	public void binaryFilesOnlyWhenAsked() {
		assertTrue(!run("-l", "order", root.getPath()).lines.contains("logs/blob.bin"));
		assertTrue(run("-l", "--binary", "order", root.getPath()).lines.contains("logs/blob.bin"));
	}

	@Test
	public void errorsGiveExitCodeTwo() {
		assertEquals(2, run().code);
		assertEquals(2, run("--nope", "x").code);
		assertEquals(2, run("x", new File(root, "missing").getPath()).code);
		Run bad = run("-e", "(unclosed", root.getPath());
		assertEquals(2, bad.code);
		assertTrue(bad.err, bad.err.contains("invalid regular expression"));
		assertEquals(0, run("--help").code);
	}

	@Test
	public void sameMatchingLinesAsTheEngineOfTheWindow() throws Exception {
		FileContentSearchDTO dto = new FileContentSearchDTO("order", false, false, false, false, new String[0]);
		List<String> expected = new ArrayList<>();
		FileContentSearch engine = new FileContentSearch(dto, Collections.singletonList(root), null);
		engine.setResultLimit(Long.MAX_VALUE);
		engine.searchStreaming(result -> {
			synchronized (expected) {
				for (Integer line : result.resultLines.keySet())
					expected.add(rel(result) + ":" + line);
			}
		}, 2);
		Collections.sort(expected);

		List<String> cli = new ArrayList<>();
		for (String line : run(SearchCli.argsFor(dto, Collections.singletonList(root), 2).toArray(new String[0])).lines) {
			String[] parts = line.split(":");
			cli.add(parts[0] + ":" + parts[1]);
		}
		Collections.sort(cli);
		assertEquals(expected, cli);
		assertEquals(4, cli.size());
	}

	private String rel(SearchResult result) {
		return result.ownerFile.getPath().replace(root.getPath() + File.separator, "").replace(File.separatorChar, '/');
	}

	@Test
	public void argsForReproducesTheWindowOptions() {
		FileContentSearchDTO dto = new FileContentSearchDTO("a b", true, true, true, true, new String[] { "*.java", "<>*test*" });
		List<String> args = SearchCli.argsFor(dto, Collections.singletonList(root), 4);
		assertEquals(Arrays.asList("-w", "-e", "--binary", "-j", "4", "--engine-filter", "*.java", "--engine-filter", "<>*test*", "--", "a b",
				root.getAbsolutePath()), args);
	}
}
