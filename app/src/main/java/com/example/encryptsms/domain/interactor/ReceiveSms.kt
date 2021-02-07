package com.example.encryptsms.domain.interactor

import android.telephony.SmsMessage
import io.reactivex.Flowable
import javax.inject.Inject

class ReceiveSms @Inject constructor(
    //private val conversationRepo: ConversationRepository,
    //private val messageRepo: MessageRepository
): Interactor<ReceiveSms.Params>(){

    class Params(val subId: Int, val messages: Array<SmsMessage>)

    override fun buildObservable(params: Params): Flowable<*> {
        TODO("Not yet implemented")
    }
    /**
    override fun buildObservable(params: Params): Flowable<*> {
        return Flowable.just(params)
            .filter { it.messages.isNotEmpty() }
            .mapNotNull { message ->
                {
                    // Don't continue if the sender is blocked
                    val messages = it.messages
                    val address = messages[0].displayOriginatingAddress
                    val time = messages[0].timestampMillis
                    val body: String = messages
                        .mapNotNull { message -> message.displayMessageBody }
                        .reduce { body, new -> body + new }

                    // Add the message to the db
                    val message = messageRepo.insertReceivedSms(it.subId, address, body, time)
                }
            }
            .doOnNext { message ->
                conversationRepo.updateConversations(message.threadId) // Update the conversation
            }
            .mapNotNull { message ->
                conversationRepo.getOrCreateConversation(message.threadId) // Map message to conversation
            }
            .filter { conversation -> !conversation.blocked } // Don't notify for blocked conversations
            .doOnNext { conversation ->
                // Unarchive conversation if necessary
                if (conversation.archived) conversationRepo.markUnarchived(conversation.id)
            }
            .map { conversation -> conversation.id } // Map to the id because [delay] will put us on the wrong thread
    }
*/
}