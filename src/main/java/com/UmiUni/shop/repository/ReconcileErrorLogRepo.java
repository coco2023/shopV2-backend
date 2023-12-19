package com.UmiUni.shop.repository;

import com.UmiUni.shop.entity.ReconcileErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReconcileErrorLogRepo extends JpaRepository<ReconcileErrorLog, Long> {
}
