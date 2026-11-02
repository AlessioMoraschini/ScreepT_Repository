#!/bin/bash
# ScreepT starter (Linux/macOS). Finds Java 8 or newer; if missing, offers to download Java 21 (Eclipse Temurin JRE)
# into the "jre" folder next to this file (no root needed, nothing installed system wide).
# To test the download without removing Java: SCREEPT_FORCE_NO_JAVA=1 ./ScreepT.sh (delete "jre" to repeat).

cd "$(dirname "$0")" || exit 1
ROOT="$(pwd)"
JAVA_EXE=""

# prints the Java major version of $1 (8 for 1.8.x), nothing if $1 is not a working Java
java_major() {
	local out v major minor
	out="$("$1" -version 2>&1)" || return 1
	v="$(printf '%s\n' "$out" | sed -n 's/.*version "\([^"]*\)".*/\1/p' | head -n 1)"
	[ -n "$v" ] || return 1
	major="${v%%[._-]*}"
	if [ "$major" = "1" ]; then
		minor="${v#1.}"
		major="${minor%%[._-]*}"
	fi
	case "$major" in ''|*[!0-9]*) return 1 ;; esac
	echo "$major"
}

# sets JAVA_EXE to $1 when it is a Java 8 or newer
check_java() {
	[ -x "$1" ] || return 1
	local m
	m="$(java_major "$1")" || return 1
	[ "$m" -ge 8 ] || return 1
	JAVA_EXE="$1"
}

# local jre: Linux has bin/java directly, macOS keeps it in Contents/Home
find_java() {
	check_java "$ROOT/jre/bin/java" && return 0
	check_java "$ROOT/jre/Contents/Home/bin/java" && return 0
	[ "$SCREEPT_FORCE_NO_JAVA" = "1" ] && return 1
	[ -n "$JAVA_HOME" ] && check_java "$JAVA_HOME/bin/java" && return 0
	if [ "$(uname -s)" = "Darwin" ] && [ -x /usr/libexec/java_home ]; then
		local h
		h="$(/usr/libexec/java_home -v 1.8+ 2>/dev/null)" && [ -n "$h" ] && check_java "$h/bin/java" && return 0
	fi
	local p
	p="$(command -v java 2>/dev/null)" && [ -n "$p" ] && check_java "$p" && return 0
	return 1
}

ask_download() {
	local msg="ScreepT needs Java 8 or newer, but no compatible Java was found.
Download Java 21 (Eclipse Temurin JRE, free, about 50 MB) into:
$ROOT/jre
It is not installed system wide and needs no root rights."
	if [ -t 0 ]; then
		echo; echo "$msg"; read -r -p "Download it now? [y/N] " a
		case "$a" in y|Y|s|S) return 0 ;; *) return 1 ;; esac
	elif command -v zenity >/dev/null 2>&1; then
		zenity --question --title="ScreepT" --text="$msg" 2>/dev/null
	elif command -v kdialog >/dev/null 2>&1; then
		kdialog --title "ScreepT" --yesno "$msg" 2>/dev/null
	elif command -v osascript >/dev/null 2>&1; then
		osascript -e "display dialog \"$(printf '%s' "$msg" | tr '\n' ' ')\" buttons {\"Cancel\",\"Download\"} default button \"Download\" with title \"ScreepT\"" >/dev/null 2>&1
	else
		echo "$msg" >&2
		return 1
	fi
}

fail() {
	echo "$1" >&2
	command -v zenity >/dev/null 2>&1 && [ ! -t 0 ] && zenity --error --title="ScreepT" --text="$1" 2>/dev/null
	exit 1
}

download_java() {
	local os arch tmp sha got url piece os_api
	case "$(uname -s)" in Linux) os=linux ;; Darwin) os=macos ;; *) fail "Unsupported system for the automatic Java download." ;; esac
	case "$(uname -m)" in x86_64|amd64) arch=x64 ;; aarch64|arm64) arch=aarch64 ;; *) fail "Unsupported CPU ($(uname -m)) for the automatic Java download." ;; esac
	tmp="$(mktemp -d)" || fail "Cannot create a temporary folder."
	trap 'rm -rf "$tmp" "$ROOT/jre.tmp"' EXIT

	fetch() { if command -v curl >/dev/null 2>&1; then curl -fL --progress-bar -o "$2" "$1"; else wget -O "$2" "$1"; fi; }
	command -v curl >/dev/null 2>&1 || command -v wget >/dev/null 2>&1 || fail "curl or wget is needed to download Java."

	# Eclipse Temurin 21 JRE: link and SHA-256 come from the Adoptium API (os names there: linux, mac)
	[ "$os" = "macos" ] && os_api=mac || os_api=linux
	fetch "https://api.adoptium.net/v3/assets/latest/21/hotspot?architecture=$arch&image_type=jre&os=$os_api" "$tmp/api.json" >/dev/null 2>&1
	piece="$(tr -d '\n' < "$tmp/api.json" 2>/dev/null | grep -o '"checksum": *"[0-9a-f]\{64\}"[^}]*"link": *"[^"]*\.tar\.gz"' | head -n 1)"
	sha="$(printf '%s' "$piece" | grep -o '[0-9a-f]\{64\}' | head -n 1)"
	url="$(printf '%s' "$piece" | grep -o 'https://[^"]*\.tar\.gz' | head -n 1)"
	[ -n "$sha" ] && [ -n "$url" ] || fail "Cannot reach the Java download server."
	echo "Downloading Java (Eclipse Temurin 21)..."
	fetch "$url" "$tmp/jre.tar.gz" || fail "Java download failed."
	if command -v sha256sum >/dev/null 2>&1; then got="$(sha256sum "$tmp/jre.tar.gz" | cut -d" " -f1)"; else got="$(shasum -a 256 "$tmp/jre.tar.gz" | cut -d" " -f1)"; fi
	[ "$got" = "$sha" ] || fail "The downloaded Java is corrupted (checksum mismatch). Try again."

	rm -rf "$ROOT/jre.tmp"; mkdir -p "$ROOT/jre.tmp"
	tar -xzf "$tmp/jre.tar.gz" -C "$ROOT/jre.tmp" || fail "Cannot unpack Java."
	local home
	home="$(find "$ROOT/jre.tmp" -type f -path '*/bin/java' | head -n 1)"
	[ -n "$home" ] || fail "The downloaded archive does not contain Java."
	home="$(dirname "$(dirname "$home")")"
	rm -rf "$ROOT/jre"
	if [ "$os" = "macos" ]; then
		# keep the Contents/Home layout that find_java expects
		mkdir -p "$ROOT/jre/Contents" && mv "$home" "$ROOT/jre/Contents/Home"
	else
		mv "$home" "$ROOT/jre"
	fi
	rm -rf "$ROOT/jre.tmp" "$tmp"
}

if ! find_java; then
	ask_download || exit 1
	download_java
	find_java || fail "Java was downloaded but it was not found in $ROOT/jre"
fi

find ./Application -type f -exec chmod 755 {} \;
cd Application/ || exit 1

# the Java chosen here is also the one the ScreepT launcher finds in PATH
JAVA_HOME="$(dirname "$(dirname "$JAVA_EXE")")"
export JAVA_HOME
export PATH="$JAVA_HOME/bin:$PATH"

exec "$JAVA_EXE" -Xms128m -Dsun.java2d.uiScale=1 -Djava.library.path="./Resources_ScreepT/lib" -Dfile.encoding=UTF8 -jar ScreepT_Launcher.jar "$@"
