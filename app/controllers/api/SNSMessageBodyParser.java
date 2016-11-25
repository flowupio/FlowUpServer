package controllers.api;

import akka.util.ByteString;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.codec.binary.Base64;
import play.Logger;
import play.libs.F;
import play.libs.streams.Accumulator;
import play.libs.ws.WSClient;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Inject;
import java.security.Signature;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;


class SNSMessageBodyParser implements BodyParser<SNSMessage> {

    private static final String NOTIFICATION = "Notification";
    private static final String SUBSCRIPTION_CONFIRMATION = "SubscriptionConfirmation";
    private static final String UNSUBSCRIBE_CONFIRMATION = "UnsubscribeConfirmation";
    private static final String X_AMZ_SNS_MESSAGE_TYPE = "x-amz-sns-message-type";
    private static final String SIGNATURE_VERSION_1 = "1";

    private final TolerantJson jsonParser;
    private final Executor executor;
    private final WSClient wsClient;
    private final IsMessageSignatureValid isMessageSignatureValid;

    @Inject
    public SNSMessageBodyParser(TolerantJson jsonParser, Executor executor, WSClient wsClient, IsMessageSignatureValid isMessageSignatureValid) {
        this.jsonParser = jsonParser;
        this.executor = executor;
        this.wsClient = wsClient;
        this.isMessageSignatureValid = isMessageSignatureValid;
    }

    @Override
    public Accumulator<ByteString, F.Either<Result, SNSMessage>> apply(Http.RequestHeader request) {
        String messageType = request.getHeader(X_AMZ_SNS_MESSAGE_TYPE);
        if (messageType == null) {
            return Accumulator.done(F.Either.Left(Results.unauthorized()));
        }

        Accumulator<ByteString, F.Either<Result, JsonNode>> jsonAccumulator = jsonParser.apply(request);
        return jsonAccumulator.map(resultOrJson -> {
            if (resultOrJson.left.isPresent()) {
                return F.Either.Left(resultOrJson.left.get());
            } else {
                JsonNode json = resultOrJson.right.get();
                try {
                    return parseJson(json, messageType);
                } catch (Exception e) {
                    return F.Either.Left(Results.badRequest(
                            "Unable to read SNSMessage from json: " + e.getMessage()));
                }
            }
        }, executor);
    }

    private F.Either<Result, SNSMessage> parseJson(JsonNode jsonNode, String messagetype) {
        SNSMessage msg = play.libs.Json.fromJson(jsonNode, SNSMessage.class);

        if (msg.getSignatureVersion().equals(SIGNATURE_VERSION_1)) {
            if (isMessageSignatureValid.execute(msg))
                Logger.debug("Signature verification succeeded");
            else {
                Logger.debug("Signature verification failed");
                return F.Either.Left(Results.forbidden("Signature verification failed."));
            }
        } else {
            Logger.debug("Unexpected signature version. Unable to verify signature.");
            return F.Either.Left(Results.forbidden("Unexpected signature version. Unable to verify signature."));
        }

        switch (messagetype) {
            case NOTIFICATION:
                String logMsgAndSubject = "Notification received from topic " + msg.getTopicArn();
                if (msg.getSubject() != null)
                    logMsgAndSubject += " Subject: " + msg.getSubject();
                logMsgAndSubject += " SNSMessage: " + msg.getMessage();
                Logger.debug(logMsgAndSubject);

                return F.Either.Right(msg);
            case SUBSCRIPTION_CONFIRMATION:
                try {
                    wsClient.url(msg.getSubscribeURL()).get().thenApply(wsResponse -> {
                        Logger.info("Subscription confirmation (" + msg.getSubscribeURL() + ") Return value: " + wsResponse.getBody());
                        return wsResponse;
                    }).toCompletableFuture().get();
                } catch (InterruptedException | ExecutionException e) {
                    Logger.error(e.getMessage());
                    return F.Either.Left(Results.internalServerError());
                }
                return F.Either.Left(Results.ok());
            case UNSUBSCRIBE_CONFIRMATION:
                Logger.info("Unsubscribe confirmation: " + msg.getMessage());
                return F.Either.Left(Results.ok());
            default:
                Logger.info("Unknown message type.");
                return F.Either.Left(Results.badRequest());
        }
    }
}

class IsMessageSignatureValid {
    private static final String X_509 = "X.509";
    private static final String SHA_1_WITH_RSA = "SHA1withRSA";

    public boolean execute(SNSMessage msg) {
        try {
            Logger.info(msg.getSigningCertURL());
            URL url = new URL(msg.getSigningCertURL());
            InputStream inStream = url.openStream();
            CertificateFactory cf = CertificateFactory.getInstance(X_509);
            X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
            inStream.close();

            Signature sig = Signature.getInstance(SHA_1_WITH_RSA);
            sig.initVerify(cert.getPublicKey());
            Logger.info(Arrays.toString(msg.getMessageBytes()));
            sig.update(msg.getMessageBytes());
            Logger.info(msg.getSignature());
            return sig.verify(Base64.decodeBase64(msg.getSignature()));
        } catch (Exception e) {
            Logger.error("Verify method failed.", e);
            return false;
        }
    }
}
