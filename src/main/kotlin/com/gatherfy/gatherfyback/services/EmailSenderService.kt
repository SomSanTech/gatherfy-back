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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
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
    private val pendingEmailsUpdatedEvent = ConcurrentLinkedQueue<Event>()

    fun enqueueEmailNewEvent(event: Event){
        pendingEmailsNewEvent.add(event)
    }

    fun dequeueEmailNewEvent(event: Event){
        pendingEmailsNewEvent.remove(event)
    }

//    fun enqueueEmailUpdatedEvent(event: Event){
//        pendingEmailsUpdatedEvent.add(event)
//        println(event)
//    }

    // Runs every day at 12 AM
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

//    // Runs every day at 8 AM
//    @Scheduled(cron = "\${scheduler.email-new-event-notification-cron}")
//    fun processQueuedEmailsUpdatedEvents() {
//        if (pendingEmailsUpdatedEvent.isEmpty()) return
//        val eventsToSend = mutableListOf<Event>()
//        while (pendingEmailsUpdatedEvent.isNotEmpty()) {
//            eventsToSend.add(pendingEmailsUpdatedEvent.poll()) // Retrieve and remove from queue
//        }
//        if (eventsToSend.isNotEmpty()) {
//            sendUpdatedEventBatchEmails(eventsToSend)
//        }
//        println("processQueuedEmailsUpdatedEvents working")
//    }

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
            println(email)
        }
    }

    @Async
    fun sendUpdatedEventBatchEmails(event: Event, changes: String){
        val participants = registrationRepository.findRegistrationsByEventId(event.event_id!!)
        for(attendee in participants){
            buildUpdatedEventEmail(event,changes, attendee.user)
        }
    }


    fun buildEmailNewEvent(
        events: List<Event>,
        targetEmail: String
    ){
        val message: MimeMessage = emailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")
        helper.setSubject("A New Event Just Dropped! Don’t Miss Out!")
        helper.setTo(targetEmail)

        val eventDetails = events.joinToString("<br>") {
            "<p><b>EVENT : </b> ${it.event_name}<br> " +
            "<b>&#128198; Date:</b>  ${it.event_start_date.toLocalDate()} - ${it.event_end_date.toLocalDate()} <br>" +
            "<b>&#128205; Location:</b> ${it.event_location} </p>"
        }
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
        val helper = MimeMessageHelper(message, true,"UTF-8")
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
                        <p>You just unlocked a new world of events!  &#127881; <br>By following <strong>${tag.tag_title}</strong>, you will be the first to know when something exciting happens&#8722;whether it is a brand-new event, or an update.</p>
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
        val helper = MimeMessageHelper(message, true,"UTF-8")
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
        val upcomingEvents = eventRepository.findEventsStartingOn(tomorrow,nextDay)
        for (event in upcomingEvents){
            val participants = registrationRepository.findRegistrationsByEventId(event.event_id!!)
            for(attendee in participants){
                buildReminderEmail(event,attendee.user)
            }
        }
    }

    fun buildReminderEmail(event: Event, user: User) {
        val message: MimeMessage = emailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true,"UTF-8")
        helper.setSubject("Reminder: ${event.event_name} starts soon!")
        helper.setTo(user.users_email)

        val formatStartDateTime = event.event_start_date.format(DateTimeFormatter.ofPattern("E, MMM dd yyyy HH:mm"))


        val htmlContent = """
        <html>
            <body>
                <p>Hey ${user.username},</p>
                <p>Just a reminder that the event <b>${event.event_name}</b> starts on &#128198; $formatStartDateTime!</p>
                <p>Don't forget to check the details and prepare yourself.</p>
            </body>
        </html>
    """.trimIndent()

        helper.setText(htmlContent, true)
        emailSender.send(message)
    }

    @Scheduled(cron = "\${scheduler.email-countdown-notification-cron}")
    fun sendHourReminderEmails(){
        val now = LocalDateTime.now()
        val oneHourLater = now.plusHours(1).truncatedTo(ChronoUnit.MINUTES)

        val upcomingEvents = eventRepository.findEventByStartingBetween(now, oneHourLater)
        println(upcomingEvents)
        for (event in upcomingEvents){
            val participants = registrationRepository.findRegistrationsByEventId(event.event_id!!)
            for(attendee in participants){
                buildHourReminderEmail(event,attendee.user)
            }
        }
    }

    fun buildHourReminderEmail(event: Event, user: User) {
        val message: MimeMessage = emailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true,"UTF-8")
        helper.setSubject("Reminder: ${event.event_name} starts soon!")
        helper.setTo(user.users_email)

        val time = event.event_start_date.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
        val htmlContent = """
        <html>
            <body>
                <p>Hey ${user.username},</p>
                <p>Just a reminder that the event <b>${event.event_name}</b> starts at &#128339; <b>$time!</b> </p>
                <p>Be ready and check all details before the event starts.</p>
            </body>
        </html>
    """.trimIndent()

        helper.setText(htmlContent, true)
        emailSender.send(message)
    }

    @Async
    fun sendRegistrationConfirmation(event: Event, user: User) {
        val message: MimeMessage = emailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true,"UTF-8")
        helper.setSubject("You're Registered for ${event.event_name}!")
        helper.setTo(user.users_email)

        val formatStartDateTime = event.event_start_date.format(DateTimeFormatter.ofPattern("E, MMM dd yyyy HH:mm"))
        val formatEndDateTime = event.event_end_date.format(DateTimeFormatter.ofPattern("E, MMM dd yyyy HH:mm"))

        val htmlContent = """
            <html>
                <body>
                    <p>Hey ${user.username},</p>
                    <p>&#127881; You've successfully registered for <b>${event.event_name}</b>!</p>
                    <p><b>&#128198; Date:</b>  $formatStartDateTime - $formatEndDateTime</p>
                    <p><b>&#128205; Location:</b> ${event.event_location}</p>
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
        val helper = MimeMessageHelper(message, true,"UTF-8")
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

    fun buildUpdatedEventEmail(event: Event,changes: String ,user: User) {
        val message: MimeMessage = emailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true,"UTF-8")
        helper.setSubject("Event Update Detected: ${event.event_name}")
        helper.setTo(user.users_email)

        val time = event.event_start_date.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
        val htmlContent = """
        <html>
            <body>
                <p>Hey ${user.username},</p>
                <p>Changes detected in an event you register.</p>
                <p><b>EVENT : ${event.event_name}</b></p>
                <p><b>Updated Details:</b></p>
                <div>
                    ${changes.split("\n").joinToString("") { "<p>$it</p>" }}
                </div>
                <p>Stay connected. Stay informed.</p>
            </body>
        </html>
    """.trimIndent()

        helper.setText(htmlContent, true)
        emailSender.send(message)
    }

    @Async
    fun sendOtpVerification(user: User) {
        val message: MimeMessage = emailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true,"UTF-8")
        helper.setSubject("${user.otp} is your Gathefy verify code")
        helper.setTo(user.users_email)

        val htmlContent = """
            <html>
                <body>
                    <p>Hey ${user.username},</p>
                    <p>${user.otp} is your Gathefy verify code.</p>
                </body>
            </html>
        """.trimIndent()

        helper.setText(htmlContent, true)
        emailSender.send(message)
    }

}