const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, () => {
    const orderForm = document.getElementById('orderForm');
    const orderDescriptionInput = document.getElementById('orderDescription');
    const dishesList = document.getElementById('dishesList');

    // Load dishes from the server and display them
    fetch('/rest/getDishes')
        .then(response => response.json())
        .then(dishes => {
            dishes.forEach(dish => {
                const dishContainer = document.createElement('div');
                dishContainer.className = 'dish';
                dishContainer.style.display = 'flex';

                const checkbox = document.createElement('input');
                checkbox.type = 'checkbox';
                checkbox.name = 'dishes';
                checkbox.value = dish.id;

                const label = document.createElement('label');
                label.textContent = dish.name;
                label.style.flex = '1';

                const quantityInput = document.createElement('input');
                quantityInput.type = 'number';
                quantityInput.name = 'dishQuantity';
                quantityInput.value = 1;
                quantityInput.min = 1;
                quantityInput.max = 10;
                quantityInput.style.width = '30px';
                quantityInput.style.textAlign = 'right';
                quantityInput.disabled = true;

                checkbox.addEventListener('change', () => {
                    if (checkbox.checked) {
                        quantityInput.removeAttribute('disabled');
                    } else {
                        quantityInput.setAttribute('disabled', true);
                        quantityInput.value = 1;
                    }
                });

                dishContainer.appendChild(checkbox);
                dishContainer.appendChild(label);
                dishContainer.appendChild(quantityInput);

                dishesList.appendChild(dishContainer);
            });
        })
        .catch(error => {
            console.error("Error fetching dishes:", error);
        });
    orderForm.addEventListener('submit', async (event) => {
        event.preventDefault();
        const description = orderDescriptionInput.value;

        const selectedDishes = Array.from(document.querySelectorAll('input[name="dishes"]:checked')).map(input => {
            const quantityInput = input.nextElementSibling.nextElementSibling;
            const quantity = parseInt(quantityInput.value);
            return {dishId: input.value, quantity: quantity};
        });

        const response = await fetch('/createOrder', {
            method: 'POST', headers: {
                'Content-Type': 'application/json',
            }, body: JSON.stringify({
                description, selectedDishes,
            }),
        });

        if (response.ok) {
            const order = await response.json();
            orderDescriptionInput.value = '';
            selectedDishes.forEach(dish => {
                const checkbox = document.querySelector(`input[name="dishes"][value="${dish.dishId}"]`);
                checkbox.checked = false;
                const quantityInput = checkbox.nextElementSibling.nextElementSibling;
                quantityInput.value = 1;
                quantityInput.setAttribute('disabled', true);
            });

            stompClient.send("/app/newOrder", {}, JSON.stringify(order));
        }
    });
});
