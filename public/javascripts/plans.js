var plansDiv = document.getElementById("plans");
var professionalPlanButton = document.getElementById("professional");
var businessPlanButton = document.getElementById("business");
var subscribeButton = document.getElementById("subscribe-button");

var stripeKey = plansDiv.dataset.stripeKey;

professionalPlanButton.onclick = onProfessionalPlanClicked;
businessPlanButton.onclick = onBusinessPlanClicked;

function onProfessionalPlanClicked(event) {
    onPlanClicked("ProPlan");
    event.preventDefault();
}

function onBusinessPlanClicked(event) {
    onPlanClicked("BizPlan");
    event.preventDefault();
}

function onPlanClicked(planId) {
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
                quantity: 100
            }, function(data) {});
        }
    });

    handler.open({
        name: 'FlowUp',
        description: '100 x 1000 devices',
        zipCode: true,
        currency: 'USD',
        billingAddress: true,
        amount: 100 * 1699
    });
}

window.addEventListener('popstate', function() {
    handler.close();
});