package me.aboullaite;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.messenger4j.MessengerPlatform;
import com.github.messenger4j.exceptions.MessengerApiException;
import com.github.messenger4j.exceptions.MessengerIOException;
import com.github.messenger4j.exceptions.MessengerVerificationException;
import com.github.messenger4j.receive.MessengerReceiveClient;
import com.github.messenger4j.receive.events.AccountLinkingEvent;
import com.github.messenger4j.receive.handlers.AccountLinkingEventHandler;
import com.github.messenger4j.receive.handlers.EchoMessageEventHandler;
import com.github.messenger4j.receive.handlers.FallbackEventHandler;
import com.github.messenger4j.receive.handlers.MessageDeliveredEventHandler;
import com.github.messenger4j.receive.handlers.MessageReadEventHandler;
import com.github.messenger4j.receive.handlers.OptInEventHandler;
import com.github.messenger4j.receive.handlers.PostbackEventHandler;
import com.github.messenger4j.receive.handlers.QuickReplyMessageEventHandler;
import com.github.messenger4j.receive.handlers.TextMessageEventHandler;
import com.github.messenger4j.send.MessengerSendClient;
import com.github.messenger4j.send.NotificationType;
import com.github.messenger4j.send.QuickReply;
import com.github.messenger4j.send.Recipient;
import com.github.messenger4j.send.SenderAction;

@RestController
@RequestMapping("/callback")
public class CallBackHandler {

	private static final Logger logger = LoggerFactory.getLogger(CallBackHandler.class);

	public static final String GOOD_ACTION = "DEVELOPER_DEFINED_PAYLOAD_FOR_GOOD_ACTION";
	public static final String NOT_GOOD_ACTION = "DEVELOPER_DEFINED_PAYLOAD_FOR_NOT_GOOD_ACTION";

	private final MessengerReceiveClient receiveClient;
	private final MessengerSendClient sendClient;

	/**
	 * Constructs the {@code CallBackHandler} and initializes the
	 * {@code MessengerReceiveClient}.
	 *
	 * @param appSecret
	 *            the {@code Application Secret}
	 * @param verifyToken
	 *            the {@code Verification Token} that has been provided by you
	 *            during the setup of the {@code
	 *                    Webhook}
	 * @param sendClient
	 *            the initialized {@code MessengerSendClient}
	 */
	@Autowired
	public CallBackHandler(@Value("${messenger4j.appSecret}") final String appSecret,
			@Value("${messenger4j.verifyToken}") final String verifyToken, final MessengerSendClient sendClient) {

		logger.debug("Initializing MessengerReceiveClient - appSecret: {} | verifyToken: {}", appSecret, verifyToken);
		this.receiveClient = MessengerPlatform.newReceiveClientBuilder(appSecret, verifyToken)
				.onTextMessageEvent(newTextMessageEventHandler())
				.onQuickReplyMessageEvent(newQuickReplyMessageEventHandler()).onPostbackEvent(newPostbackEventHandler())
				.onAccountLinkingEvent(newAccountLinkingEventHandler()).onOptInEvent(newOptInEventHandler())
				.onEchoMessageEvent(newEchoMessageEventHandler())
				.onMessageDeliveredEvent(newMessageDeliveredEventHandler())
				.onMessageReadEvent(newMessageReadEventHandler()).fallbackEventHandler(newFallbackEventHandler())
				.build();
		this.sendClient = sendClient;
	}

	/**
	 * Webhook verification endpoint.
	 *
	 * The passed verification token (as query parameter) must match the configured
	 * verification token. In case this is true, the passed challenge string must be
	 * returned by this endpoint.
	 */
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<String> verifyWebhook(@RequestParam("hub.mode") final String mode,
			@RequestParam("hub.verify_token") final String verifyToken,
			@RequestParam("hub.challenge") final String challenge) {

		logger.debug("Received Webhook verification request - mode: {} | verifyToken: {} | challenge: {}", mode,
				verifyToken, challenge);
		try {
			return ResponseEntity.ok(this.receiveClient.verifyWebhook(mode, verifyToken, challenge));
		} catch (MessengerVerificationException e) {
			logger.warn("Webhook verification failed: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
		}
	}

	/**
	 * Callback endpoint responsible for processing the inbound messages and events.
	 */
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<Void> handleCallback(@RequestBody final String payload,
			@RequestHeader("X-Hub-Signature") final String signature) {

		logger.debug("Received Messenger Platform callback - payload: {} | signature: {}", payload, signature);
		try {
			this.receiveClient.processCallbackPayload(payload, signature);
			logger.debug("Processed callback payload successfully");
			return ResponseEntity.status(HttpStatus.OK).build();
		} catch (MessengerVerificationException e) {
			logger.warn("Processing of callback payload failed: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
	}

	private TextMessageEventHandler newTextMessageEventHandler() {
		return event -> {
			logger.debug("Received TextMessageEvent: {}", event);

			final String messageId = event.getMid();
			final String messageText = event.getText();
			final String senderId = event.getSender().getId();
			final Date timestamp = event.getTimestamp();

			logger.info("Received message '{}' with text '{}' from user '{}' at '{}'", messageId, messageText, senderId,
					timestamp);

			try {
				switch (messageText.toLowerCase()) {

				case "yo":
					sendTextMessage(senderId, "Hello, What I can do for you ? Type the word you're looking for");
					break;

				case "great":
					sendTextMessage(senderId, "You're welcome :) keep rocking");
					break;

				default:
					sendReadReceipt(senderId);
					sendTypingOn(senderId);
					sendSpringDoc(senderId, messageText);
					sendQuickReply(senderId);
					sendTypingOff(senderId);
				}
			} catch (MessengerApiException | MessengerIOException e) {
				handleSendException(e);
			} catch (IOException e) {
				handleIOException(e);
			}
		};
	}

	private void sendSpringDoc(String recipientId, String keyword)
			throws MessengerApiException, MessengerIOException, IOException {
		
		this.sendTextMessage(recipientId, "Thanks we are working");
		 
	}

	private void sendQuickReply(String recipientId) throws MessengerApiException, MessengerIOException {
		final List<QuickReply> quickReplies = QuickReply.newListBuilder().addTextQuickReply("Looks good", GOOD_ACTION)
				.toList().addTextQuickReply("Nope!", NOT_GOOD_ACTION).toList().build();

		this.sendClient.sendTextMessage(recipientId, "Was this helpful?!", quickReplies);
	}

	private void sendReadReceipt(String recipientId) throws MessengerApiException, MessengerIOException {
		this.sendClient.sendSenderAction(recipientId, SenderAction.MARK_SEEN);
	}

	private void sendTypingOn(String recipientId) throws MessengerApiException, MessengerIOException {
		this.sendClient.sendSenderAction(recipientId, SenderAction.TYPING_ON);
	}

	private void sendTypingOff(String recipientId) throws MessengerApiException, MessengerIOException {
		this.sendClient.sendSenderAction(recipientId, SenderAction.TYPING_OFF);
	}

	private QuickReplyMessageEventHandler newQuickReplyMessageEventHandler() {
		return event -> {
			logger.debug("Received QuickReplyMessageEvent: {}", event);

			final String senderId = event.getSender().getId();
			final String messageId = event.getMid();
			final String quickReplyPayload = event.getQuickReply().getPayload();

			logger.info("Received quick reply for message '{}' with payload '{}'", messageId, quickReplyPayload);

			if (quickReplyPayload.equals(GOOD_ACTION))
				sendTextMessage(senderId, "Ok thanks for contatcting");
			else
				sendTextMessage(senderId, "Sorry couldn't serve you properly");

		};
	}

	private PostbackEventHandler newPostbackEventHandler() {
		return event -> {
			logger.debug("Received PostbackEvent: {}", event);

			final String senderId = event.getSender().getId();
			final String recipientId = event.getRecipient().getId();
			final String payload = event.getPayload();
			final Date timestamp = event.getTimestamp();

			logger.info("Received postback for user '{}' and page '{}' with payload '{}' at '{}'", senderId,
					recipientId, payload, timestamp);

			sendTextMessage(senderId, "Postback called");
		};
	}

	private AccountLinkingEventHandler newAccountLinkingEventHandler() {
		return event -> {
			logger.debug("Received AccountLinkingEvent: {}", event);

			final String senderId = event.getSender().getId();
			final AccountLinkingEvent.AccountLinkingStatus accountLinkingStatus = event.getStatus();
			final String authorizationCode = event.getAuthorizationCode();

			logger.info("Received account linking event for user '{}' with status '{}' and auth code '{}'", senderId,
					accountLinkingStatus, authorizationCode);
		};
	}

	private OptInEventHandler newOptInEventHandler() {
		return event -> {
			logger.debug("Received OptInEvent: {}", event);

			final String senderId = event.getSender().getId();
			final String recipientId = event.getRecipient().getId();
			final String passThroughParam = event.getRef();
			final Date timestamp = event.getTimestamp();

			logger.info("Received authentication for user '{}' and page '{}' with pass through param '{}' at '{}'",
					senderId, recipientId, passThroughParam, timestamp);

			sendTextMessage(senderId, "Authentication successful");
		};
	}

	private EchoMessageEventHandler newEchoMessageEventHandler() {
		return event -> {
			logger.debug("Received EchoMessageEvent: {}", event);

			final String messageId = event.getMid();
			final String recipientId = event.getRecipient().getId();
			final String senderId = event.getSender().getId();
			final Date timestamp = event.getTimestamp();

			logger.info("Received echo for message '{}' that has been sent to recipient '{}' by sender '{}' at '{}'",
					messageId, recipientId, senderId, timestamp);
		};
	}

	private MessageDeliveredEventHandler newMessageDeliveredEventHandler() {
		return event -> {
			logger.debug("Received MessageDeliveredEvent: {}", event);

			final List<String> messageIds = event.getMids();
			final Date watermark = event.getWatermark();
			final String senderId = event.getSender().getId();

			if (messageIds != null) {
				messageIds.forEach(messageId -> {
					logger.info("Received delivery confirmation for message '{}'", messageId);
				});
			}

			logger.info("All messages before '{}' were delivered to user '{}'", watermark, senderId);
		};
	}

	private MessageReadEventHandler newMessageReadEventHandler() {
		return event -> {
			logger.debug("Received MessageReadEvent: {}", event);

			final Date watermark = event.getWatermark();
			final String senderId = event.getSender().getId();

			logger.info("All messages before '{}' were read by user '{}'", watermark, senderId);
		};
	}

	/**
	 * This handler is called when either the message is unsupported or when the
	 * event handler for the actual event type is not registered. In this showcase
	 * all event handlers are registered. Hence only in case of an unsupported
	 * message the fallback event handler is called.
	 */
	private FallbackEventHandler newFallbackEventHandler() {
		return event -> {
			logger.debug("Received FallbackEvent: {}", event);

			final String senderId = event.getSender().getId();
			logger.info("Received unsupported message from user '{}'", senderId);
		};
	}

	private void sendTextMessage(String recipientId, String text) {
		try {
			final Recipient recipient = Recipient.newBuilder().recipientId(recipientId).build();
			final NotificationType notificationType = NotificationType.REGULAR;
			final String metadata = "DEVELOPER_DEFINED_METADATA";

			this.sendClient.sendTextMessage(recipient, notificationType, text, metadata);
		} catch (MessengerApiException | MessengerIOException e) {
			handleSendException(e);
		}
	}

	private void handleSendException(Exception e) {
		logger.error("Message could not be sent. An unexpected error occurred.", e);
	}

	private void handleIOException(Exception e) {
		logger.error("Could not open Spring.io page. An unexpected error occurred.", e);
	}
}
