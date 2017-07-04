var plansDiv = document.getElementById("plans");
var professionalPlanButton = document.getElementById("professional");
var businessPlanButton = document.getElementById("business");
var subscribeButton = document.getElementById("subscribe-button");
var professionalQuantityInput = document.getElementById("professional-quantity");
var businessQuantityInput = document.getElementById("business-quantity");

var stripeKey = plansDiv.dataset.stripeKey;

professionalPlanButton.onclick = onProfessionalPlanClicked;
businessPlanButton.onclick = onBusinessPlanClicked;
professionalQuantityInput.oninput = onPlanInputChange;
businessQuantityInput.oninput = onPlanInputChange;

function onProfessionalPlanClicked(event) {
    onPlanClicked("ProPlan", 1700, professionalQuantityInput.value);
    event.preventDefault();
}

function onBusinessPlanClicked(event) {
    onPlanClicked("BizPlan", 1200, businessQuantityInput.value);
    event.preventDefault();
}

function onPlanInputChange(event) {
    var value = parseInt(event.target.value);
    if (value === NaN || value  < 0) {
        event.target.value = event.target.dataset["previous"]
    }
    event.target.dataset["previous"] = event.target.value;

    if (professionalQuantityInput.value < 1 || professionalQuantityInput.value > 100) {
        professionalPlanButton.setAttribute("disabled", "disabled");
    } else {
        professionalPlanButton.removeAttribute("disabled");
    }

    if (businessQuantityInput.value < 101) {
        businessPlanButton.setAttribute("disabled", "disabled");
    } else {
        businessPlanButton.removeAttribute("disabled");
    }
}

function onPlanClicked(planId, pricePerQuantity, quantity) {
    var handler = StripeCheckout.configure({
        key: stripeKey,
        image: '/assets/images/flowup_stripe.png',
        locale: 'auto',
        token: function(token) {
            $.post("create-subscription", {
                email: token.email,
                token: token.id,
                plan: planId,
                country: token.card.country,
                buyerIp: token.client_ip,
                quantity: quantity
            }, function(data) {});
        }
    });

    handler.open({
        name: 'FlowUp',
        description: quantity + ' x 1000 devices',
        zipCode: true,
        currency: 'USD',
        billingAddress: true,
        amount: quantity * pricePerQuantity
    });
}

window.addEventListener('popstate', function() {
    handler.close();
});