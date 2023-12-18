package com.UmiUni.shop.repository;

import com.UmiUni.shop.entity.PaymentErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentErrorLogRepo extends JpaRepository<PaymentErrorLog, Long> {
}
