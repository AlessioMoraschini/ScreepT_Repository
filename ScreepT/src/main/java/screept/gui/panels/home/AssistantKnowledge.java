/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2026
 *
 *  AM-Design-Development - (Alessio Moraschini) - All Rights Reserved
 * =========================================================================================
 *
 * You should have received a copy of the license with this file.
 * If not, please write to: info@am-design-development.com, or visit : https://www.am-design-development.com
 */
package screept.gui.panels.home;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import resources.GeneralConfig;

/**
 * Offline knowledge of the home assistant: it indexes the documents shipped with the application (the HTML guide and
 * the CHANGELOG), so the answers are always the ones written in the documentation. Nothing is stored apart from the
 * in-memory index, which is rebuilt when one of the files changes.
 * Retrieval is a plain TF-IDF over sections of the documents, with a small Italian to English vocabulary.
 */
public final class AssistantKnowledge {

	private static final AssistantKnowledge INSTANCE = new AssistantKnowledge();

	private static final double MIN_SCORE = 1.1;
	private static final int MAX_LINE_CHARS = 280;
	private static final int PREFIX_LENGTH = 5;

	private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
			// english
			"the", "an", "is", "are", "to", "how", "do", "does", "can", "what", "where", "which", "of", "in", "on", "for",
			"and", "or", "with", "it", "this", "that", "my", "me", "you", "be", "i", "if", "at", "by", "from", "was", "there",
			"want", "need", "please", "tell", "about", "use", "using", "get", "make",
			// italian
			"come", "cosa", "dove", "quale", "quali", "posso", "si", "fa", "fare", "per", "un", "una", "il", "lo", "la", "le",
			"gli", "di", "da", "su", "con", "che", "e", "ma", "non", "mi", "ti", "ci", "del", "della", "dei", "delle", "al",
			"alla", "nel", "nella", "sono", "ho", "voglio", "vorrei", "devo", "dimmi", "puoi", "qual", "usare", "uso"));

	/** Italian words (accents removed) mapped to the English terms of the documentation */
	private static final Map<String, String> VOCABULARY = new HashMap<>();
	static {
		put("cifrare cifra cifratura crittografare crittografia cifrato", "encrypt crypter");
		put("decifrare decifra decrittare", "decrypt");
		put("scorciatoia scorciatoie tasti tasto combinazione", "shortcut keyboard");
		put("tema temi stile", "theme style");
		put("colore colori", "color");
		put("appunti", "notes clipboard");
		put("nota note promemoria", "notes");
		put("calcolatrice calcolatore", "calculator");
		put("avvio avviare avviarlo lanciare partenza", "start launch");
		put("aggiornare aggiorna aggiornamento aggiornamenti", "update");
		put("cartella cartelle directory", "folder directory");
		put("salvare salva salvataggio", "save");
		put("aprire apri apertura", "open");
		put("chiudere chiudi", "close");
		put("cercare cerca ricerca trova trovare", "search find");
		put("sostituire sostituisci", "replace");
		put("correttore ortografia ortografico", "spell checker spellchecker");
		put("lingua dizionario", "dictionary language");
		put("schermo monitor", "screen monitor");
		put("finestra finestre", "window");
		put("impostazioni opzioni preferenze configurazione", "preferences options settings configuration");
		put("grafica aspetto vista", "graphic view");
		put("carattere caratteri", "font");
		put("installazione installare installato", "installation install");
		put("licenza", "license");
		put("errore errori problema problemi guasto", "problem error log");
		put("registro", "log");
		put("password chiave chiavi", "key password");
		put("immagine immagini foto", "image");
		put("riga righe", "line lines");
		put("testo", "text");
		put("editor", "editor text");
		put("giochi gioco", "game snake puppynoid");
		put("novita", "new changelog");
		put("sfondo", "background");
		put("tabella tabelle", "table");
		put("copia copiare incolla incollare", "copy paste clipboard");
		put("annulla annullare", "undo");
		put("ripeti ripristina", "redo");
		put("usare utilizzare", "use");
		put("scheda schede", "tab tabs");
		put("rete", "network");
		put("velocita", "speed");
		put("plugin estensioni", "plugin");
		put("riavviare riavvio", "restart");
		put("uscire esci", "exit");
		put("home", "home page welcome");
		put("pagina iniziale benvenuto", "home page welcome");
		put("documentazione guida manuale", "documentation guide");
		put("codice", "code syntax");
		put("evidenziazione", "highlight syntax");
		put("macchina computer", "hardware");
	}

	private static void put(String italianWords, String english) {
		for (String word : italianWords.split(" ")) {
			VOCABULARY.put(word, english);
		}
	}

	public static AssistantKnowledge get() {
		return INSTANCE;
	}

	/** Section of a document: a title and its lines of text */
	private static final class Chunk {
		final String source;
		final String title;
		final List<String> lines = new ArrayList<>();
		final Map<String, Integer> weights = new HashMap<>();
		boolean shortcuts;
		int length;

		Chunk(String source, String title) {
			this.source = source;
			this.title = title;
		}
	}

	/** Immutable snapshot of the indexed documents */
	private static final class Index {
		final List<Chunk> chunks = new ArrayList<>();
		final Map<String, Integer> documentFrequency = new HashMap<>();
		final List<String> topics = new ArrayList<>();
		final List<String> latestVersionLines = new ArrayList<>();
		String latestVersion = "";
		String latestHighlight = "";
	}

	/** Result of a search */
	public static final class Answer {
		public final boolean found;
		public final String title;
		public final String source;
		public final List<String> lines;
		public final List<String> alsoSee;

		Answer(boolean found, String title, String source, List<String> lines, List<String> alsoSee) {
			this.found = found;
			this.title = title;
			this.source = source;
			this.lines = lines;
			this.alsoSee = alsoSee;
		}
	}

	private volatile Index index = new Index();
	private long guideStamp = -1;
	private long changelogStamp = -1;
	private boolean loaded;

	private AssistantKnowledge() {
	}

	////////// LOADING ////////////////

	/** Loads the documents on first use and again whenever they change on disk (cheap check: two timestamps) */
	public synchronized void ensureLoaded() {
		String guidePath = GeneralConfig.SCREEPT_TUTORIAL_HTML_PATH != null ? GeneralConfig.SCREEPT_TUTORIAL_HTML_PATH
				: GeneralConfig.CURRENT_EXECUTION_DIR + "../Readme/ReadMe.html";
		File guide = new File(guidePath);
		File changelog = GeneralConfig.RELEASE_NOTES_FILE;
		long guideTime = guide.exists() ? guide.lastModified() : 0;
		long changelogTime = changelog.exists() ? changelog.lastModified() : 0;
		if (loaded && guideTime == guideStamp && changelogTime == changelogStamp) {
			return;
		}

		Index fresh = new Index();
		try {
			if (guideTime > 0) {
				parseGuide(read(guide), fresh);
			}
			if (changelogTime > 0) {
				parseChangelog(read(changelog), fresh);
			}
		} catch (IOException | RuntimeException e) {
			// documents are optional: the assistant still answers its built-in commands
		}
		computeFrequencies(fresh);
		index = fresh;
		guideStamp = guideTime;
		changelogStamp = changelogTime;
		loaded = true;
	}

	private static String read(File file) throws IOException {
		return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
	}

	private static final Pattern SCRIPT = Pattern.compile("(?is)<(script|style)[^>]*>.*?</\\1>");
	private static final Pattern COMMENT = Pattern.compile("(?s)<!--.*?-->");
	private static final Pattern HEADING = Pattern.compile("(?is)<(h[1-4])[^>]*>(.*?)</\\1>");
	private static final Pattern LIST_ITEM = Pattern.compile("(?i)<li[^>]*>");
	private static final Pattern BLOCK = Pattern.compile("(?i)</?(p|br|ul|ol|div|tr|table|hr|tbody|thead|td|th)[^>]*>");
	private static final Pattern TAG = Pattern.compile("(?s)<[^>]+>");

	private static void parseGuide(String html, Index target) {
		String text = SCRIPT.matcher(html).replaceAll(" ");
		text = COMMENT.matcher(text).replaceAll(" ");
		text = HEADING.matcher(text).replaceAll("\n@@H$1@@$2\n");
		text = LIST_ITEM.matcher(text).replaceAll("\n");
		text = BLOCK.matcher(text).replaceAll("\n");
		text = TAG.matcher(text).replaceAll("");
		text = decodeEntities(text);

		Chunk current = null;
		boolean inShortcuts = false;
		for (String raw : text.split("\n")) {
			String line = raw.replaceAll("\\s+", " ").trim();
			if (line.isEmpty()) {
				continue;
			}
			if (line.startsWith("@@H")) {
				String level = line.substring(3, 5);
				String title = line.substring(line.indexOf("@@", 3) + 2).trim().replaceFirst("^[0-9.]+\\s*-\\s*", "");
				if ("h1".equals(level)) {
					inShortcuts = title.toUpperCase(Locale.ENGLISH).contains("SHORTCUT");
				}
				if (title.isEmpty() || "INDEX".equalsIgnoreCase(title)) {
					current = null;
					continue;
				}
				current = new Chunk("guide", capitalize(title));
				current.shortcuts = inShortcuts;
				target.chunks.add(current);
				if ("h2".equals(level)) {
					target.topics.add(current.title);
				}
			} else if (current != null && line.length() > 2) {
				current.lines.add(line);
			}
		}
		target.chunks.removeIf(chunk -> chunk.lines.isEmpty());
	}

	private static void parseChangelog(String markdown, Index target) {
		String version = "";
		String section = "";
		boolean firstVersion = true;
		for (String raw : markdown.split("\n")) {
			String line = raw.trim();
			if (line.startsWith("## ")) {
				if (!version.isEmpty()) {
					firstVersion = false;
				}
				version = line.substring(3).trim();
				section = "";
				if (firstVersion) {
					target.latestVersion = version;
				}
			} else if (line.startsWith("> ") && !version.isEmpty()) {
				if (firstVersion) {
					target.latestHighlight = line.substring(2).trim();
				}
			} else if (line.startsWith("### ")) {
				section = line.substring(4).trim();
			} else if (line.startsWith("- ") && !version.isEmpty()) {
				String item = line.substring(2).trim();
				Chunk chunk = new Chunk("changelog", version.split(" - ")[0] + " " + section);
				chunk.lines.add(item);
				target.chunks.add(chunk);
				if (firstVersion) {
					target.latestVersionLines.add(section + ": " + item);
				}
			}
		}
	}

	private static String decodeEntities(String text) {
		return text.replace("&nbsp;", " ").replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">")
				.replace("&quot;", "\"").replace("&#39;", "'").replace("&middot;", "-").replace("&rarr;", "->")
				.replace("&ndash;", "-").replace("&mdash;", "-").replace("&copy;", "(c)");
	}

	private static String capitalize(String upper) {
		String lower = upper.toLowerCase(Locale.ENGLISH);
		return lower.isEmpty() ? lower : Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
	}

	private static void computeFrequencies(Index target) {
		for (Chunk chunk : target.chunks) {
			Map<String, Integer> weights = chunk.weights;
			for (String stem : stems(chunk.title)) {
				weights.merge(stem, 3, Integer::sum);
			}
			int length = 0;
			for (String line : chunk.lines) {
				for (String stem : stems(line)) {
					weights.merge(stem, 1, Integer::sum);
					length++;
				}
			}
			chunk.length = length + 3;
			for (String stem : weights.keySet()) {
				target.documentFrequency.merge(stem, 1, Integer::sum);
			}
		}
	}

	////////// TEXT ANALYSIS ////////////////

	/** Lowercase, without accents */
	public static String normalize(String text) {
		String decomposed = Normalizer.normalize(text.toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
		return decomposed.replaceAll("\\p{M}", "").replace('’', '\'');
	}

	/** Search stems of a text: normalized, without stop words, shortened so that "encrypt/encrypting/encryption" match */
	static List<String> stems(String text) {
		List<String> result = new ArrayList<>();
		for (String token : normalize(text).split("[^a-z0-9]+")) {
			if (token.length() < 2 || STOP_WORDS.contains(token)) {
				continue;
			}
			result.add(stem(token));
		}
		return result;
	}

	private static String stem(String token) {
		return token.length() > PREFIX_LENGTH ? token.substring(0, PREFIX_LENGTH) : token;
	}

	/** First English term for an Italian word (the word itself when it is not in the vocabulary) */
	static String englishFor(String word) {
		String english = VOCABULARY.get(normalize(word));
		return english != null ? english.split(" ")[0] : normalize(word);
	}

	/** Query stems, with the English terms for the Italian words */
	static Set<String> queryStems(String query) {
		Set<String> result = new LinkedHashSet<>();
		for (String token : normalize(query).split("[^a-z0-9]+")) {
			if (token.length() < 2) {
				continue;
			}
			String english = VOCABULARY.get(token);
			if (english != null) {
				result.addAll(stems(english));
			}
			if (!STOP_WORDS.contains(token)) {
				result.add(stem(token));
			}
		}
		return result;
	}

	////////// SEARCH ////////////////

	/** Best matching section of the documents for a free question */
	public Answer search(String query) {
		ensureLoaded();
		Index snapshot = index;
		Set<String> terms = queryStems(query);
		if (terms.isEmpty() || snapshot.chunks.isEmpty()) {
			return new Answer(false, "", "", new ArrayList<String>(), new ArrayList<String>());
		}

		int total = snapshot.chunks.size();
		List<Chunk> ranked = new ArrayList<>();
		Map<Chunk, Double> scores = new HashMap<>();
		for (Chunk chunk : snapshot.chunks) {
			double score = 0;
			for (String term : terms) {
				Integer weight = chunk.weights.get(term);
				if (weight != null) {
					double idf = Math.log(1.0 + total / (double) snapshot.documentFrequency.get(term));
					score += idf * (1.0 + Math.log(weight));
				}
			}
			score /= 1.0 + 0.15 * Math.log(1.0 + chunk.length);
			if (score > 0) {
				scores.put(chunk, score);
				ranked.add(chunk);
			}
		}
		ranked.sort((a, b) -> Double.compare(scores.get(b), scores.get(a)));

		if (ranked.isEmpty() || scores.get(ranked.get(0)) < MIN_SCORE) {
			List<String> near = new ArrayList<>();
			for (int i = 0; i < Math.min(3, ranked.size()); i++) {
				near.add(ranked.get(i).title);
			}
			return new Answer(false, "", "", new ArrayList<String>(), near);
		}

		Chunk best = ranked.get(0);
		List<String> lines = bestLines(best, terms, snapshot);
		List<String> also = new ArrayList<>();
		double bestScore = scores.get(best);
		for (int i = 1; i < ranked.size() && also.size() < 2; i++) {
			Chunk other = ranked.get(i);
			if (scores.get(other) >= bestScore * 0.6 && !other.title.equals(best.title) && !also.contains(other.title)) {
				also.add(other.title);
			}
		}
		return new Answer(true, best.title, best.source, lines, also);
	}

	private static List<String> bestLines(Chunk chunk, Set<String> terms, Index snapshot) {
		List<String> picked = new ArrayList<>();
		List<double[]> scored = new ArrayList<>();
		int total = snapshot.chunks.size();
		for (int i = 0; i < chunk.lines.size(); i++) {
			Set<String> lineStems = new HashSet<>(stems(chunk.lines.get(i)));
			double weight = 0;
			for (String term : terms) {
				Integer frequency = snapshot.documentFrequency.get(term);
				if (frequency != null && lineStems.contains(term)) {
					// rare words say more about what the user is asking
					weight += Math.log(1.0 + total / (double) frequency);
				}
			}
			if (weight > 0) {
				scored.add(new double[] {i, weight});
			}
		}
		scored.sort((x, y) -> y[1] != x[1] ? Double.compare(y[1], x[1]) : Double.compare(x[0], y[0]));
		List<Integer> order = new ArrayList<>();
		for (int i = 0; i < Math.min(3, scored.size()); i++) {
			order.add((int) scored.get(i)[0]);
		}
		if (order.isEmpty()) {
			for (int i = 0; i < Math.min(2, chunk.lines.size()); i++) {
				order.add(i);
			}
		}
		order.sort(Integer::compare);
		for (int lineIndex : order) {
			picked.add(shorten(chunk.lines.get(lineIndex)));
		}
		return picked;
	}

	private static String shorten(String line) {
		if (line.length() <= MAX_LINE_CHARS) {
			return line;
		}
		int cut = line.lastIndexOf(' ', MAX_LINE_CHARS);
		return line.substring(0, cut > 100 ? cut : MAX_LINE_CHARS) + " ...";
	}

	////////// SPECIFIC VIEWS ////////////////

	/** "0.9.60 - 2026-10-27" style title of the latest documented version, empty when no changelog is available */
	public String latestVersion() {
		ensureLoaded();
		return index.latestVersion;
	}

	public String latestHighlight() {
		ensureLoaded();
		return index.latestHighlight;
	}

	public List<String> latestChanges() {
		ensureLoaded();
		return new ArrayList<>(index.latestVersionLines);
	}

	/** Titles of the main sections of the guide */
	public List<String> topics() {
		ensureLoaded();
		return new ArrayList<>(index.topics);
	}

	/** Shortcut lines of the guide containing every word of the filter (all of them when the filter is empty), at most {@code limit} */
	public List<String> shortcuts(String filter, int limit) {
		ensureLoaded();
		String[] words = normalize(filter == null ? "" : filter).trim().split("\\s+");
		Set<String> expanded = new LinkedHashSet<>();
		for (String word : words) {
			if (!word.isEmpty() && !STOP_WORDS.contains(word)) {
				String english = VOCABULARY.get(word);
				expanded.add(english != null ? english.split(" ")[0] : word);
			}
		}
		List<String> result = new ArrayList<>();
		for (Chunk chunk : index.chunks) {
			if (!chunk.shortcuts) {
				continue;
			}
			for (String line : chunk.lines) {
				String normalized = normalize(line);
				boolean all = true;
				for (String word : expanded) {
					if (!normalized.contains(word)) {
						all = false;
						break;
					}
				}
				if (all && line.contains("[") && !result.contains(line)) {
					result.add(shorten(line));
					if (result.size() >= limit) {
						return result;
					}
				}
			}
		}
		return result;
	}
}
