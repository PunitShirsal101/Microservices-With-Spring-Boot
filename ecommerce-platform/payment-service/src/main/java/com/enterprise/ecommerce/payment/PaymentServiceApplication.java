package com.enterprise.ecommerce.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.autoconfigure.ldap.LdapAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main application class for Payment Service
 * Handles payment processing and transactions
 */
@SpringBootApplication(scanBasePackages = {
    "com.enterprise.ecommerce.payment",
    "com.enterprise.ecommerce.common"
}, exclude = {
    BatchAutoConfiguration.class,
    LdapAutoConfiguration.class,
    MailSenderAutoConfiguration.class
})
@EnableDiscoveryClient
@EnableJpaRepositories
@EnableTransactionManagement
@EnableCaching
public class PaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}