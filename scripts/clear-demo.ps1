# Uses the Java JDBC utility, never places database passwords in command arguments.
$ErrorActionPreference='Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
& .\.tools\apache-maven-3.9.9\bin\mvn.cmd -q exec:java '-Dexec.mainClass=com.inkspell.util.ClearDemo'
exit $LASTEXITCODE
