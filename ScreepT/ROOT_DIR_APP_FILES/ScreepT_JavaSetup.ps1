# Downloads a Java runtime into <ScreepT folder>\jre (no admin rights, nothing installed system wide). Called by ScreepT.bat.
# 64 bit Windows: Eclipse Temurin 21 JRE (SHA-256 from the Adoptium API). 32 bit Windows: Amazon Corretto 8 JRE (MD5).
param([Parameter(Mandatory = $true)][string]$Root)
$ErrorActionPreference = 'Stop'
[Net.ServicePointManager]::SecurityProtocol = 3072 # TLS 1.2
$Root = $Root.TrimEnd('\') + '\'
$wc = New-Object Net.WebClient
$wc.Headers['User-Agent'] = 'ScreepT-launcher'

if ([Environment]::Is64BitOperatingSystem) {
	$json = $wc.DownloadString('https://api.adoptium.net/v3/assets/latest/21/hotspot?architecture=x64&image_type=jre&os=windows')
	# the zip package: "checksum" comes before "link" inside the same object
	$m = [regex]::Match($json, '"checksum"\s*:\s*"([0-9a-fA-F]{64})"[^}]*?"link"\s*:\s*"([^"]+\.zip)"')
	if (-not $m.Success) { throw 'Unexpected answer from the Java download server.' }
	$hash = $m.Groups[1].Value.ToLower(); $url = $m.Groups[2].Value; $algorithm = 'SHA256'
} else {
	$file = 'amazon-corretto-8-x86-windows-jre.zip'
	$url = 'https://corretto.aws/downloads/latest/' + $file
	$hash = $wc.DownloadString('https://corretto.aws/downloads/latest_checksum/' + $file).Trim().ToLower(); $algorithm = 'MD5'
}

$zip = Join-Path $env:TEMP ('screept_jre_' + [guid]::NewGuid().ToString('N') + '.zip')
$tmp = $Root + 'jre.tmp'
try {
	$wc.DownloadFile($url, $zip)
	if ((Get-FileHash $zip -Algorithm $algorithm).Hash.ToLower() -ne $hash) { throw 'The downloaded file is corrupted (checksum mismatch).' }
	if (Test-Path $tmp) { Remove-Item $tmp -Recurse -Force }
	Expand-Archive $zip $tmp -Force
	$home_ = (Get-ChildItem $tmp -Recurse -Filter javaw.exe | Select-Object -First 1).Directory.Parent.FullName
	if (Test-Path ($Root + 'jre')) { Remove-Item ($Root + 'jre') -Recurse -Force }
	Move-Item $home_ ($Root + 'jre')
} finally {
	Remove-Item $zip -Force -ErrorAction SilentlyContinue
	Remove-Item $tmp -Recurse -Force -ErrorAction SilentlyContinue
}
