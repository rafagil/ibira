console.log('Running in Dev mode');
const ws = new WebSocket('ws://servidor:8085/watch');
ws.onmessage = () => {
		window.location.reload();
}


