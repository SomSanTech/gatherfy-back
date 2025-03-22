package com.gatherfy.gatherfyback.entities

import jakarta.persistence.*

@Entity(name = "contacts")
data class Contact(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contact_id")
    var contactId: Long? = null,
    @Column(name = "user_id")
    var userId: Long,
//    @Column(name = "save_user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "save_user_id")
    var saveUserId: User
)
