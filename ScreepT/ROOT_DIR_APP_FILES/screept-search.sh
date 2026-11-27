#!/bin/sh
# ScreepT search in files from the command line: the same engine as Search in files, at full speed and without windows.
# Usage: screept-search.sh [options] TEXT [PATH...]   (screept-search.sh --help for the options)
ROOT="$(cd "$(dirname "$0")" && pwd)"
JAVA=""
for candidate in "$ROOT/jre/bin/java" "$ROOT/jre/Contents/Home/bin/java" "$ROOT/Application/jre/bin/java" "${JAVA_HOME:+$JAVA_HOME/bin/java}"; do
	if [ -n "$candidate" ] && [ -x "$candidate" ]; then JAVA="$candidate"; break; fi
done
[ -z "$JAVA" ] && JAVA="$(command -v java)"
if [ -z "$JAVA" ]; then
	echo "screept-search: Java 8 or newer is needed: start ScreepT.sh once to get it." >&2
	exit 2
fi
JAR=""
for j in "$ROOT"/Application/ScreepT_*.jar; do
	case "$j" in *ScreepT_Launcher.jar) ;; *) [ -f "$j" ] && JAR="$j" ;; esac
done
if [ -z "$JAR" ]; then
	echo "screept-search: ScreepT jar not found in $ROOT/Application" >&2
	exit 2
fi
exec "$JAVA" -Dfile.encoding=UTF-8 -XX:+UseParallelGC -cp "$JAR:$ROOT/Application/Resources_ScreepT/lib/*" gui.texteditor.commons.filesearch.cli.SearchCli "$@"
