const { URL } = require('url');

module.exports = async (req, res) => {
  const backendUrl = process.env.BACKEND_URL;

  // Friendly setup helper if BACKEND_URL is not yet configured in Vercel
  if (!backendUrl) {
    res.setHeader('Content-Type', 'text/html; charset=utf-8');
    return res.status(200).send(`
      <!DOCTYPE html>
      <html lang="en">
      <head>
        <meta charset="UTF-8">
        <title>InkSpell Gateway · Deployment Status</title>
        <style>
          body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #101115; color: #e9dec9; padding: 40px 20px; text-align: center; }
          .card { max-width: 640px; margin: 40px auto; background: #18202b; border: 1px solid #d6ae6340; border-radius: 16px; padding: 36px; box-shadow: 0 10px 40px rgba(0,0,0,0.6); }
          h1 { color: #d6ae63; font-size: 28px; margin-bottom: 12px; }
          p { color: #a79f91; line-height: 1.6; font-size: 16px; }
          .code-box { background: #0c0e13; border: 1px solid #333; border-radius: 8px; padding: 14px; text-align: left; font-family: monospace; color: #8cdcfe; font-size: 14px; margin: 20px 0; overflow-x: auto; }
          .step { text-align: left; margin: 16px 0; color: #ddd; }
          .step strong { color: #d6ae63; }
        </style>
      </head>
      <body>
        <div class="card">
          <h1>✦ InkSpell Vercel Gateway Active ✦</h1>
          <p>Your frontend entrypoint is successfully deployed on Vercel!</p>
          <div class="step"><strong>Next Step:</strong> Connect your Vercel frontend to your Java backend service.</div>
          <div class="code-box">
            Vercel Dashboard &gt; Project Settings &gt; Environment Variables<br>
            Name: <strong>BACKEND_URL</strong><br>
            Value: <strong>https://your-inkspell-backend.onrender.com</strong> (or Railway URL)
          </div>
          <p>Once saved, redeploy or refresh this page to connect to your live reading sanctuary.</p>
        </div>
      </body>
      </html>
    `);
  }

  try {
    const rawUrl = req.url || '/';
    const targetBase = backendUrl.replace(/\\/+$/, '');
    const targetUrl = new URL(rawUrl, targetBase);

    // Filter incoming hop-by-hop headers
    const forwardedHeaders = {};
    for (const [key, value] of Object.entries(req.headers)) {
      const lower = key.toLowerCase();
      if (!['host', 'connection', 'keep-alive', 'transfer-encoding'].includes(lower)) {
        forwardedHeaders[key] = value;
      }
    }

    forwardedHeaders['x-forwarded-host'] = req.headers['host'] || '';
    forwardedHeaders['x-forwarded-proto'] = 'https';

    // Buffer incoming body for POST/PUT/DELETE
    let body = undefined;
    if (['POST', 'PUT', 'PATCH', 'DELETE'].includes(req.method)) {
      body = await new Promise((resolve) => {
        const chunks = [];
        req.on('data', (chunk) => chunks.push(chunk));
        req.on('end', () => resolve(Buffer.concat(chunks)));
      });
    }

    const response = await fetch(targetUrl.toString(), {
      method: req.method,
      headers: forwardedHeaders,
      body,
      redirect: 'manual'
    });

    // Forward status code
    res.status(response.status);

    // Forward response headers
    response.headers.forEach((value, key) => {
      const lower = key.toLowerCase();
      if (lower === 'set-cookie') {
        // Handle set-cookie headers properly
        const rawCookies = response.headers.getSetCookie ? response.headers.getSetCookie() : [value];
        res.setHeader('Set-Cookie', rawCookies);
      } else if (!['transfer-encoding', 'connection'].includes(lower)) {
        res.setHeader(key, value);
      }
    });

    const responseBuffer = Buffer.from(await response.arrayBuffer());
    res.send(responseBuffer);
  } catch (error) {
    console.error('InkSpell Gateway Proxy Error:', error);
    res.status(502).json({
      error: 'The connection to the InkSpell backend sanctuary was interrupted.',
      details: error.message
    });
  }
};
