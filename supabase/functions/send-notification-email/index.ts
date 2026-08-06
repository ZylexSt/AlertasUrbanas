type EmailRequest = {
  toEmail?: string
  toName?: string
  subject?: string
  title?: string
  message?: string
  reportType?: string
  status?: string
  rejectionReason?: string
}

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
}

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders })
  }

  if (req.method !== "POST") {
    return jsonResponse({ error: "Metodo no permitido" }, 405)
  }

  const brevoApiKey = Deno.env.get("BREVO_API_KEY")
  const senderEmail = Deno.env.get("BREVO_SENDER_EMAIL")
  const senderName = Deno.env.get("BREVO_SENDER_NAME") ?? "GeoNav"

  if (!brevoApiKey || !senderEmail) {
    return jsonResponse({ error: "Faltan secretos de Brevo" }, 500)
  }

  let body: EmailRequest
  try {
    body = await req.json()
  } catch (_error) {
    return jsonResponse({ error: "JSON invalido" }, 400)
  }

  const toEmail = body.toEmail?.trim()
  if (!toEmail) {
    return jsonResponse({ error: "toEmail es requerido" }, 400)
  }

  const subject = body.subject?.trim() || buildSubject(body)
  const htmlContent = buildHtmlContent(body)
  const textContent = buildTextContent(body)

  const brevoResponse = await fetch("https://api.brevo.com/v3/smtp/email", {
    method: "POST",
    headers: {
      "accept": "application/json",
      "api-key": brevoApiKey,
      "content-type": "application/json",
    },
    body: JSON.stringify({
      sender: {
        name: senderName,
        email: senderEmail,
      },
      to: [
        {
          email: toEmail,
          name: body.toName?.trim() || undefined,
        },
      ],
      subject,
      htmlContent,
      textContent,
    }),
  })

  const responseText = await brevoResponse.text()

  await saveEmailLog({
    toEmail,
    toName: body.toName?.trim() || "",
    subject,
    reportType: body.reportType?.trim() || "",
    status: body.status?.trim() || "",
    rejectionReason: body.rejectionReason?.trim() || "",
    brevoStatus: brevoResponse.status,
    success: brevoResponse.ok,
    errorMessage: brevoResponse.ok ? "" : responseText,
  })

  if (!brevoResponse.ok) {
    return jsonResponse(
      {
        error: "Brevo rechazo el envio",
        status: brevoResponse.status,
        details: responseText,
      },
      502,
    )
  }

  return jsonResponse({ ok: true, brevo: safeJson(responseText) }, 200)
})

async function saveEmailLog(log: {
  toEmail: string
  toName: string
  subject: string
  reportType: string
  status: string
  rejectionReason: string
  brevoStatus: number
  success: boolean
  errorMessage: string
}) {
  const supabaseUrl = Deno.env.get("SUPABASE_URL")
  const serviceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")

  if (!supabaseUrl || !serviceRoleKey) return

  await fetch(`${supabaseUrl}/rest/v1/email_logs`, {
    method: "POST",
    headers: {
      "apikey": serviceRoleKey,
      "Authorization": `Bearer ${serviceRoleKey}`,
      "Content-Type": "application/json",
      "Prefer": "return=minimal",
    },
    body: JSON.stringify({
      to_email: log.toEmail,
      to_name: log.toName || null,
      subject: log.subject,
      report_type: log.reportType || null,
      report_status: log.status || null,
      rejection_reason: log.rejectionReason || null,
      brevo_status: log.brevoStatus,
      success: log.success,
      error_message: log.errorMessage || null,
    }),
  })
}

function buildSubject(body: EmailRequest): string {
  if (body.status === "approved") return "Tu reporte fue validado"
  if (body.status === "rejected") return "Tu reporte fue rechazado"
  return body.title?.trim() || "Actualizacion de GeoNav"
}

function buildTextContent(body: EmailRequest): string {
  const title = body.title?.trim() || buildSubject(body)
  const message = body.message?.trim() || defaultMessage(body)
  return `${title}\n\n${message}`
}

function buildHtmlContent(body: EmailRequest): string {
  const title = escapeHtml(body.title?.trim() || buildSubject(body))
  const message = escapeHtml(body.message?.trim() || defaultMessage(body))

  return `
    <html>
      <body style="font-family: Arial, sans-serif; background: #f4f1ed; padding: 24px; color: #26302f;">
        <div style="max-width: 560px; margin: 0 auto; background: #ffffff; border-radius: 14px; padding: 24px;">
          <h1 style="font-size: 22px; margin: 0 0 12px;">${title}</h1>
          <p style="font-size: 15px; line-height: 1.5; margin: 0;">${message}</p>
          <p style="font-size: 12px; color: #697370; margin-top: 22px;">GeoNav</p>
        </div>
      </body>
    </html>
  `
}

function defaultMessage(body: EmailRequest): string {
  const reportType = body.reportType?.trim() || "tu reporte"

  if (body.status === "approved") {
    return `Tu reporte de ${reportType} fue validado por administracion.`
  }

  if (body.status === "rejected") {
    const reason = body.rejectionReason?.trim() || "No se especifico un motivo."
    return `Tu reporte de ${reportType} fue rechazado. Motivo: ${reason}`
  }

  return body.message?.trim() || "Tienes una nueva actualizacion en GeoNav."
}

function jsonResponse(payload: unknown, status: number): Response {
  return new Response(JSON.stringify(payload), {
    status,
    headers: {
      ...corsHeaders,
      "content-type": "application/json; charset=utf-8",
    },
  })
}

function safeJson(value: string): unknown {
  try {
    return JSON.parse(value)
  } catch (_error) {
    return value
  }
}

function escapeHtml(value: string): string {
  return value
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;")
}
