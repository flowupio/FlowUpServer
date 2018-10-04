package usecases;

import datasources.billing.StripeClient;
import datasources.billing.TaxamoClient;
import models.CreateSubscriptionRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import utils.WithFlowUpApplication;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static play.inject.Bindings.bind;

@RunWith(MockitoJUnitRunner.class)
public class CreateSubscriptionTest extends WithFlowUpApplication {

    private static final String BILLING_ID = "Billing id";
    private static final String TRANSACTION_KEY = "Transaction key";

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder()
                .overrides(bind(TaxamoClient.class).toInstance(taxamo))
                .overrides(bind(StripeClient.class).toInstance(stripe))
                .build();
    }

    @Mock
    private TaxamoClient taxamo;
    @Mock
    private StripeClient stripe;
    private CreateSubscription createSubscription;

    @Before
    public void setUp() {
        createSubscription = this.app.injector().instanceOf(CreateSubscription.class);
    }

    @Test
    public void whenTaxamoFailsToCreatePlaceholderSubscriptionStripeIsNotUsed() throws Exception {
        givenTaxamoSubscriptionPlaceholderIsNotCreated();

        boolean isSuccess = createSubscription.execute(anyRequest(), BILLING_ID).toCompletableFuture().get();

        assertFalse(isSuccess);
        verify(stripe, never()).createSubscription(any(), any());
    }

    @Test
    public void whenSubscriptionIsCreatedReturnSuccess() throws Exception {
        givenTaxamoSubscriptionPlaceholderIsCreated();
        givenStripeSubscriptionIsCreated();

        boolean isSuccess = createSubscription.execute(anyRequest(), BILLING_ID).toCompletableFuture().get();

        assertTrue(isSuccess);
    }

    private void givenTaxamoSubscriptionPlaceholderIsCreated() {
        when(taxamo.createPlaceholderTransaction(any(), anyString())).thenReturn(Optional.of(TRANSACTION_KEY));
    }

    private void givenTaxamoSubscriptionPlaceholderIsNotCreated() {
        when(taxamo.createPlaceholderTransaction(any(), anyString())).thenReturn(Optional.empty());
    }

    private void givenStripeSubscriptionIsCreated() {
        when(stripe.createSubscription(any(), anyString())).thenReturn(true);
    }

    private CreateSubscriptionRequest anyRequest() {
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        return request;
    }
}
