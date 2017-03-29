package com.kickstarter.viewmodels;

import android.content.Intent;
import android.support.annotation.NonNull;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.MessageFactory;
import com.kickstarter.factories.MessageThreadEnvelopeFactory;
import com.kickstarter.factories.MessageThreadFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.models.Message;
import com.kickstarter.models.MessageThread;
import com.kickstarter.services.MockApiClient;
import com.kickstarter.services.apiresponses.MessageThreadEnvelope;
import com.kickstarter.ui.IntentKey;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.observers.TestSubscriber;

public final class MessagesViewModelTest extends KSRobolectricTestCase {
  private MessagesViewModel.ViewModel vm;
  private final TestSubscriber<String> creatorNameTextViewText = new TestSubscriber<>();
  private final TestSubscriber<List<Message>> messages = new TestSubscriber<>();
  private final TestSubscriber<String> projectNameTextViewText = new TestSubscriber<>();

  protected void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new MessagesViewModel.ViewModel(environment);
    this.vm.outputs.creatorNameTextViewText().subscribe(this.creatorNameTextViewText);
    this.vm.outputs.messages().subscribe(this.messages);
    this.vm.outputs.projectNameTextViewText().subscribe(this.projectNameTextViewText);
  }

  @Test
  public void testProjectData() {
    final MessageThread messageThread = MessageThreadFactory.messageThread();
    setUpEnvironment(environment());

    // Start the view model with a message thread.
    this.vm.intent(new Intent().putExtra(IntentKey.MESSAGE_THREAD, messageThread));

    this.creatorNameTextViewText.assertValues(messageThread.project().creator().name());
    this.projectNameTextViewText.assertValues(messageThread.project().name());
  }

  @Test
  public void testMessagesEmit() {
    final MessageThreadEnvelope envelope = MessageThreadEnvelopeFactory.messageThreadEnvelope()
      .toBuilder()
      .messages(Collections.singletonList(MessageFactory.message()))
      .build();

    final MockApiClient apiClient = new MockApiClient() {
      @Override public @NonNull Observable<MessageThreadEnvelope> fetchMessagesForThread(final @NonNull MessageThread messageThread) {
        return Observable.just(envelope);
      }
    };

    setUpEnvironment(environment().toBuilder().apiClient(apiClient).build());

    // Start the view model with a message thread.
    this.vm.intent(new Intent().putExtra(IntentKey.MESSAGE_THREAD, MessageThreadFactory.messageThread()));

    // Messages emit.
    this.messages.assertValueCount(1);
  }
}