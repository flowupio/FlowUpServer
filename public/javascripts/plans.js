Taxamo.initialize('public_test_9QC3z9tNxxPOTze4GJzd4GC9JsVJJvv5OxW5v2t2BIo');

var professionalPlanButton = document.getElementById("professional");
var businessPlanButton = document.getElementById("business");

professionalPlanButton.onclick = onProfessionalPlanClicked;
businessPlanButton.onclick = onBusinessPlanClicked;

function onProfessionalPlanClicked(src) {
    onPlanClicked("company_plan", src.srcElement.dataset.billingId);
}

function onBusinessPlanClicked(src) {
    onPlanClicked("enterprise_plan", src.srcElement.dataset.billingId);
}

function onPlanClicked(planId, billingId) {
    var transaction = { custom_id: billingId,
                        custom_fields: [
                            { key: 'taxed-plan-id', value: planId },
                            { key: 'untaxed-plan-id', value: planId + '_untaxed' } ],
                        billing_country_code: Taxamo.defaultTransaction.billing_country_code
                      };
    var metadata    = { allowed_payment_providers: 'braintree',
                        subscription_mode: true,
                        show_email: true,
                        show_buyer_name: true,
                        show_invoice_address: true,
                        finished_redirect_url: self.location.href};

    var checkout = new Taxamo.Checkout(transaction, metadata);
    checkout.overlay(function(data) {
        if (data.success) {
            setTimeout(location.reload.bind(location), 1000);
        }
    });
}