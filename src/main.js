import express from 'express';
import { Actor } from 'apify';
import gplay from 'google-play-scraper';

const app = express();
const PORT = process.env.APIFY_CONTAINER_PORT || 8800;

let playStoreList = [];

const fetchData = async () => {
    try {
        const result = await gplay.list({
            category: gplay.category.GAME_CARD,
            collection: gplay.collection.TOP_FREE,
            num: 2,
        });
        playStoreList = result;
    } catch (error) {
        console.error('Error fetching data from Google Play:', error);
    }
};

// Initialize the actor and fetch data when the server starts
Actor.init()
    .then(fetchData)
    .catch(err => console.error('Error initializing actor:', err));

// Endpoint to get the playStoreList
app.get('/', async (req, res) => {
    // Ensure the data is up-to-date before responding
    await fetchData();
    res.json({ playStoreList });
});
 
// Start the Express server
app.listen(PORT, () => {
    console.log(`Server is running on port ${PORT}`);
});
