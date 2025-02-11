package com.gatherfy.gatherfyback.services

import com.gatherfy.gatherfyback.entities.Event
import com.gatherfy.gatherfyback.entities.Tag
import com.gatherfy.gatherfyback.entities.User
import com.gatherfy.gatherfyback.repositories.EventRepository
import com.gatherfy.gatherfyback.repositories.RegistrationRepository
import com.gatherfy.gatherfyback.repositories.SubscriptionRepository
import com.gatherfy.gatherfyback.repositories.UserRepository
import jakarta.mail.internet.MimeMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.concurrent.ConcurrentLinkedQueue

@Service
class EmailSenderService(
    private val emailSender: JavaMailSender,
    private val subscriptionRepository: SubscriptionRepository,
    private val userRepository: UserRepository,
    private val eventRepository: EventRepository,
    private val registrationRepository: RegistrationRepository,
) {

    private val pendingEmailsNewEvent = ConcurrentLinkedQueue<Event>()

    fun enqueueEmailNewEvent(event: Event){
        pendingEmailsNewEvent.add(event)
        println(event)
    }

    // Runs every day at 8 AM
    @Scheduled(cron = "\${scheduler.email-new-event-notification-cron}")
    fun processQueuedEmailsNewEvents() {
        if (pendingEmailsNewEvent.isEmpty()) return
        val eventsToSend = mutableListOf<Event>()
        while (pendingEmailsNewEvent.isNotEmpty()) {
            eventsToSend.add(pendingEmailsNewEvent.poll()) // Retrieve and remove from queue
        }
        if (eventsToSend.isNotEmpty()) {
            sendEventBatchEmails(eventsToSend)
        }
    }

    fun sendEventBatchEmails(events: List<Event>){
        val userEmailMap = mutableMapOf<String, MutableList<Event>>()

        for(event in events){
            val tagIds = event.tags.map { it.tag_id }
            val followers = subscriptionRepository.findSubscriptionsByTagIdIn(tagIds)
                ?.mapNotNull { it.userId }
                ?.distinct()
                ?: emptyList()


            for (userId in followers) {
                val user = userRepository.findUserById(userId)
                userEmailMap.computeIfAbsent(user?.users_email!!) { mutableListOf() }
                    .add(event)
            }
        }
        // Send emails
        for ((email, eventList) in userEmailMap) {
            buildEmailNewEvent(eventList, email)
        }
        println(userEmailMap)
    }


    fun buildEmailNewEvent(
        events: List<Event>,
//        username: String,
        targetEmail: String
    ){
        val message: MimeMessage = emailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true)
        helper.setSubject("[Gatherfy news] A New Event Just Dropped! Don’t Miss Out!")
        helper.setTo(targetEmail)
        val eventDetails = events.joinToString("<br>") { "<p>🎉 ${it.event_name}</p>" }
        val htmlContent = """
        <!DOCTYPE html>
        <html>
            <head>
                <link rel="preconnect" href="https://fonts.googleapis.com">
                <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
                <link href="https://fonts.googleapis.com/css2?family=Oooh+Baby&family=Poppins:wght@400;500&display=swap" rel="stylesheet">
    
                <style>
                    body { font-family: 'Poppins', Arial, sans-serif; }
                    .container { padding: 20px; background-color: #f4f4f4; max-width:600px; }
                    .content { background: white; padding: 20px; border-radius: 8px; }
                    .btn { display: inline-block; padding: 10px 20px; background: #007bff; color: white; text-decoration: none; border-radius: 5px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="content">
                        <p>Hey Xxxxxx,</p>
                        <p>🛑 ALERT: New Event Detected!</p>
                        <p>$eventDetails</p>
                        <p style="font-family: 'Oooh Baby';font-size: 24px;"><span style="color:#D71515">G</span>atherfy</p>
                    </div>
                </div>
            </body>
        </html>
    """.trimIndent()
        helper.setText(htmlContent,true)
        emailSender.send(message)
    }


    @Async
    fun sendEmailFollowTag(tag: Tag, user: User) {
//        val message = SimpleMailMessage()
        val message: MimeMessage = emailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true)
        helper.setSubject("Stay in the Loop! New Events You’ll Love Inside")
        helper.setTo(user.users_email)

        val htmlContent = """
        <!DOCTYPE html>
        <html>
            <head>
                <link rel="preconnect" href="https://fonts.googleapis.com">
                <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
                <link href="https://fonts.googleapis.com/css2?family=Oooh+Baby&family=Poppins:wght@400;500&display=swap" rel="stylesheet">
    
                <style>
                    body { font-family: 'Poppins', Arial, sans-serif; }
                    .container { padding: 20px; background-color: #f4f4f4; max-width:600px; }
                    .content { background: white; padding: 20px; border-radius: 8px; }
                    .btn { display: inline-block; padding: 10px 20px; background: #007bff; color: white; text-decoration: none; border-radius: 5px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="content">
                        <p>Hello ${user.username},</p>
                        <p>You just unlocked a new world of events! &#127881; By following <strong>${tag.tag_title}</strong>, you will be the first to know when something exciting happens&#8722;whether it is a brand-new event, an update, or even a surprise cancellation.</p>
                        <p>Get ready to explore, engage, and never miss out!</p>
                        <p>&#128640; Stay tuned,</p>
                        <p style="font-family: 'Oooh Baby';font-size: 24px;"><span style="color:#D71515">G</span>atherfy</p>
                    </div>
                </div>
            </body>
        </html>
    """.trimIndent()
        helper.setText(htmlContent,true)
        emailSender.send(message)
    }

    @Async
    fun sendEmailUnfollowTag(
        tag: Tag,
        user: User
    ) {
//        val message = SimpleMailMessage()
        val message: MimeMessage = emailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true)
        helper.setSubject("All Set! You've Unfollowed ${tag.tag_title}")
        helper.setTo(user.users_email)

        val htmlContent = """
        <!DOCTYPE html>
        <html>
            <head>
                <link rel="preconnect" href="https://fonts.googleapis.com">
                <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
                <link href="https://fonts.googleapis.com/css2?family=Oooh+Baby&family=Poppins:wght@400;500&display=swap" rel="stylesheet">
    
                <style>
                    body { font-family: 'Poppins', Arial, sans-serif; }
                    .container { padding: 20px; background-color: #f4f4f4; max-width:600px; }
                    .content { background: white; padding: 20px; border-radius: 8px; }
                    .btn { display: inline-block; padding: 10px 20px; background: #007bff; color: white; text-decoration: none; border-radius: 5px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="content">
                        <p>Hey ${user.username},</p>
                        <p>We just got word that you unfollowed <strong>${tag.tag_title}</strong>. That means you will not be getting the latest scoops, epic updates, or exclusive event news anymore.</p>
                        <p>But hey, no hard feelings! Just know that whenever you are ready to rejoin the action, the Follow button is always there.</p>
                        <p>Until next time,</p>
                        <p style="font-family: 'Oooh Baby';font-size: 24px;"><span style="color:#D71515">G</span>atherfy</p>
                    </div>
                </div>
            </body>
        </html>
    """.trimIndent()
        helper.setText(htmlContent,true)
        emailSender.send(message)
    }

    @Scheduled(cron = "\${scheduler.email-reminder-notification-cron}")
    fun sendReminderEmails(){
        val localDate = LocalDate.now()
        val tomorrow = localDate.plusDays(1).atStartOfDay() // 2025-02-13T00:00:00
        val nextDay = localDate.plusDays(2).atStartOfDay() // 2025-02-14T00:00:00
        val upcomingEvents = eventRepository.findEventByStartDate(tomorrow, nextDay)
        for (event in upcomingEvents){
            val participants = registrationRepository.findRegistrationsByEventId(event.event_id!!)
            for(attendee in participants){
                buildReminderEmail(event,attendee.user)
            }
        }
    }

    fun buildReminderEmail(event: Event, user: User) {
        val message: MimeMessage = emailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true)
        helper.setSubject("📢 Reminder: ${event.event_name} starts soon!")
        helper.setTo(user.users_email)

        val htmlContent = """
        <html>
            <body>
                <p>Hey ${user.username},</p>
                <p>Just a reminder that the event <b>${event.event_name}</b> starts on ${event.event_start_date}!</p>
                <p>Don't forget to check the details and prepare yourself.</p>
            </body>
        </html>
    """.trimIndent()

        helper.setText(htmlContent, true)
        emailSender.send(message)
    }

    @Async
    fun sendRegistrationConfirmation(event: Event, user: User) {
        val message: MimeMessage = emailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true)
        helper.setSubject("You're Registered for ${event.event_name}!")
        helper.setTo(user.users_email)

        val htmlContent = """
            <html>
                <body>
                    <p>Hey ${user.username},</p>
                    <p>&#127881; You've successfully registered for <b>${event.event_name}</b>!</p>
                    <p><b>&#128197; Date:</b> ${event.event_start_date}</p>
                    <p><b>&#128681; Location:</b> ${event.event_location}</p>
                    <p>We’ll send you a reminder before the event starts.</p>
                </body>
            </html>
        """.trimIndent()

        helper.setText(htmlContent, true)
        emailSender.send(message)
    }

    fun sendEmailCancelEvent(
        event: String,
        username: String,
        targetEmail: String
    ) {
        val message: MimeMessage = emailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true)
        helper.setSubject("[Gatherfy news] $event Has Been Cancelled")
        helper.setTo(targetEmail)

        val htmlContent = """
        <!DOCTYPE html>
        <html>
            <head>
                <link rel="preconnect" href="https://fonts.googleapis.com">
                <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
                <link href="https://fonts.googleapis.com/css2?family=Oooh+Baby&family=Poppins:wght@400;500&display=swap" rel="stylesheet">
    
                <style>
                    body { font-family: 'Poppins', Arial, sans-serif; }
                    .container { padding: 20px; background-color: #f4f4f4; max-width:600px; }
                    .content { background: white; padding: 20px; border-radius: 8px; }
                    .btn { display: inline-block; padding: 10px 20px; background: #007bff; color: white; text-decoration: none; border-radius: 5px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="content">
                        <p>Hey ${username},</p>
                        <p>We’ve got some unfortunate news. [Event Title] has been removed. </p>
                        <p>We know this might be disappointing, but don’t worry—there are plenty of other exciting events coming up!</p>
                        <p>Check out other events you might love:</p>
                        <p>🔗 [Browse Events]</p>
                        <p>Thank you for staying with us!</p>
                        <p style="font-family: 'Oooh Baby';font-size: 24px;"><span style="color:#D71515">G</span>atherfy</p>
                    </div>
                </div>
            </body>
        </html>
    """.trimIndent()
        helper.setText(htmlContent,true)
        emailSender.send(message)
    }

    fun sendEmailUpdateEvent( // Notice only ticket and event date changed
        event: String,
        username: String,
        targetEmail: String
    ) {
        val message: MimeMessage = emailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true)
        helper.setSubject("[Gatherfy news] ⚠ EVENT UPDATE DETECTED: [$event]")
        helper.setTo(targetEmail)

        val htmlContent = """
        <!DOCTYPE html>
        <html>
            <head>
                <link rel="preconnect" href="https://fonts.googleapis.com">
                <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
                <link href="https://fonts.googleapis.com/css2?family=Oooh+Baby&family=Poppins:wght@400;500&display=swap" rel="stylesheet">
    
                <style>
                    body { font-family: 'Poppins', Arial, sans-serif; }
                    .container { padding: 20px; background-color: #f4f4f4; max-width:600px; }
                    .content { background: white; padding: 20px; border-radius: 8px; }
                    .btn { display: inline-block; padding: 10px 20px; background: #007bff; color: white; text-decoration: none; border-radius: 5px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="content">
                        <p>Hey ${username},</p>
                        <p>Heads up! [$event] has been updated. Here’s what’s new:</p>
                        <p>🔹 Updated Details:</p>
                        <p>Don’t miss out—check out the latest details!</p>
                        <p>🔗 [Browse Events]</p>
                        <p>Stay tuned for more updates!</p>
                        <p style="font-family: 'Oooh Baby';font-size: 24px;"><span style="color:#D71515">G</span>atherfy</p>
                    </div>
                </div>
            </body>
        </html>
    """.trimIndent()
        helper.setText(htmlContent,true)
        emailSender.send(message)
    }
}