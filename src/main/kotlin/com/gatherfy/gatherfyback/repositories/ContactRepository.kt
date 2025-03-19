package com.gatherfy.gatherfyback.repositories

import com.gatherfy.gatherfyback.entities.Contact
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ContactRepository: JpaRepository<Contact, Long> {
    fun findContactsByUserId(userId: Long): List<Contact>?
    fun findContactByUserIdAndContactId(userId: Long, contactId: Long): Contact?
    fun findContactByUserIdAndAndSaveUserId(userId: Long, saveUserId: Int): Contact?
}