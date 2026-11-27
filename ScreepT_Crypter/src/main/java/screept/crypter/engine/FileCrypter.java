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
package screept.crypter.engine;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Files and folders on top of {@link CryptoEngine}, without any GUI.
 * <ul>
 * <li>file {@code a.txt} -> {@code a.txt.scpt}</li>
 * <li>folder {@code d}: by default ONE encrypted archive {@code d.dir.scpt} (names and structure are hidden too);
 * {@link OutputMode#PER_FILE_TREE}: same tree with every file encrypted; {@link OutputMode#ZIP_OF_ENCRYPTED_FILES}:
 * that tree packed in a plain {@code d.zip}</li>
 * <li>decryption recognises everything by itself: engine files, {@code .dir.scpt} archives, folders of encrypted
 * files and zips of them; the algorithms are read from each file header, only the password is needed.</li>
 * </ul>
 * Plaintext never touches the disk except in the final destination. Passwords are never stored or logged.
 */
public final class FileCrypter {

	public static final String EXT = ".scpt";
	public static final String DIR_EXT = ".dir.scpt";

	public enum OutputMode {
		/** Folder -> one encrypted archive (default). */
		SINGLE_ENCRYPTED_ARCHIVE,
		/** Folder -> same tree, every file encrypted. */
		PER_FILE_TREE,
		/** Folder or file -> plain zip that contains the encrypted files. */
		ZIP_OF_ENCRYPTED_FILES
	}

	private FileCrypter() {
	}

	// ---------------------------------------------------------------- encrypt

	public static File encrypt(File src, File destDir, char[] password, CryptoProfile profile, OutputMode mode)
			throws CryptoException, IOException {
		return encrypt(src, destDir, CryptoKey.of(password), profile, mode);
	}

	public static File decrypt(File src, File destDir, char[] password) throws CryptoException, IOException {
		return decrypt(src, destDir, CryptoKey.of(password));
	}

	/** @return the produced file or folder */
	public static File encrypt(File src, File destDir, CryptoKey password, CryptoProfile profile, OutputMode mode)
			throws CryptoException, IOException {
		if (src == null || !src.exists()) {
			throw new CryptoException("Source not found");
		}
		if (!destDir.isDirectory() && !destDir.mkdirs()) {
			throw new CryptoException("Cannot create the destination folder");
		}
		if (mode == OutputMode.ZIP_OF_ENCRYPTED_FILES) {
			return encryptToZip(src, destDir, password, profile);
		}
		if (src.isFile()) {
			File out = free(new File(destDir, src.getName() + EXT));
			encryptFile(src, out, password, profile);
			return out;
		}
		if (mode == OutputMode.PER_FILE_TREE) {
			File root = free(new File(destDir, src.getName()));
			encryptTree(src, root, password, profile);
			return root;
		}
		File out = free(new File(destDir, src.getName() + DIR_EXT));
		encryptFolderToArchive(src, out, password, profile);
		return out;
	}

	private static void encryptFile(File in, File out, CryptoKey password, CryptoProfile profile)
			throws CryptoException, IOException {
		try (InputStream is = new BufferedInputStream(new FileInputStream(in));
				OutputStream os = new BufferedOutputStream(new FileOutputStream(out))) {
			CryptoEngine.encrypt(is, os, password, profile);
		} catch (CryptoException | IOException | RuntimeException e) {
			out.delete();
			throw e;
		}
	}

	private static void encryptTree(File srcDir, File destDir, CryptoKey password, CryptoProfile profile)
			throws CryptoException, IOException {
		if (!destDir.isDirectory() && !destDir.mkdirs()) {
			throw new IOException("Cannot create " + destDir.getName());
		}
		File[] children = srcDir.listFiles();
		if (children == null) {
			return;
		}
		for (File child : children) {
			if (child.isDirectory()) {
				encryptTree(child, new File(destDir, child.getName()), password, profile);
			} else {
				encryptFile(child, new File(destDir, child.getName() + EXT), password, profile);
			}
		}
	}

	private static File encryptToZip(File src, File destDir, CryptoKey password, CryptoProfile profile)
			throws CryptoException, IOException {
		File out = free(new File(destDir, stripExt(src.getName()) + ".zip"));
		try (ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(out)))) {
			zip.setLevel(0); // encrypted data does not compress
			if (src.isFile()) {
				zipEncryptedEntry(zip, src, src.getName() + EXT, password, profile);
			} else {
				zipEncryptedTree(zip, src, src.getName() + "/", password, profile);
			}
		} catch (CryptoException | IOException | RuntimeException e) {
			out.delete();
			throw e;
		}
		return out;
	}

	private static void zipEncryptedTree(ZipOutputStream zip, File dir, String prefix, CryptoKey password,
			CryptoProfile profile) throws CryptoException, IOException {
		zip.putNextEntry(new ZipEntry(prefix)); // folder entry: empty folders survive too
		zip.closeEntry();
		File[] children = dir.listFiles();
		if (children == null) {
			return;
		}
		for (File child : children) {
			if (child.isDirectory()) {
				zipEncryptedTree(zip, child, prefix + child.getName() + "/", password, profile);
			} else {
				zipEncryptedEntry(zip, child, prefix + child.getName() + EXT, password, profile);
			}
		}
	}

	private static void zipEncryptedEntry(ZipOutputStream zip, File file, String entryName, CryptoKey password,
			CryptoProfile profile) throws CryptoException, IOException {
		zip.putNextEntry(new ZipEntry(entryName));
		try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
			CryptoEngine.encrypt(is, new NoCloseOutputStream(zip), password, profile);
		}
		zip.closeEntry();
	}

	private static void encryptFolderToArchive(final File srcDir, File out, CryptoKey password, CryptoProfile profile)
			throws CryptoException, IOException {
		final PipedOutputStream pipeOut = new PipedOutputStream();
		PipedInputStream pipeIn = new PipedInputStream(pipeOut, 1 << 16);
		final AtomicReference<Throwable> zipError = new AtomicReference<Throwable>();
		Thread zipper = new Thread(new Runnable() {
			@Override
			public void run() {
				try (ZipOutputStream zip = new ZipOutputStream(pipeOut)) {
					zipTree(zip, srcDir, srcDir.getName() + "/");
				} catch (Throwable t) {
					zipError.set(t);
					try {
						pipeOut.close();
					} catch (IOException ignored) {
						// nothing to do
					}
				}
			}
		}, "Crypter-zipper");
		zipper.setDaemon(true);
		zipper.start();

		try (OutputStream os = new BufferedOutputStream(new FileOutputStream(out))) {
			CryptoEngine.encrypt(pipeIn, os, password, profile);
			zipper.join();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			out.delete();
			throw new CryptoException("Interrupted");
		} catch (CryptoException | IOException | RuntimeException e) {
			out.delete();
			throw e;
		} finally {
			pipeIn.close();
		}
		if (zipError.get() != null) {
			out.delete();
			throw new IOException("Cannot read the folder to encrypt", zipError.get());
		}
	}

	private static void zipTree(ZipOutputStream zip, File dir, String prefix) throws IOException {
		zip.putNextEntry(new ZipEntry(prefix));
		zip.closeEntry();
		File[] children = dir.listFiles();
		if (children == null) {
			return;
		}
		byte[] buf = new byte[1 << 16];
		for (File child : children) {
			if (child.isDirectory()) {
				zipTree(zip, child, prefix + child.getName() + "/");
			} else {
				zip.putNextEntry(new ZipEntry(prefix + child.getName()));
				try (InputStream is = new FileInputStream(child)) {
					int n;
					while ((n = is.read(buf)) > 0) {
						zip.write(buf, 0, n);
					}
				}
				zip.closeEntry();
			}
		}
	}

	// ---------------------------------------------------------------- decrypt

	/**
	 * Decrypts a file, a {@code .dir.scpt} archive, a folder of encrypted files or a zip of them.
	 * @return the produced file or folder
	 */
	public static File decrypt(File src, File destDir, CryptoKey password) throws CryptoException, IOException {
		if (src == null || !src.exists()) {
			throw new CryptoException("Source not found");
		}
		if (!destDir.isDirectory() && !destDir.mkdirs()) {
			throw new CryptoException("Cannot create the destination folder");
		}
		if (src.isDirectory()) {
			File root = free(new File(destDir, src.getName()));
			decryptTree(src, root, password);
			return root;
		}
		if (isPlainZip(src)) {
			decryptZip(src, destDir, password); // entries already carry their top folder name
			return destDir;
		}
		if (src.getName().endsWith(DIR_EXT)) {
			File root = free(new File(destDir, src.getName().substring(0, src.getName().length() - DIR_EXT.length())));
			decryptArchiveToFolder(src, destDir, password);
			return root.exists() ? root : destDir;
		}
		if (isPicture(src) && inspect(src) == null) {
			return decryptFromPicture(src, destDir, password);
		}
		File out = free(new File(destDir, originalName(src.getName())));
		decryptFile(src, out, password);
		return out;
	}

	// ---------------------------------------------------------------- steganography

	/** Lossless pictures that may hide data (a JPEG would have destroyed it). */
	public static boolean isPicture(File f) {
		String n = f == null ? "" : f.getName().toLowerCase();
		return f != null && f.isFile() && (n.endsWith(".png") || n.endsWith(".bmp"));
	}

	/**
	 * Encrypts a file or a folder (one archive) and hides the result in a copy of {@code cover}: the output is a normal
	 * looking PNG named like the cover. Needs a password profile: the hiding places come from the password.
	 * The written picture is read back and checked before returning.
	 */
	public static File encryptIntoPicture(File src, File cover, File destDir, CryptoKey password, CryptoProfile profile)
			throws CryptoException, IOException {
		if (src == null || !src.exists()) {
			throw new CryptoException("Source not found");
		}
		if (!profile.kdf.passwordBased()) {
			throw new CryptoException("Hiding in a picture needs a password mode (public key files cannot be hidden)");
		}
		java.awt.image.BufferedImage img = cover == null || !cover.isFile() ? null : javax.imageio.ImageIO.read(cover);
		if (img == null) {
			throw new CryptoException("The cover picture cannot be read");
		}
		if (!destDir.isDirectory() && !destDir.mkdirs()) {
			throw new CryptoException("Cannot create the destination folder");
		}
		File tmp = File.createTempFile("scpt_hide", ".tmp", destDir);
		try {
			boolean folder = src.isDirectory();
			if (folder) {
				tmp.delete();
				encryptFolderToArchive(src, tmp, password, profile);
			} else {
				encryptFile(src, tmp, password, profile);
			}
			long cap = StegoPicture.capacity(img.getWidth(), img.getHeight());
			byte[] name = src.getName().getBytes(java.nio.charset.StandardCharsets.UTF_8);
			if (tmp.length() + 3 + name.length > cap) {
				throw new CryptoException("The picture can hide at most " + (cap / 1024) + " KB, the encrypted data is "
						+ ((tmp.length() + 1023) / 1024) + " KB: choose a bigger picture or less data");
			}
			byte[] enc = java.nio.file.Files.readAllBytes(tmp.toPath());
			byte[] payload = new byte[3 + name.length + enc.length];
			payload[0] = (byte) (folder ? 1 : 0);
			payload[1] = (byte) (name.length >>> 8);
			payload[2] = (byte) name.length;
			System.arraycopy(name, 0, payload, 3, name.length);
			System.arraycopy(enc, 0, payload, 3 + name.length, enc.length);
			java.awt.image.BufferedImage stego = StegoPicture.hide(img, payload, password.password());
			File out = free(new File(destDir, stripExt(cover.getName()) + ".png"));
			try {
				if (!javax.imageio.ImageIO.write(stego, "png", out)) {
					throw new IOException("PNG writer missing");
				}
				// reliability: what was written must give back exactly what was hidden
				if (!java.util.Arrays.equals(payload, StegoPicture.reveal(javax.imageio.ImageIO.read(out), password.password()))) {
					throw new CryptoException("The picture could not be verified after writing");
				}
			} catch (CryptoException | IOException | RuntimeException e) {
				out.delete();
				throw e;
			}
			return out;
		} finally {
			tmp.delete();
		}
	}

	private static File decryptFromPicture(File picture, File destDir, CryptoKey password) throws CryptoException, IOException {
		if (password == null || password.password() == null) {
			throw new CryptoException("The password is empty");
		}
		byte[] payload = StegoPicture.reveal(javax.imageio.ImageIO.read(picture), password.password());
		int nameLen = payload.length < 3 ? -1 : ((payload[1] & 0xff) << 8) | (payload[2] & 0xff);
		if (nameLen < 0 || payload.length < 3 + nameLen || (payload[0] != 0 && payload[0] != 1)) {
			throw new CryptoException("The hidden data is damaged");
		}
		String name = new String(payload, 3, nameLen, java.nio.charset.StandardCharsets.UTF_8);
		if (name.isEmpty() || name.contains("/") || name.contains("\\") || name.equals("..") || name.equals(".")) {
			throw new CryptoException("The hidden data is damaged");
		}
		File tmpDir = new File(destDir, ".scpt_reveal_" + System.nanoTime());
		if (!tmpDir.mkdirs()) {
			throw new IOException("Cannot create a temporary folder in the destination");
		}
		try {
			File enc = new File(tmpDir, name + (payload[0] == 1 ? DIR_EXT : EXT));
			java.nio.file.Files.write(enc.toPath(), java.util.Arrays.copyOfRange(payload, 3 + nameLen, payload.length));
			return decrypt(enc, destDir, password);
		} finally {
			deleteRecursive(tmpDir);
		}
	}

	/** Describes how a file is protected, from its header only (no password). Null if it is not an engine file. */
	public static CryptoProfile inspect(File file) {
		if (file == null || !file.isFile()) {
			return null;
		}
		byte[] head = new byte[CryptoEngine.MAX_HEADER_BYTES];
		try (InputStream is = new FileInputStream(file)) {
			int n = 0;
			for (int r; n < head.length && (r = is.read(head, n, head.length - n)) > 0;) {
				n += r;
			}
			if (n < 4 || !CryptoEngine.isEngineData(head)) {
				return null;
			}
			return CryptoEngine.readProfile(java.util.Arrays.copyOf(head, n));
		} catch (IOException | CryptoException e) {
			return null;
		}
	}

	/** True if the source is something this engine produced (engine file, folder of them, or zip of them). */
	public static boolean isEngineInput(File src) {
		if (src == null || !src.exists()) {
			return false;
		}
		if (src.isFile()) {
			if (inspect(src) != null) {
				return true;
			}
			if (!isPlainZip(src)) {
				return false;
			}
			try (java.util.zip.ZipFile z = new java.util.zip.ZipFile(src)) {
				java.util.Enumeration<? extends ZipEntry> en = z.entries();
				while (en.hasMoreElements()) {
					if (en.nextElement().getName().endsWith(EXT)) {
						return true;
					}
				}
			} catch (IOException e) {
				return false;
			}
			return false;
		}
		File[] children = src.listFiles();
		if (children != null) {
			for (File c : children) {
				if (isEngineInput(c)) {
					return true;
				}
			}
		}
		return false;
	}

	private static void decryptFile(File in, File out, CryptoKey password) throws CryptoException, IOException {
		try (InputStream is = new BufferedInputStream(new FileInputStream(in));
				OutputStream os = new BufferedOutputStream(new FileOutputStream(out))) {
			CryptoEngine.decrypt(is, os, password);
		} catch (CryptoException | IOException | RuntimeException e) {
			out.delete(); // never leave partial or garbage plaintext behind
			throw e;
		}
	}

	private static void decryptTree(File srcDir, File destDir, CryptoKey password) throws CryptoException, IOException {
		if (!destDir.isDirectory() && !destDir.mkdirs()) {
			throw new IOException("Cannot create " + destDir.getName());
		}
		File[] children = srcDir.listFiles();
		if (children == null) {
			return;
		}
		for (File child : children) {
			if (child.isDirectory()) {
				decryptTree(child, new File(destDir, child.getName()), password);
			} else if (child.getName().endsWith(EXT)) {
				decryptFile(child, new File(destDir, originalName(child.getName())), password);
			}
		}
	}

	private static void decryptZip(File zipFile, File destRoot, CryptoKey password) throws CryptoException, IOException {
		destRoot.mkdirs();
		try (ZipInputStream zip = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)))) {
			ZipEntry e;
			while ((e = zip.getNextEntry()) != null) {
				if (e.isDirectory()) {
					safeTarget(destRoot, e.getName()).mkdirs();
					continue;
				}
				File target = safeTarget(destRoot, originalName(e.getName()));
				target.getParentFile().mkdirs();
				try (OutputStream os = new BufferedOutputStream(new FileOutputStream(target))) {
					CryptoEngine.decrypt(new NoCloseInputStream(zip), os, password);
				} catch (CryptoException | IOException | RuntimeException ex) {
					target.delete();
					throw ex;
				}
			}
		}
	}

	/** Extracts into a temporary folder first: a damaged or truncated archive leaves nothing behind. */
	private static void decryptArchiveToFolder(File archive, File destDir, CryptoKey password)
			throws CryptoException, IOException {
		File tmp = new File(destDir, ".scpt_extract_" + System.nanoTime());
		if (!tmp.mkdirs()) {
			throw new IOException("Cannot create a temporary folder in the destination");
		}
		try {
			decryptArchiveInto(archive, tmp, password);
			File[] top = tmp.listFiles();
			if (top != null) {
				for (File f : top) {
					if (!f.renameTo(free(new File(destDir, f.getName())))) {
						throw new IOException("Cannot move " + f.getName());
					}
				}
			}
		} finally {
			deleteRecursive(tmp);
		}
	}

	private static void deleteRecursive(File f) {
		File[] children = f.listFiles();
		if (children != null) {
			for (File c : children) {
				deleteRecursive(c);
			}
		}
		f.delete();
	}

	private static void decryptArchiveInto(final File archive, File destDir, CryptoKey password)
			throws CryptoException, IOException {
		final PipedInputStream pipeIn = new PipedInputStream(1 << 16);
		final PipedOutputStream pipeOut = new PipedOutputStream(pipeIn);
		final AtomicReference<Throwable> unzipError = new AtomicReference<Throwable>();
		final File dest = destDir;
		Thread unzipper = new Thread(new Runnable() {
			@Override
			public void run() {
				try (ZipInputStream zip = new ZipInputStream(pipeIn)) {
					ZipEntry e;
					while ((e = zip.getNextEntry()) != null) {
						File target = safeTarget(dest, e.getName());
						if (e.isDirectory()) {
							target.mkdirs();
							continue;
						}
						target.getParentFile().mkdirs();
						target = free(target);
						try (OutputStream os = new BufferedOutputStream(new FileOutputStream(target))) {
							byte[] buf = new byte[1 << 16];
							int n;
							while ((n = zip.read(buf)) > 0) {
								os.write(buf, 0, n);
							}
						}
					}
					// drain what is left (zip trailer) so the producer never blocks
					byte[] sink = new byte[1 << 12];
					while (pipeIn.read(sink) >= 0) {
						// discard
					}
				} catch (Throwable t) {
					unzipError.set(t);
					try {
						pipeIn.close();
					} catch (IOException ignored) {
						// nothing to do
					}
				}
			}
		}, "Crypter-unzipper");
		unzipper.setDaemon(true);
		unzipper.start();

		try (InputStream is = new BufferedInputStream(new FileInputStream(archive))) {
			CryptoEngine.decrypt(is, pipeOut, password);
		} catch (IOException e) {
			if (unzipError.get() == null) {
				throw e;
			} // else the unzipper closed the pipe: report its error below
		} finally {
			pipeOut.close();
			// always wait: the caller deletes the temporary folder and nothing must be written into it afterwards
			try {
				unzipper.join();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new CryptoException("Interrupted");
			}
		}
		if (unzipError.get() != null) {
			Throwable t = unzipError.get();
			throw new CryptoException("Cannot extract the decrypted folder: " + t.getClass().getSimpleName());
		}
	}

	// ---------------------------------------------------------------- helpers

	private static boolean isPlainZip(File f) {
		if (!f.getName().toLowerCase().endsWith(".zip")) {
			return false;
		}
		byte[] head = new byte[4];
		try (InputStream is = new FileInputStream(f)) {
			return is.read(head) == 4 && head[0] == 'P' && head[1] == 'K';
		} catch (IOException e) {
			return false;
		}
	}

	private static String originalName(String name) {
		String n = name.endsWith(EXT) ? name.substring(0, name.length() - EXT.length()) : name + ".decrypted";
		return n.isEmpty() ? "decrypted" : n;
	}

	private static String stripExt(String name) {
		int dot = name.lastIndexOf('.');
		return dot > 0 ? name.substring(0, dot) : name;
	}

	/** Zip-slip protection: the entry must stay inside the destination. */
	private static File safeTarget(File root, String entryName) throws IOException {
		File target = new File(root, entryName);
		String rootPath = root.getCanonicalPath() + File.separator;
		if (!(target.getCanonicalPath() + File.separator).startsWith(rootPath)) {
			throw new IOException("Unsafe path in archive");
		}
		return target;
	}

	/** Never overwrite: name, name (1), name (2)... */
	private static File free(File f) {
		if (!f.exists()) {
			return f;
		}
		String name = f.getName();
		int dot = name.indexOf('.');
		String base = dot > 0 ? name.substring(0, dot) : name;
		String ext = dot > 0 ? name.substring(dot) : "";
		for (int i = 1;; i++) {
			File c = new File(f.getParentFile(), base + " (" + i + ")" + ext);
			if (!c.exists()) {
				return c;
			}
		}
	}

	private static final class NoCloseOutputStream extends java.io.FilterOutputStream {
		NoCloseOutputStream(OutputStream out) {
			super(out);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			out.write(b, off, len);
		}

		@Override
		public void close() throws IOException {
			flush();
		}
	}

	private static final class NoCloseInputStream extends java.io.FilterInputStream {
		NoCloseInputStream(InputStream in) {
			super(in);
		}

		@Override
		public void close() {
			// the zip entry stream is closed by the caller
		}
	}

}
