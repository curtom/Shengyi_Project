$url = "http://localhost:8090/fccapi/COMM_B2G_QueryAcceptanceBillPageList"
$body = '{"current":1,"size":10,"data":{}}'

$response = Invoke-RestMethod -Uri $url -Method Post -ContentType "application/json" -Body $body
Write-Host "Response:"
$response | ConvertTo-Json -Depth 10