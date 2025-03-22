package com.gatherfy.gatherfyback.repositories

import com.gatherfy.gatherfyback.entities.Contact
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ContactRepository: JpaRepository<Contact, Long> {
    fun findContactsByUserId(userId: Long): List<Contact>?
    fun findContactByUserIdAndContactId(userId: Long, contactId: Long): Contact?
    @Query("SELECT c FROM contacts c WHERE c.userId = :userId AND c.saveUserId.users_id = :saveUserId")
    fun findContactByUserIdAndAndSaveUserId(userId: Long, saveUserId: Long): Contact?
}