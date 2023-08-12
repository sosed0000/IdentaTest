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

                const checkbox = document.createElement('input');
                checkbox.type = 'checkbox';
                checkbox.name = 'dishes';
                checkbox.value = dish.id;

                const label = document.createElement('label');
                label.textContent = dish.name;

                const quantityInput = document.createElement('input');
                quantityInput.type = 'number';
                quantityInput.name = 'dishQuantity';
                quantityInput.value = 1;
                quantityInput.min = 1;
                quantityInput.max = 10;
                quantityInput.className = 'dish-quantity';
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

        const selectedDishes = Array.from(document.querySelectorAll('input[name="dishes"]:checked')).map(input => input.value);

        const response = await fetch('/createOrder', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                description,
                dishIds: selectedDishes,
            }),
        });

        if (response.ok) {
            const order = await response.json(); // Получаем объект заказа из ответа
            orderDescriptionInput.value = '';
            selectedDishes.forEach(dishId => {
                const checkbox = document.querySelector(`input[name="dishes"][value="${dishId}"]`);
                checkbox.checked = false;
            });

            // Не отправляем новый объект, используем полученный объект
            stompClient.send("/app/newOrder", {}, JSON.stringify(order));
        }
    });

});
