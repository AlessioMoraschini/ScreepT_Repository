package gui.texteditor.commons.filesearch.name;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * Names of all the files under a folder, read once and kept in memory: typing in the search box only filters this list
 * (milliseconds) instead of walking the disk at every key.
 */
class FileNameIndex {

	/** Folders that hold thousands of generated files nobody searches by name */
	private static final Set<String> SKIPPED_FOLDERS = new HashSet<String>(Arrays.asList(".git", ".svn", ".hg", "node_modules", ".idea", ".gradle", "$RECYCLE.BIN", "System Volume Information"));

	static final int MAX_ENTRIES = 600_000;

	final File root;
	private final List<String> paths = new ArrayList<String>();
	private final List<String> lowerNames = new ArrayList<String>();
	private volatile boolean ready = false;
	volatile long builtAt = 0;
	private volatile boolean truncated = false;
	private final AtomicBoolean cancelled = new AtomicBoolean(false);

	FileNameIndex(File root) {
		this.root = root;
	}

	boolean isReady() {
		return ready;
	}

	boolean isTruncated() {
		return truncated;
	}

	int size() {
		return lowerNames.size();
	}

	void cancel() {
		cancelled.set(true);
	}

	/** Walks the folder (call it from a background thread) */
	void build() {
		if (root == null || !root.isDirectory()) {
			ready = true;
			return;
		}
		try {
			Files.walkFileTree(root.toPath(), new FileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
					if (cancelled.get()) {
						return FileVisitResult.TERMINATE;
					}
					Path name = dir.getFileName();
					if (name != null && !dir.equals(root.toPath()) && SKIPPED_FOLDERS.contains(name.toString())) {
						return FileVisitResult.SKIP_SUBTREE;
					}
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
					if (cancelled.get()) {
						return FileVisitResult.TERMINATE;
					}
					if (attrs.isRegularFile()) {
						synchronized (paths) {
							if (lowerNames.size() >= MAX_ENTRIES) {
								truncated = true;
								return FileVisitResult.TERMINATE;
							}
							paths.add(file.toString());
							lowerNames.add(file.getFileName().toString().toLowerCase(Locale.ROOT));
						}
					}
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) {
					return cancelled.get() ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
					return cancelled.get() ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			// partial index is still useful
		}
		ready = !cancelled.get();
		builtAt = System.currentTimeMillis();
	}

	/**
	 * Files whose name matches: case insensitive, "*" and "?" are wildcards, without wildcards it is "contains".
	 * Names that start with the text come first. Returns null if the search was superseded (check {@code stale}).
	 */
	List<File> search(String text, int limit, java.util.function.BooleanSupplier stale) {
		String pattern = text.trim().toLowerCase(Locale.ROOT);
		boolean all = pattern.equals("*");
		boolean wildcard = pattern.indexOf('*') >= 0 || pattern.indexOf('?') >= 0;
		Pattern regex = null;
		if (wildcard && !all) {
			StringBuilder sb = new StringBuilder();
			for (char c : pattern.toCharArray()) {
				sb.append(c == '*' ? ".*" : c == '?' ? "." : Pattern.quote(String.valueOf(c)));
			}
			regex = Pattern.compile(sb.toString());
		}

		List<String> first = new ArrayList<String>();
		List<String> others = new ArrayList<String>();
		int count;
		synchronized (paths) {
			count = lowerNames.size();
		}
		for (int i = 0; i < count; i++) {
			if ((i & 0x1FFF) == 0 && stale.getAsBoolean()) {
				return null;
			}
			String name;
			String path;
			synchronized (paths) {
				name = lowerNames.get(i);
				path = paths.get(i);
			}
			boolean match;
			boolean prefix;
			if (all) {
				match = true;
				prefix = true;
			} else if (regex != null) {
				match = regex.matcher(name).matches();
				prefix = match;
			} else {
				int at = name.indexOf(pattern);
				match = at >= 0;
				prefix = at == 0;
			}
			if (!match) {
				continue;
			}
			if (prefix) {
				first.add(path);
				if (first.size() >= limit) {
					break;
				}
			} else if (others.size() < limit) {
				others.add(path);
			}
		}

		List<File> result = new ArrayList<File>(Math.min(limit, first.size() + others.size()));
		for (String p : first) {
			result.add(new File(p));
		}
		for (String p : others) {
			if (result.size() >= limit) {
				break;
			}
			result.add(new File(p));
		}
		return result;
	}
}
