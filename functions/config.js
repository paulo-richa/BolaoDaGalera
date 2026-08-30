// A chave é injetada em tempo de execução pelo Firebase Secret Manager
// (ver defineSecret("FOOTBALL_DATA_API_KEY") em index.js).
// Nunca commitar o valor real aqui.
module.exports = {
    get API_KEY() {
        return process.env.FOOTBALL_DATA_API_KEY || "";
    }
};
