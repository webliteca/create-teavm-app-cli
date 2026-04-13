const { PubSub } = require('@google-cloud/pubsub');
const http = require('http');

const projectId = process.env.PUBSUB_PROJECT_ID || 'demo-project';
const pubsub = new PubSub({ projectId });

// Topic-to-endpoint mapping
// Add entries here as you create processor services
const subscriptions = [
    // { topic: 'my-topic', subscription: 'my-sub', endpoint: 'http://my-processor:8080/process' }
];

async function setupSubscriptions() {
    for (const sub of subscriptions) {
        try {
            const [topic] = await pubsub.createTopic(sub.topic).catch(() => [pubsub.topic(sub.topic)]);
            const [subscription] = await topic.createSubscription(sub.subscription).catch(() => [topic.subscription(sub.subscription)]);

            subscription.on('message', async (message) => {
                try {
                    const response = await fetch(sub.endpoint, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({
                            message: {
                                data: message.data.toString('base64'),
                                messageId: message.id,
                                attributes: message.attributes
                            }
                        })
                    });

                    if (response.ok) {
                        message.ack();
                    } else {
                        console.error(`Failed to deliver message to ${sub.endpoint}: ${response.status}`);
                        message.nack();
                    }
                } catch (err) {
                    console.error(`Error delivering message to ${sub.endpoint}:`, err);
                    message.nack();
                }
            });

            console.log(`Listening on ${sub.topic} -> ${sub.endpoint}`);
        } catch (err) {
            console.error(`Failed to set up subscription for ${sub.topic}:`, err);
        }
    }
}

setupSubscriptions().catch(console.error);

// Keep process alive
const server = http.createServer((req, res) => {
    res.writeHead(200);
    res.end('Pub/Sub bridge running');
});
server.listen(8086, () => console.log('Pub/Sub bridge health check on :8086'));
