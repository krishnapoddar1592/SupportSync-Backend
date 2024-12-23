package com.chatSDK.SupportSync.Repositories;

import com.chatSDK.SupportSync.User.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<AppUser, Long> {
}