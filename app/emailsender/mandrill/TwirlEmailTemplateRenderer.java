package emailsender.mandrill;

import emailsender.EmailTemplateRenderer;
import models.Application;
import org.jetbrains.annotations.NotNull;
import usecases.models.KeyStatCard;
import usecases.models.KeyStatDetail;
import usecases.models.KeyStatRow;
import usecases.models.StatCard;
import views.html.api.findbugs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class TwirlEmailTemplateRenderer implements EmailTemplateRenderer {

    private static final String HTTP_APP_FLOWUP_IO = "http://app.flowup.io";

    @Override
    public CompletionStage<String> findbugs(Application application, List<KeyStatCard> statCards) {
        List<KeyStatRow> rows = mapKeyStatCards(statCards.stream().map(KeyStatCard::getMain).collect(Collectors.toList()));
        List<KeyStatDetail> details = mapDetails(statCards);

        return CompletableFuture.supplyAsync(() -> findbugs.render(application, rows, details, HTTP_APP_FLOWUP_IO).body());
    }

    @NotNull
    private List<KeyStatDetail> mapDetails(List<KeyStatCard> statCards) {
        List<KeyStatDetail> details = new ArrayList<>();

        for (KeyStatCard card : statCards) {
            if (!card.getDetails().isEmpty()) {
                List<KeyStatRow> detailRows = mapKeyStatCards(card.getDetails());
                details.add(new KeyStatDetail(card.getDescription(), detailRows));
            }
        }
        return details;
    }

    @NotNull
    private List<KeyStatRow> mapKeyStatCards(List<StatCard> statCards) {
        List<KeyStatRow> rows = new ArrayList<>();
        for (int i = 0; i < statCards.size(); i += 2) {
            KeyStatRow pair = new KeyStatRow(statCards.get(i), i + 1 < statCards.size() ? statCards.get(i + 1) : null);
            rows.add(pair);
        }
        return rows;
    }
}
