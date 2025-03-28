package com.gatherfy.gatherfyback.services

import com.gatherfy.gatherfyback.entities.Event
import com.gatherfy.gatherfyback.entities.Tag
import com.gatherfy.gatherfyback.entities.User
import com.gatherfy.gatherfyback.repositories.EventRepository
import com.gatherfy.gatherfyback.repositories.UserRepository
import jakarta.mail.internet.MimeMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentLinkedQueue

@Service
class EmailSenderService(
    private val javaMailSender: JavaMailSender,
    private val userRepository: UserRepository,
    private val eventRepository: EventRepository,
    val templateEngine: TemplateEngine
) {

    private val pendingEmailsNewEvent = ConcurrentLinkedQueue<Event>()

    fun enqueueEmailNewEvent(event: Event){
        pendingEmailsNewEvent.add(event)
    }

    fun dequeueEmailNewEvent(event: Event){
        pendingEmailsNewEvent.remove(event)
    }

    fun sendEmail(subject: String, userEmail: String, template: String, context: Context ){
        val message: MimeMessage = javaMailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true,"UTF-8")
        helper.setFrom("Gatherfy")
        helper.setSubject(subject)
        helper.setTo(userEmail)

        val content = templateEngine.process(template, context)

        helper.setText(content, true)
        javaMailSender.send(message)
    }

    // New events notification
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
            val notifiedUsers = userRepository.findFollowerByTagIdAndEnableEmailNewEvents(tagIds) // Fetch users directly

            if(notifiedUsers.isNotEmpty()){
                for (user in notifiedUsers) {
                    userEmailMap.computeIfAbsent(user.users_email) { mutableListOf() }
                        .add(event)
                }
            }
        }
        // Send emails
        for ((email, eventList) in userEmailMap) {
            buildEmailNewEvent(eventList, email)
            println(email)
        }
    }

    fun buildEmailNewEvent(
        events: List<Event>,
        targetEmail: String
    ){
        val eventDetails = events.joinToString("<br>") {
            "<p><b>EVENT : </b> ${it.event_name}<br> " +
            "<b>&#128198; Date:</b>  ${it.event_start_date.toLocalDate()} - ${it.event_end_date.toLocalDate()} <br>" +
            "<b>&#128205; Location:</b> ${it.event_location} </p>"
        }
        val context = Context()
        context.setVariable("eventDetails", eventDetails)

        sendEmail("A New Event Just Dropped! Don’t Miss Out!", targetEmail, "new-events.html", context)
    }

    @Async
    fun sendUpdatedEventBatchEmails(event: Event, changes: String){
        val notifiedParticipants = userRepository.findParticipantsByEventIdAndEnableEmailUpdatedEvent(event.event_id!!)
        if(notifiedParticipants.isNotEmpty()){
            for(participant in notifiedParticipants){
                buildUpdatedEventEmail(event,changes, participant)
            }
        }
    }

    fun buildUpdatedEventEmail(event: Event,changes: String ,user: User) {
        val context = Context()
        context.setVariable("username", user.username)
        context.setVariable("eventName", event.event_name)
        context.setVariable("changes", changes)

        sendEmail("Event Update Detected: ${event.event_name}",user.users_email,"event-updated.html",context)
    }

    @Async
    fun sendEmailFollowTag(tag: Tag, user: User) {
        val context = Context()
        context.setVariable("username", user.username)
        context.setVariable("tag", tag.tag_title)

        sendEmail("Stay in the Loop! New Events You’ll Love Inside", user.users_email, "subscription.html", context)
    }

    @Async
    fun sendEmailUnfollowTag(tag: Tag, user: User) {
        val context = Context()
        context.setVariable("username", user.username)
        context.setVariable("tag", tag.tag_title)

        sendEmail("All Set! You've Unfollowed ${tag.tag_title}",user.users_email,"unsubscription.html",context)

    }

    @Scheduled(cron = "\${scheduler.email-reminder-notification-cron}")
    fun sendReminderEmails(){
        val localDate = LocalDate.now()
        val tomorrow = localDate.plusDays(1).atStartOfDay() // 2025-02-13T00:00:00
        val nextDay = localDate.plusDays(2).atStartOfDay() // 2025-02-14T00:00:00
        val upcomingEvents = eventRepository.findEventsStartingOn(tomorrow,nextDay)
        for (event in upcomingEvents){
            val notifiedParticipants = userRepository.findParticipantsByEventIdAndEnableEmailReminderDay(event.event_id!!)
            for(participant in notifiedParticipants!!){
                buildReminderEmail(event,participant)
            }
        }
    }

    fun buildReminderEmail(event: Event, user: User) {
        val formatStartDateTime = event.event_start_date.format(DateTimeFormatter.ofPattern("E, MMM dd yyyy HH:mm"))

        val context = Context()
        context.setVariable("username", user.username)
        context.setVariable("eventName", event.event_name)
        context.setVariable("startTime", formatStartDateTime)

        sendEmail("Reminder: ${event.event_name} starts soon!",user.users_email,"reminder.html",context)
    }

    @Scheduled(cron = "\${scheduler.email-countdown-notification-cron}")
    fun sendHourReminderEmails(){
        val now = LocalDateTime.now()
        val oneHourLater = now.plusHours(1).truncatedTo(ChronoUnit.MINUTES)

        val upcomingEvents = eventRepository.findEventByStartingBetween(now.plusMinutes(1), oneHourLater)
        println(upcomingEvents)
        for (event in upcomingEvents){
            val notifiedParticipants = userRepository.findParticipantsByEventIdAndEnableEmailReminderHour(event.event_id!!)
            for(participant in notifiedParticipants!!){
                buildHourReminderEmail(event,participant)
            }
        }
    }

    fun buildHourReminderEmail(event: Event, user: User) {
        val time = event.event_start_date.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))

        val context = Context()
        context.setVariable("username", user.username)
        context.setVariable("eventName", event.event_name)
        context.setVariable("startTime", time)

        sendEmail("Reminder: ${event.event_name} starts soon!",user.users_email,"reminderHour.html",context)
    }

    @Async
    fun sendRegistrationConfirmation(event: Event, user: User) {
        val formatStartDateTime = event.event_start_date.format(DateTimeFormatter.ofPattern("E, MMM dd yyyy HH:mm"))
        val formatEndDateTime = event.event_end_date.format(DateTimeFormatter.ofPattern("E, MMM dd yyyy HH:mm"))

        val context = Context()
        context.setVariable("username", user.username)
        context.setVariable("eventName", event.event_name)
        context.setVariable("formatStartDateTime",formatStartDateTime)
        context.setVariable("formatEndDateTime",formatEndDateTime)
        context.setVariable("location",event.event_location)

        sendEmail("You're Registered for ${event.event_name}!",user.users_email,"registration.html",context)
    }

    @Async
    fun sendOtpVerification(user: User) {
        val context = Context()
        context.setVariable("otp", user.otp)
        sendEmail("${user.otp} is your Gathefy verify code",user.users_email,"otp-verification.html",context)
    }
}