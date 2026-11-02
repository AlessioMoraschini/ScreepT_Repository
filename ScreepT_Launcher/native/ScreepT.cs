/*
 * Copyright (c) A.M. Design And Development. All rights reserved.
 *
 * ScreepT native launcher (Windows). Built with the .NET Framework C# compiler, see build_exe.bat.
 * What it does: finds a Java >= 8 (local jre, JAVA_HOME, PATH, registry); if none is found it offers to
 * download Amazon Corretto 8 JRE into <ScreepT folder>\jre (no admin rights, nothing installed system wide);
 * then starts Application\ScreepT_Launcher.jar. Plain process start: no packing, no self-extraction.
 */
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Drawing;
using System.IO;
using System.IO.Compression;
using System.Net;
using System.Reflection;
using System.Security.Cryptography;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading;
using System.Windows.Forms;
using Microsoft.Win32;

[assembly: AssemblyTitle("ScreepT launcher")]
[assembly: AssemblyDescription("Launcher for ScreepT application")]
[assembly: AssemblyCompany("A.M. Design And Development")]
[assembly: AssemblyProduct("ScreepT")]
[assembly: AssemblyCopyright("Copyright (c) A.M. Design And Development")]
[assembly: AssemblyVersion("1.0.0.0")]
[assembly: AssemblyFileVersion("1.0.0.0")]

namespace ScreepTLauncher
{
	internal static class Program
	{
		private const string JAR_NAME = "ScreepT_Launcher.jar";
		private const string JVM_ARGS = "-Dsun.java2d.ddscale=false -Dsun.java2d.noddraw=true -Dsun.java2d.uiScale=1 -Dfile.encoding=utf8 -Xmx256m -Xms128m";

		[STAThread]
		private static int Main(string[] args)
		{
			string root = AppDomain.CurrentDomain.BaseDirectory.TrimEnd('\\');
			string appDir = Path.Combine(root, "Application");
			if (!File.Exists(Path.Combine(appDir, JAR_NAME)))
			{
				Ui.Error("Cannot find " + JAR_NAME + " in:\n" + appDir + "\n\nKeep this launcher next to the Application folder.");
				return 2;
			}

			string localJre = Path.Combine(root, "jre");
			string javaw = JavaFinder.Find(localJre);

			if (javaw == null)
			{
				if (!Ui.AskInstall()) return 1;
				if (!Ui.RunDownload(JreInstaller.Url(), JreInstaller.ChecksumUrl(), localJre)) return 3;
				javaw = JavaFinder.Find(localJre);
				if (javaw == null)
				{
					Ui.Error("Java was downloaded but it was not found in:\n" + localJre);
					return 3;
				}
			}
			return Launch(javaw, appDir, args);
		}

		private static int Launch(string javaw, string appDir, string[] userArgs)
		{
			StringBuilder cmd = new StringBuilder(JVM_ARGS);
			cmd.Append(" -jar ").Append(JAR_NAME);
			foreach (string a in userArgs) cmd.Append(' ').Append(Quote(a));

			ProcessStartInfo psi = new ProcessStartInfo(javaw, cmd.ToString());
			psi.WorkingDirectory = appDir;
			psi.UseShellExecute = false;
			psi.CreateNoWindow = true;

			// the Java chosen here must also be the one the ScreepT launcher finds in PATH
			string javaBin = Path.GetDirectoryName(javaw);
			string javaHome = Path.GetDirectoryName(javaBin);
			string oldPath = Environment.GetEnvironmentVariable("PATH") ?? "";
			psi.EnvironmentVariables["PATH"] = javaBin + ";" + oldPath;
			psi.EnvironmentVariables["JAVA_HOME"] = javaHome;

			try
			{
				using (Process p = Process.Start(psi))
				{
					// a very fast exit with an error code means the JVM could not even start
					if (p.WaitForExit(3000) && p.ExitCode != 0)
					{
						Ui.Error("ScreepT stopped right after the start (exit code " + p.ExitCode + ").\n\nJava used:\n" + javaw);
						return p.ExitCode;
					}
				}
			}
			catch (Exception e)
			{
				Ui.Error("Cannot start ScreepT:\n" + e.Message);
				return 4;
			}
			return 0;
		}

		/// <summary>Quotes one argument following the Windows command line rules.</summary>
		private static string Quote(string arg)
		{
			if (arg.Length > 0 && arg.IndexOfAny(new char[] { ' ', '\t', '"' }) < 0) return arg;
			StringBuilder sb = new StringBuilder("\"");
			int backslashes = 0;
			foreach (char c in arg)
			{
				if (c == '\\') { backslashes++; continue; }
				if (c == '"') sb.Append('\\', backslashes * 2 + 1).Append('"');
				else sb.Append('\\', backslashes).Append(c);
				backslashes = 0;
			}
			sb.Append('\\', backslashes * 2).Append('"');
			return sb.ToString();
		}
	}

	internal static class JavaFinder
	{
		/// <summary>Returns the full path of a javaw.exe with Java >= 8, or null.
		/// SCREEPT_FORCE_NO_JAVA=1 simulates a PC without Java (only to test the download path).</summary>
		public static string Find(string localJre)
		{
			if (Environment.GetEnvironmentVariable("SCREEPT_FORCE_NO_JAVA") == "1" && !Directory.Exists(localJre)) return null;

			List<string> candidates = new List<string>();
			candidates.Add(Path.Combine(localJre, "bin"));
			if (Environment.GetEnvironmentVariable("SCREEPT_FORCE_NO_JAVA") != "1")
			{
				string javaHome = Environment.GetEnvironmentVariable("JAVA_HOME");
				if (!string.IsNullOrEmpty(javaHome)) candidates.Add(Path.Combine(javaHome.Trim('"'), "bin"));
				foreach (string dir in (Environment.GetEnvironmentVariable("PATH") ?? "").Split(';'))
					if (dir.Trim().Length > 0) candidates.Add(dir.Trim().Trim('"'));
				candidates.AddRange(RegistryJavaBins());
			}

			HashSet<string> seen = new HashSet<string>(StringComparer.OrdinalIgnoreCase);
			foreach (string bin in candidates)
			{
				string javaw = SafeCombine(bin, "javaw.exe");
				if (javaw == null || !seen.Add(javaw) || !File.Exists(javaw)) continue;
				if (MajorVersion(Path.Combine(bin, "java.exe")) >= 8) return javaw;
			}
			return null;
		}

		private static string SafeCombine(string dir, string file)
		{
			try { return Path.Combine(dir, file); } catch (ArgumentException) { return null; }
		}

		private static IEnumerable<string> RegistryJavaBins()
		{
			string[] roots = { @"SOFTWARE\JavaSoft\Java Runtime Environment", @"SOFTWARE\JavaSoft\Java Development Kit", @"SOFTWARE\JavaSoft\JRE", @"SOFTWARE\JavaSoft\JDK" };
			foreach (RegistryView view in new RegistryView[] { RegistryView.Registry64, RegistryView.Registry32 })
			{
				RegistryKey hklm = null;
				try { hklm = RegistryKey.OpenBaseKey(RegistryHive.LocalMachine, view); } catch (Exception) { continue; }
				using (hklm)
				{
					foreach (string root in roots)
					{
						string home = null;
						try
						{
							using (RegistryKey k = hklm.OpenSubKey(root))
							{
								if (k == null) continue;
								string current = k.GetValue("CurrentVersion") as string;
								if (current == null) continue;
								using (RegistryKey v = k.OpenSubKey(current)) { if (v != null) home = v.GetValue("JavaHome") as string; }
							}
						}
						catch (Exception) { }
						if (!string.IsNullOrEmpty(home)) yield return Path.Combine(home, "bin");
					}
				}
			}
		}

		/// <summary>Runs "java -version" and returns the major version (8 for 1.8.x), or 0 when unknown.</summary>
		private static int MajorVersion(string javaExe)
		{
			if (!File.Exists(javaExe)) return 0;
			try
			{
				ProcessStartInfo psi = new ProcessStartInfo(javaExe, "-version");
				psi.UseShellExecute = false;
				psi.CreateNoWindow = true;
				psi.RedirectStandardError = true;
				psi.RedirectStandardOutput = true;
				using (Process p = Process.Start(psi))
				{
					string output = p.StandardError.ReadToEnd() + p.StandardOutput.ReadToEnd();
					p.WaitForExit(5000);
					Match m = Regex.Match(output, "version \"(\\d+)(?:\\.(\\d+))?");
					if (!m.Success) return 0;
					int first = int.Parse(m.Groups[1].Value);
					return (first == 1 && m.Groups[2].Success) ? int.Parse(m.Groups[2].Value) : first;
				}
			}
			catch (Exception) { return 0; }
		}
	}

	internal static class JreInstaller
	{
		private static string FileName()
		{
			return "amazon-corretto-8-" + (Environment.Is64BitOperatingSystem ? "x64" : "x86") + "-windows-jre.zip";
		}

		public static string Url() { return "https://corretto.aws/downloads/latest/" + FileName(); }

		public static string ChecksumUrl() { return "https://corretto.aws/downloads/latest_checksum/" + FileName(); }
	}

	internal static class Ui
	{
		private const string TITLE = "ScreepT";

		public static void Error(string message)
		{
			MessageBox.Show(message, TITLE, MessageBoxButtons.OK, MessageBoxIcon.Error);
		}

		public static bool AskInstall()
		{
			Application.EnableVisualStyles();
			DialogResult r = MessageBox.Show(
				"ScreepT needs Java 8 or newer, but no compatible Java was found on this PC.\n\n" +
				"Download Amazon Corretto 8 (free, about 40 MB) into the ScreepT folder and start ScreepT?\n\n" +
				"It is not installed system wide and needs no administrator rights.",
				TITLE, MessageBoxButtons.YesNo, MessageBoxIcon.Question);
			return r == DialogResult.Yes;
		}

		public static bool RunDownload(string url, string checksumUrl, string targetDir)
		{
			using (DownloadForm form = new DownloadForm(url, checksumUrl, targetDir))
			{
				Application.Run(form);
				if (!form.Success && form.ErrorMessage != null) Error(form.ErrorMessage);
				return form.Success;
			}
		}
	}

	internal sealed class DownloadForm : Form
	{
		private readonly string url, checksumUrl, targetDir;
		private readonly Label label = new Label();
		private readonly ProgressBar bar = new ProgressBar();
		private readonly Button cancel = new Button();
		private WebClient client;
		private string zipPath;
		private volatile bool cancelled;
		private volatile bool done;

		public bool Success;
		public string ErrorMessage;

		public DownloadForm(string url, string checksumUrl, string targetDir)
		{
			this.url = url; this.checksumUrl = checksumUrl; this.targetDir = targetDir;
			Text = "ScreepT - Java setup";
			FormBorderStyle = FormBorderStyle.FixedDialog;
			MaximizeBox = false; MinimizeBox = false;
			StartPosition = FormStartPosition.CenterScreen;
			ClientSize = new Size(420, 110);
			label.SetBounds(12, 12, 396, 20); label.Text = "Contacting server...";
			bar.SetBounds(12, 38, 396, 20); bar.Style = ProgressBarStyle.Marquee;
			cancel.SetBounds(333, 72, 75, 26); cancel.Text = "Cancel";
			cancel.Click += delegate { cancelled = true; if (client != null) client.CancelAsync(); };
			Controls.Add(label); Controls.Add(bar); Controls.Add(cancel);
			Shown += delegate { ThreadPool.QueueUserWorkItem(delegate { Work(); }); };
			FormClosing += delegate(object s, FormClosingEventArgs e)
			{
				if (!done) { e.Cancel = true; cancel.PerformClick(); }
			};
		}

		private void Work()
		{
			string tmpExtract = targetDir + ".tmp";
			try
			{
				ServicePointManager.SecurityProtocol = (SecurityProtocolType)3072; // TLS 1.2
				string expectedMd5;
				using (WebClient wc = new WebClient()) expectedMd5 = wc.DownloadString(checksumUrl).Trim().ToLowerInvariant();
				if (!Regex.IsMatch(expectedMd5, "^[0-9a-f]{32}$")) throw new Exception("Invalid checksum received from the server.");

				zipPath = Path.Combine(Path.GetTempPath(), "screept_jre_" + Guid.NewGuid().ToString("N") + ".zip");
				SetStatus("Downloading Java...", true);
				client = new WebClient();
				client.DownloadProgressChanged += delegate(object s, DownloadProgressChangedEventArgs e)
				{
					BeginInvoke((MethodInvoker)delegate
					{
						bar.Value = e.ProgressPercentage;
						label.Text = "Downloading Java... " + (e.BytesReceived / 1048576) + " / " + (e.TotalBytesToReceive / 1048576) + " MB";
					});
				};
				ManualResetEvent finished = new ManualResetEvent(false);
				Exception downloadError = null;
				client.DownloadFileCompleted += delegate(object s, System.ComponentModel.AsyncCompletedEventArgs e)
				{
					downloadError = e.Error; finished.Set();
				};
				client.DownloadFileAsync(new Uri(url), zipPath);
				finished.WaitOne();
				if (cancelled) throw new OperationCanceledException();
				if (downloadError != null) throw downloadError;

				SetStatus("Checking the download...", false);
				if (Md5Of(zipPath) != expectedMd5) throw new Exception("The downloaded file is corrupted (checksum mismatch). Try again.");

				SetStatus("Unpacking Java...", false);
				if (Directory.Exists(tmpExtract)) Directory.Delete(tmpExtract, true);
				ZipFile.ExtractToDirectory(zipPath, tmpExtract);
				string home = FindJavaHome(tmpExtract);
				if (home == null) throw new Exception("The downloaded archive does not contain a Java runtime.");
				if (Directory.Exists(targetDir)) Directory.Delete(targetDir, true);
				Directory.Move(home, targetDir);
				Success = true;
			}
			catch (OperationCanceledException) { }
			catch (Exception e)
			{
				if (!cancelled) ErrorMessage = "Cannot download Java automatically:\n" + e.Message +
					"\n\nInstall Java 8 or newer manually (for example Amazon Corretto 8: https://aws.amazon.com/corretto) and start ScreepT again.";
			}
			finally
			{
				try { if (zipPath != null && File.Exists(zipPath)) File.Delete(zipPath); } catch (Exception) { }
				try { if (Directory.Exists(tmpExtract)) Directory.Delete(tmpExtract, true); } catch (Exception) { }
				if (client != null) client.Dispose();
				done = true;
				BeginInvoke((MethodInvoker)delegate { Close(); });
			}
		}

		private void SetStatus(string text, bool progress)
		{
			Invoke((MethodInvoker)delegate
			{
				label.Text = text;
				bar.Style = progress ? ProgressBarStyle.Continuous : ProgressBarStyle.Marquee;
				cancel.Enabled = progress;
			});
		}

		private static string Md5Of(string path)
		{
			using (MD5 md5 = MD5.Create())
			using (FileStream fs = File.OpenRead(path))
			{
				StringBuilder sb = new StringBuilder();
				foreach (byte b in md5.ComputeHash(fs)) sb.Append(b.ToString("x2"));
				return sb.ToString();
			}
		}

		private static string FindJavaHome(string dir)
		{
			if (File.Exists(Path.Combine(dir, "bin", "javaw.exe"))) return dir;
			foreach (string sub in Directory.GetDirectories(dir))
			{
				string found = FindJavaHome(sub);
				if (found != null) return found;
			}
			return null;
		}
	}
}
