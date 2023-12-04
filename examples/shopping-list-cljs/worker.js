const requestChannel = new BroadcastChannel("request");
const responseChannel = new BroadcastChannel("response");

const log = s => console.log(`[ServiceWorker] ${s}`);

const parseUrl = async (request) => {
		const { url, headers } = request;
		const path = `/${url.split('/').splice(3).join('/')}`;
		const [uri, query] = path.split('?');
		const body = request.body? await new Response(request.body).text() : null;
		return {
				url,
				path,
				uri,
				query: query && `?${query}`,
				body,
				headers: Object.fromEntries(headers),
		}
}

self.addEventListener('fetch', (event) => {
		event.respondWith(new Promise(async (resolve, reject) => {
				const parsed = await parseUrl(event.request);
				if (parsed.uri === '/list'
						|| parsed.uri.startsWith('/store-updates/')
						|| parsed.uri.startsWith('/dispatcher/')) {
						requestChannel.postMessage(parsed);
						responseChannel.onmessage = (e) => {
								resolve(new Response(e.data.body, {headers: e.data.headers, status: e.data.status}));
						}
				} else {
						resolve(fetch(event.request));
				}
		}));
});


