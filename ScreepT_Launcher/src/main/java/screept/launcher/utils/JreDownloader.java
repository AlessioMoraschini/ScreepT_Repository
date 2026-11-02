/**
 *
 * =========================================================================================
*  Copyright (C) 2019-2020
*
*  AM-Design-Development - (Alessio Moraschini) - All Rights Reserved
* =========================================================================================
*
* You should have received a copy of the license with this file.
* If not, please write to: info@am-design-development.com, or visit : https://www.am-design-development.com
*/
package screept.launcher.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import os.commons.utils.JvmLocator;

/**
 * Downloads the latest Eclipse Temurin JRE of a given Java feature release (21 by default) for the current OS and
 * architecture, checks its SHA-256 and unpacks it into a folder (normally the "jre" folder next to the application,
 * which {@link JvmLocator} looks into). Nothing is installed system wide and no administrator rights are needed.
 */
public final class JreDownloader {

	public static final int FEATURE_VERSION = 21;

	/** Receives what is going on: percent is 0-100, or -1 when it cannot be known */
	public interface Progress {
		void update(String message, int percent);
	}

	private static final String ASSETS_URL = "https://api.adoptium.net/v3/assets/latest/%d/hotspot?architecture=%s&image_type=jre&os=%s&vendor=eclipse";
	private static final Pattern PACKAGE_PATTERN = Pattern.compile(
			"\"package\"\\s*:\\s*\\{[^}]*?\"checksum\"\\s*:\\s*\"([0-9a-fA-F]{64})\"[^}]*?\"link\"\\s*:\\s*\"([^\"]+)\"", Pattern.DOTALL);

	private JreDownloader() {
	}

	/**
	 * @param targetDir where the JRE will be placed (replaced if present), e.g. new File("jre")
	 * @return the canonical path of the new Java home
	 * @throws IOException when the download, the checksum check or the unpacking fail: the previous folder is left untouched
	 */
	public static File download(File targetDir, Progress progress) throws IOException {
		String os = osName();
		String arch = archName();
		progress.update("Looking for the latest Java " + FEATURE_VERSION + " for " + os + " " + arch + "...", -1);

		String assets = readText(String.format(ASSETS_URL, FEATURE_VERSION, arch, os));
		Matcher matcher = PACKAGE_PATTERN.matcher(assets);
		if (!matcher.find()) {
			throw new IOException("Java " + FEATURE_VERSION + " (JRE) is not available for " + os + " " + arch);
		}
		String checksum = matcher.group(1).toLowerCase(Locale.ROOT);
		String link = matcher.group(2);
		boolean zip = link.toLowerCase(Locale.ROOT).endsWith(".zip");

		File parent = targetDir.getAbsoluteFile().getParentFile();
		File archive = File.createTempFile("screept-jre-", zip ? ".zip" : ".tar.gz", parent);
		File unpacked = new File(parent, targetDir.getName() + ".new");
		try {
			download(link, archive, checksum, progress);

			progress.update("Unpacking...", -1);
			deleteRecursively(unpacked);
			if (!unpacked.mkdirs()) {
				throw new IOException("Cannot create folder " + unpacked);
			}
			if (zip) {
				unzipStrippingTopFolder(archive, unpacked);
			} else {
				untar(archive, unpacked);
			}
			if (!JvmLocator.isJavaHome(unpacked) && !JvmLocator.isJavaHome(new File(unpacked, "Contents/Home"))) {
				throw new IOException("The downloaded archive does not contain a Java runtime");
			}

			progress.update("Installing...", -1);
			replace(targetDir, unpacked);

		} finally {
			archive.delete();
			deleteRecursively(unpacked);
		}

		File home = JvmLocator.isJavaHome(targetDir) ? targetDir : new File(targetDir, "Contents/Home");
		return home.getCanonicalFile();
	}

	// ---------------------------------------------------------------------------------------------

	private static String osName() throws IOException {
		String name = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
		if (name.contains("win")) {
			return "windows";
		} else if (name.contains("mac") || name.contains("darwin")) {
			return "mac";
		} else if (name.contains("linux")) {
			return "linux";
		}
		throw new IOException("Unsupported operating system: " + name);
	}

	private static String archName() throws IOException {
		String arch = System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);
		if (arch.contains("aarch64") || arch.contains("arm64")) {
			return "aarch64";
		} else if (arch.equals("amd64") || arch.equals("x86_64") || arch.equals("x64")) {
			return "x64";
		}
		throw new IOException("Java " + FEATURE_VERSION + " is not available for the architecture " + arch
				+ " (32 bit systems are not supported by Java " + FEATURE_VERSION + ")");
	}

	private static HttpURLConnection open(String address) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL(address).openConnection();
		connection.setConnectTimeout(15000);
		connection.setReadTimeout(60000);
		connection.setInstanceFollowRedirects(true);
		connection.setRequestProperty("User-Agent", "ScreepT-Launcher");
		int code = connection.getResponseCode();
		if (code != HttpURLConnection.HTTP_OK) {
			throw new IOException("HTTP " + code + " from " + new URL(address).getHost());
		}
		return connection;
	}

	private static String readText(String address) throws IOException {
		HttpURLConnection connection = open(address);
		try (InputStream in = connection.getInputStream()) {
			java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
			byte[] buffer = new byte[8192];
			int read;
			while ((read = in.read(buffer)) > 0) {
				out.write(buffer, 0, read);
			}
			return new String(out.toByteArray(), Charset.forName("UTF-8"));
		} finally {
			connection.disconnect();
		}
	}

	private static void download(String link, File target, String expectedSha256, Progress progress) throws IOException {
		HttpURLConnection connection = open(link);
		long total = connection.getContentLengthLong();
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new IOException(e);
		}
		try (InputStream in = new BufferedInputStream(connection.getInputStream()); OutputStream out = new FileOutputStream(target)) {
			byte[] buffer = new byte[64 * 1024];
			long done = 0;
			int lastPercent = -2;
			int read;
			while ((read = in.read(buffer)) > 0) {
				out.write(buffer, 0, read);
				digest.update(buffer, 0, read);
				done += read;
				int percent = total > 0 ? (int) (done * 100 / total) : -1;
				if (percent != lastPercent) {
					lastPercent = percent;
					progress.update(String.format("Downloading Java %d... %d / %d MB", FEATURE_VERSION, done >> 20, Math.max(total, done) >> 20), percent);
				}
			}
		} finally {
			connection.disconnect();
		}

		StringBuilder hex = new StringBuilder();
		for (byte b : digest.digest()) {
			hex.append(String.format("%02x", b));
		}
		if (!hex.toString().equals(expectedSha256)) {
			throw new IOException("Checksum mismatch: the downloaded file is corrupted or has been altered");
		}
	}

	private static void unzipStrippingTopFolder(File zip, File target) throws IOException {
		String targetPath = target.getCanonicalPath() + File.separator;
		try (ZipInputStream in = new ZipInputStream(new BufferedInputStream(new FileInputStream(zip)))) {
			ZipEntry entry;
			byte[] buffer = new byte[64 * 1024];
			while ((entry = in.getNextEntry()) != null) {
				String name = entry.getName().replace('\\', '/');
				int slash = name.indexOf('/');
				String relative = slash < 0 ? "" : name.substring(slash + 1);
				if (relative.isEmpty()) {
					continue; // the top folder itself
				}
				File out = new File(target, relative);
				if (!out.getCanonicalPath().startsWith(targetPath)) {
					throw new IOException("Unsafe path in archive: " + name);
				}
				if (entry.isDirectory()) {
					out.mkdirs();
					continue;
				}
				out.getParentFile().mkdirs();
				try (OutputStream o = new FileOutputStream(out)) {
					int read;
					while ((read = in.read(buffer)) > 0) {
						o.write(buffer, 0, read);
					}
				}
			}
		}
	}

	/** tar is part of Linux, macOS and recent Windows */
	private static void untar(File archive, File target) throws IOException {
		Process process = new ProcessBuilder("tar", "-xzf", archive.getAbsolutePath(), "-C", target.getAbsolutePath(), "--strip-components=1")
				.redirectErrorStream(true).start();
		try {
			if (process.waitFor() != 0) {
				throw new IOException("tar failed to unpack the archive");
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IOException("Interrupted while unpacking", e);
		}
	}

	/** Swaps the folders: the old one is kept until the new one is in place, and restored if the swap fails */
	private static void replace(File targetDir, File unpacked) throws IOException {
		File backup = new File(targetDir.getAbsoluteFile().getParentFile(), targetDir.getName() + ".old");
		deleteRecursively(backup);
		boolean hadOld = targetDir.exists();
		if (hadOld && !targetDir.renameTo(backup)) {
			throw new IOException("Cannot replace " + targetDir + ": it is probably in use by a running Java. Close ScreepT and try again.");
		}
		if (!unpacked.renameTo(targetDir)) {
			if (hadOld) {
				backup.renameTo(targetDir);
			}
			throw new IOException("Cannot move the new Java into " + targetDir);
		}
		deleteRecursively(backup);
	}

	private static void deleteRecursively(File dir) {
		if (dir == null || !dir.exists()) {
			return;
		}
		try {
			Files.walkFileTree(dir.toPath(), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					file.toFile().setWritable(true);
					Files.deleteIfExists(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path directory, IOException exc) throws IOException {
					Files.deleteIfExists(directory);
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			// best effort: leftovers are removed by the next run
		}
	}
}
