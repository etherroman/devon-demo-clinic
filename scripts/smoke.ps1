param([string]$BaseUrl = 'http://localhost:8081')
$ErrorActionPreference = 'Stop'
$BaseUrl = $BaseUrl.TrimEnd('/')

function Send-Json([string]$Path, [hashtable]$Payload) {
    $body = [Text.Encoding]::UTF8.GetBytes(($Payload | ConvertTo-Json))
    Invoke-RestMethod "$BaseUrl$Path" -Method Post -ContentType 'application/json' -Body $body
}

$owner = Send-Json '/api/owners' @{name="Smoke $([DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds())"; telephone='010-7777-0000'; address='Seoul'}
if ($owner.id -le 0) { throw 'Owner creation failed' }
$pet = Send-Json "/api/owners/$($owner.id)/pets" @{name='Smoke Nabi'; type='cat'; birthDate='2020-04-01'}
if ($pet.ownerId -ne $owner.id) { throw 'Pet owner mismatch' }
$path = "/api/owners/$($owner.id)/pets/$($pet.id)"
$visit = Send-Json "$path/visits" @{visitDate='2024-03-10'; description='Annual vaccination'}
if ($visit.petId -ne $pet.id) { throw 'Visit pet mismatch' }
$visits = @(Invoke-RestMethod "$BaseUrl$path/visits")
if ($visits.Count -ne 1) { throw 'Visit list mismatch' }
$result = Invoke-RestMethod "$BaseUrl/api/owners?name=Smoke&page=0&size=1"
if ($result.content.Count -ne 1 -or $result.totalElements -lt 1) { throw 'Owner pagination failed' }

try {
    Invoke-RestMethod "$BaseUrl$path" -Method Delete | Out-Null
    throw 'Expected delete to return 409'
} catch {
    if ($null -eq $_.Exception.Response -or [int]$_.Exception.Response.StatusCode -ne 409) { throw }
}
try {
    Send-Json '/api/owners' @{name=' '; telephone=''; address=''} | Out-Null
    throw 'Expected invalid owner to return 400'
} catch {
    if ($null -eq $_.Exception.Response -or [int]$_.Exception.Response.StatusCode -ne 400) { throw }
}
$stillPresent = Invoke-RestMethod "$BaseUrl$path"
if ($stillPresent.id -ne $pet.id) { throw 'Conflict unexpectedly removed pet' }
Write-Output "PASS owner=$($owner.id) pet=$($pet.id) visit=$($visit.id): creation, lists, pagination, 409 restrict, 400 validation"
