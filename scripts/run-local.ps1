$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
$mavenCommand = Get-Command mvn -ErrorAction SilentlyContinue
if ($mavenCommand) { $mavenPath = $mavenCommand.Source } else {
    $mavenPath = Join-Path (Get-Location) '.tools/apache-maven-3.9.9/bin/mvn.cmd'
    if (-not (Test-Path -LiteralPath $mavenPath)) {
        New-Item -ItemType Directory -Force '.tools' | Out-Null
        Invoke-WebRequest 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip' -OutFile '.tools/maven.zip'
        Expand-Archive '.tools/maven.zip' '.tools' -Force
    }
}
& $mavenPath clean package cargo:run
exit $LASTEXITCODE
