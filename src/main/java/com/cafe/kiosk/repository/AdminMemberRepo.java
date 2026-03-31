package com.cafe.kiosk.repository;

import com.cafe.kiosk.domain.AdminMembers;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminMemberRepo extends JpaRepository<AdminMembers, Integer> {

    Optional<AdminMembers> findByUsername(String username);
}
