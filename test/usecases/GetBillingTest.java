package usecases;

import com.taxamo.client.common.ApiException;
import com.taxamo.client.model.InvoiceAddress;
import com.taxamo.client.model.ListTransactionsOut;
import com.taxamo.client.model.Transactions;
import datasources.taxamo.TaxamoClient;
import models.Organization;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import usecases.models.Billing;
import utils.WithFlowUpApplication;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static play.inject.Bindings.bind;

@RunWith(MockitoJUnitRunner.class)
public class GetBillingTest extends WithFlowUpApplication {

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder()
                .overrides(bind(TaxamoClient.class).toInstance(taxamoClient))
                .build();
    }

    private static final String NO_BILLING_ID = null;
    private static final String ANY_BILLING_ID = "something";

    @Mock
    private TaxamoClient taxamoClient;
    private GetBilling getBilling;

    @Before
    public void setUp() {
        getBilling = this.app.injector().instanceOf(GetBilling.class);
    }

    @Test
    public void whenOrganizationHasNoBillingIdThenBillingIsNull() throws Exception {
        Organization organization = givenAnOrganizationWithBillingId(NO_BILLING_ID);

        Billing billing = getBilling.execute(organization).toCompletableFuture().get();

        assertNull(billing);
    }

    @Test
    public void whenOrganizationHasValidBillingIdThenBillingHasTransactions() throws Exception {
        givenTaxamoClientReturnsValidTransaction(ANY_BILLING_ID);
        Organization organization = givenAnOrganizationWithBillingId(ANY_BILLING_ID);

        Billing billing = getBilling.execute(organization).toCompletableFuture().get();

        assertBillingIsCorrect(billing);
    }

    @Test
    public void whenTaxamoClientFailsThenBillingIsNull() throws Exception {
        givenTaxamoClientFails(ANY_BILLING_ID);
        Organization organization = givenAnOrganizationWithBillingId(ANY_BILLING_ID);

        Billing billing = getBilling.execute(organization).toCompletableFuture().get();

        assertNull(billing);
    }

    private Organization givenAnOrganizationWithBillingId(String billingId) {
        Organization organization = new Organization();
        organization.setBillingId(billingId);
        return organization;
    }

    private void givenTaxamoClientFails(String billingId) throws Exception {
        when(taxamoClient.getAllTransactions(billingId)).thenThrow(new ApiException());
    }

    private void givenTaxamoClientReturnsValidTransaction(String billingId) throws Exception {
        ListTransactionsOut transactionsOut = new ListTransactionsOut();
        Transactions transaction = new Transactions();
        transaction.setKey("Key");
        transaction.setCreateTimestamp("2017-01-04T11:51:19Z");
        transaction.setCurrencyCode("EUR");
        transaction.setTotalAmount(BigDecimal.valueOf(150));
        transaction.setBuyerName("Buyer name");
        transaction.setBuyerEmail("Buyeremail@asd.com");
        transaction.setInvoicePlace("Invoice place");
        InvoiceAddress invoiceAddress = new InvoiceAddress();
        invoiceAddress.setPostalCode("Postal code");
        invoiceAddress.setCity("City");
        transaction.setInvoiceAddress(invoiceAddress);
        transaction.setBillingCountryCode("ES");
        transaction.setBuyerCreditCardPrefix("12345");
        transactionsOut.setTransactions(Collections.singletonList(transaction));
        when(taxamoClient.getAllTransactions(billingId)).thenReturn(transactionsOut);
    }

    private void assertBillingIsCorrect(Billing billing) {
        assertEquals("Buyer name", billing.getFullName());
        assertEquals("Buyeremail@asd.com", billing.getEmail());
        assertEquals("1234 5*** **** ****", billing.getCardNumber());
        assertEquals(1, billing.getTransactions().size());
    }
}
