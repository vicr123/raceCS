const webpush = require('web-push');
const fs = require('fs');

const vapidKeys = webpush.generateVAPIDKeys();

console.log("VAPID Keys:");
console.log(`Private Key: ${vapidKeys.privateKey}`);
console.log(`Public Key: ${vapidKeys.publicKey}`);

fs.writeFileSync("vapid-privkey", vapidKeys.privateKey);
fs.writeFileSync("vapid-pubkey", vapidKeys.publicKey);
console.log("The keys were written");