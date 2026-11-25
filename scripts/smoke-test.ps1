param([string]$BaseUrl = 'http://localhost:8080')
$ErrorActionPreference='Stop'
$failures=0
foreach($path in @('/','/discover','/discover?q=Hobbit','/discover?genre=Fantasy','/discover?sort=popular','/discover?page=2','/login','/register','/book?id=1','/css/app.css','/js/app.js')) {
 try { $r=Invoke-WebRequest "$BaseUrl$path"; if($r.StatusCode -eq 200){Write-Host "PASS $path"}else{throw "HTTP $($r.StatusCode)"} }
 catch {Write-Host "FAIL $path : $($_.Exception.Message)";$failures++}
}
foreach($path in @('/dashboard','/library','/bookmarks','/preferences','/profile','/assistant','/admin')) {
 try {$r=Invoke-WebRequest "$BaseUrl$path";if($r.BaseResponse.ResponseUri.AbsolutePath -eq '/login' -or $r.Content -match 'Enter the portal'){Write-Host "PASS protected $path"}else{throw 'Expected login'}}
 catch{Write-Host "FAIL protected $path";$failures++}
}
try{Invoke-WebRequest "$BaseUrl/login" -Method Post -Body @{email='csrf@example.com';password='Invalid123'} | Out-Null;Write-Host 'FAIL missing CSRF';$failures++}catch{if([int]$_.Exception.Response.StatusCode -eq 403){Write-Host 'PASS missing CSRF blocked'}else{Write-Host 'FAIL CSRF response';$failures++}}
Write-Host "$failures failures"
exit ([int]($failures -gt 0))
