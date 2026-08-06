# Supabase Edge Function: send-notification-email

Esta funcion envia correos transaccionales con Brevo sin exponer la API key en Android.

## Secretos requeridos en Supabase

- `BREVO_API_KEY`: API key de Brevo.
- `BREVO_SENDER_EMAIL`: correo remitente verificado en Brevo.
- `BREVO_SENDER_NAME`: nombre visible del remitente, opcional.

## Deploy

```powershell
supabase login
supabase link --project-ref TU_PROJECT_REF
supabase secrets set BREVO_API_KEY=TU_API_KEY BREVO_SENDER_EMAIL=correo@dominio.com BREVO_SENDER_NAME="GeoNav"
supabase functions deploy send-notification-email
```

## Prueba

```powershell
curl -X POST "https://TU_PROJECT_REF.supabase.co/functions/v1/send-notification-email" `
  -H "Content-Type: application/json" `
  -H "apikey: TU_SUPABASE_ANON_KEY" `
  -d '{"toEmail":"usuario@correo.com","toName":"Usuario","status":"approved","reportType":"Bache"}'
```

## Nota de seguridad

Esta primera version deja `verify_jwt = false` para simplificar pruebas desde Android/Firebase. Para produccion conviene proteger la funcion verificando Firebase ID tokens o moviendo el disparo del correo a un backend controlado.
