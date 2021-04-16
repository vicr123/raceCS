self.addEventListener('push', (event) => {
    // Only post the notification if the window is not focused
    event.waitUntil(clients.matchAll({
        type: "window"
    }).then(function(clientList) {
        for (let i = 0; i < clientList.length; i++) {
            if (clientList[i].focused) return;
        }
        
        let data = event.data.json();
        event.waitUntil(self.registration.showNotification("AirCS Race Update", data));
    }));
});

self.addEventListener("notificationclick", (event) => {
    event.notification.close();

    // This looks to see if the current is already open and
    // focuses if it is
    event.waitUntil(clients.matchAll({
        type: "window"
    }).then(function(clientList) {
        for (let i = 0; i < clientList.length; i++) {
            return clientList[i].focus();
        }

        if (clients.openWindow) return clients.openWindow('/');
    }));
});