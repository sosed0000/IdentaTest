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
        const orderItem = document.createElement('li');
        orderItem.dataset.orderId = order.id;
        orderItem.textContent = `${order.description} `;

        const statusSpan = document.createElement('span');
        statusSpan.textContent = `(${order.status})`;
        statusSpan.className = 'status';
        orderItem.appendChild(statusSpan);

        const takeButton = document.createElement('button');
        takeButton.textContent = "Take Order";
        takeButton.addEventListener('click', () => takeOrder(order.id));
        takeButton.id = `takeButton_${order.id}`;
        orderItem.appendChild(takeButton);

        const completeButton = document.createElement('button');
        completeButton.textContent = "Complete";
        completeButton.addEventListener('click', () => completeOrder(order.id));
        completeButton.id = `completeButton_${order.id}`;
        orderItem.appendChild(completeButton);

        orderList.appendChild(orderItem);

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

    const orderItem = document.querySelector(`li[data-order-id="${orderId}"]`);
    if (orderItem) {
        const statusElement = orderItem.querySelector('.status');
        if (statusElement) {
            statusElement.textContent = `(${status})`;
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
