package com.gatherfy.gatherfyback.repositories

import com.gatherfy.gatherfyback.entities.Tag
import org.springframework.data.jpa.repository.JpaRepository

interface TagRepository: JpaRepository<Tag, Long> {
}