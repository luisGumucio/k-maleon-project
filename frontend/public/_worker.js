export default {
  async fetch(request, env) {
    const url = new URL(request.url);

    // Serve static assets directly
    if (url.pathname.startsWith("/assets/") || url.pathname.includes(".")) {
      return env.ASSETS.fetch(request);
    }

    // SPA fallback — serve index.html for all routes
    const indexUrl = new URL("/index.html", url.origin);
    return env.ASSETS.fetch(new Request(indexUrl, request));
  },
};
