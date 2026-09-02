// The key is injected at runtime by Firebase Secret Manager
// (see defineSecret("FOOTBALL_DATA_API_KEY") in index.js).
// Never commit the real value here.
module.exports = {
    get API_KEY() {
        return process.env.FOOTBALL_DATA_API_KEY || "";
    }
};
