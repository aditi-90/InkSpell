param([string]$MySqlBinary = 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqld.exe')
$ErrorActionPreference='Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
$dataPath = Join-Path $projectRoot '.tools/mysql-data'
if(-not (Test-Path -LiteralPath $MySqlBinary)){throw 'MySQL binary not found. Supply -MySqlBinary with the installed mysqld.exe path.'}
$probe = [System.Net.Sockets.TcpClient]::new()
try {$probe.Connect('127.0.0.1',3307);Write-Host 'A server is already listening on 3307; not starting another.';return} catch {} finally {$probe.Dispose()}
$firstStart = -not (Test-Path -LiteralPath $dataPath)
$localConfig = Join-Path $projectRoot 'config/inkspell.properties'
$initialSql = Join-Path $projectRoot '.tools/mysql-first-start.sql'
if($firstStart){
 if(Test-Path -LiteralPath $localConfig){throw 'Local configuration already exists. Configure your own MySQL server or preserve that file before initializing a project database.'}
 New-Item -ItemType Directory -Force -Path $dataPath | Out-Null
 & $MySqlBinary --no-defaults --initialize-insecure "--datadir=$dataPath" --console 2> (Join-Path $projectRoot '.tools/mysql-init.log')
 if($LASTEXITCODE -ne 0){throw 'MySQL initialization failed; see .tools/mysql-init.log.'}
 $appPassword=[Convert]::ToHexString([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(24))
 $rootPassword=[Convert]::ToHexString([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(24))
 @"
ALTER USER 'root'@'localhost' IDENTIFIED BY '$rootPassword';
CREATE DATABASE IF NOT EXISTS inkspell_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'inkspell_local'@'localhost' IDENTIFIED BY '$appPassword';
GRANT ALL PRIVILEGES ON inkspell_db.* TO 'inkspell_local'@'localhost';
"@ | Set-Content -LiteralPath $initialSql -Encoding ascii
 @"
DB_URL=jdbc:mysql://localhost:3307/inkspell_db?serverTimezone=UTC
DB_SETUP_URL=jdbc:mysql://localhost:3307/?serverTimezone=UTC
DB_USER=inkspell_local
DB_PASSWORD=$appPassword
GEMINI_MODEL=gemini-2.5-flash
"@ | Set-Content -LiteralPath $localConfig -Encoding ascii
}
$mysqlArguments=@('--no-defaults',"--datadir=`"$dataPath`"",'--port=3307','--bind-address=127.0.0.1','--mysqlx=OFF','--innodb-buffer-pool-size=128M','--console')
if($firstStart){$mysqlArguments+="--init-file=`"$initialSql`""}
$databaseProcess=Start-Process -FilePath $MySqlBinary -ArgumentList $mysqlArguments -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $projectRoot '.tools/mysql-out.log') -RedirectStandardError (Join-Path $projectRoot '.tools/mysql-error.log')
$databaseProcess.Id | Set-Content (Join-Path $projectRoot '.tools/mysql.pid')
Write-Host "Project MySQL starting on 127.0.0.1:3307 (PID $($databaseProcess.Id)). Inspect .tools/mysql-error.log for readiness."
