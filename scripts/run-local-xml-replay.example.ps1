# Example local replay trigger.
# Configure application-local.properties before running:
# - artikos.source.mode=local-xml
# - artikos.source.local-xml-path=classpath:samples/ZSVIDA_Nom15965_v2.xml
# - artikos.confirm.enabled=false
# - artikos.result.enabled=false
# - procurement.client.enabled=true
# - procurement.integration.enabled=true
# - procurement.client.base-url=<PROCUREMENT_QA_OR_LOCAL_URL>

$body = @{
  profile = "GENERALES"
  dryRun = $false
} | ConvertTo-Json

Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8080/api/v1/nominas/batch/start" `
  -ContentType "application/json" `
  -Body $body
