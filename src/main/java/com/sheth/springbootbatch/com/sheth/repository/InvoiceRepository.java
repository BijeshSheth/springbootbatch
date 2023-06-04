package com.sheth.springbootbatch.com.sheth.repository;

import com.sheth.springbootbatch.com.sheth.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
}
