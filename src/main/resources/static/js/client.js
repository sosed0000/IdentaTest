const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, () => {
    const orderForm = document.getElementById('orderForm');
    const orderDescriptionInput = document.getElementById('orderDescription');

    orderForm.addEventListener('submit', (event) => {
        event.preventDefault();
        const description = orderDescriptionInput.value;

        if (description.trim() !== '') {
            stompClient.send("/app/newOrder", {}, description);
            orderDescriptionInput.value = '';
        }
    });
});
