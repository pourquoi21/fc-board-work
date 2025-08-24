package org.fc.fcboardwork.repository;

import org.fc.fcboardwork.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
}
