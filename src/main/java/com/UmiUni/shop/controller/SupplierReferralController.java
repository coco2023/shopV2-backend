package com.UmiUni.shop.controller;

import com.UmiUni.shop.service.SupplierReferralService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/suppliers/refer")
@Log4j2
public class SupplierReferralController {

    /**
     * only for referral aims
     */
    @Autowired
    private SupplierReferralService partnerReferralService;

    @PostMapping("/create-referral")
    public String createReferral() {
        return partnerReferralService.createPartnerReferral();
    }

}
