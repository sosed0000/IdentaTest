const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, () => {
    stompClient.subscribe('/topic/orders', (order) => {
        console.log("Received new order:", order.body);
        const newOrder = JSON.parse(order.body);
        addOrderToList(newOrder);
    });

    stompClient.subscribe('/topic/orderStatusUpdate', (order) => {
        console.log("Received order status update:", order.body);
        const updatedOrder = JSON.parse(order.body);
        updateOrderStatus(updatedOrder.id, updatedOrder.status);
    });

    fetchOrders();
});

function addOrderToList(order) {
    if (order.status !== 'COMPLETED') {
        const orderList = document.getElementById('orderList');
        const row = orderList.insertRow();
        row.dataset.orderId = order.id;

        const orderIdCell = row.insertCell();
        orderIdCell.textContent = order.id;

        const orderDescriptionCell = row.insertCell();
        orderDescriptionCell.textContent = order.description;

        const statusCell = row.insertCell();
        const statusSpan = document.createElement('span');
        statusSpan.textContent = order.status;
        statusSpan.className = 'status';
        statusCell.appendChild(statusSpan);

        const actionCell = row.insertCell();
        const takeButton = document.createElement('button');
        takeButton.textContent = "Take Order";
        takeButton.addEventListener('click', () => takeOrder(order.id));
        takeButton.id = `takeButton_${order.id}`;
        actionCell.appendChild(takeButton);

        const completeButton = document.createElement('button');
        completeButton.textContent = "Complete";
        completeButton.addEventListener('click', () => completeOrder(order.id));
        completeButton.id = `completeButton_${order.id}`;
        actionCell.appendChild(completeButton);

        updateButtonStates(order.id, order.status);
    }
}


function fetchOrders() {
    fetch('/rest/getOrders')
        .then(response => response.json())
        .then(orders => {
            const orderList = document.getElementById('orderList');
            orderList.innerHTML = ''; // Clear the list before adding new orders
            orders.forEach(order => {
                addOrderToList(order);
            });
        })
        .catch(error => {
            console.error("Error fetching orders:", error);
        });
}

function takeOrder(orderId) {
    const takeButton = document.getElementById(`takeButton_${orderId}`);
    takeButton.disabled = true;
    stompClient.send(`/app/takeOrder/${orderId}`, {});
    updateOrderStatus(orderId, 'IN_PROGRESS');
}

function completeOrder(orderId) {
    stompClient.send(`/app/completeOrder/${orderId}`, {});
    // updateOrderStatus(orderId, 'COMPLETED');
}

function updateOrderStatus(orderId, status) {
    const takeButton = document.getElementById(`takeButton_${orderId}`);
    const completeButton = document.getElementById(`completeButton_${orderId}`);

    if (status === 'IN_PROGRESS') {
        takeButton.disabled = true;
        completeButton.disabled = false;
    } else if (status === 'COMPLETED') {
        takeButton.disabled = true;
        completeButton.disabled = true;
    }

    // Находим строку (row) с соответствующим orderId
    const row = document.querySelector(`tr[data-order-id="${orderId}"]`);
    if (row) {
        const statusElement = row.querySelector('.status');
        if (statusElement) {
            statusElement.textContent = status; // Обновляем текст статуса
        }
    }
}

function updateButtonStates(orderId, status) {
    const takeButton = document.getElementById(`takeButton_${orderId}`);
    const completeButton = document.getElementById(`completeButton_${orderId}`);

    if (status === 'OPEN') {
        takeButton.disabled = false;
        completeButton.disabled = true;
    } else if (status === 'IN_PROGRESS') {
        takeButton.disabled = true;
        completeButton.disabled = false;
    } else if (status === 'COMPLETED') {
        takeButton.disabled = true;
        completeButton.disabled = true;
    }
}
