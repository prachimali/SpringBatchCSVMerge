package com.sample.spring.batch.payments.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
/**
 * A DTO holding the Payment posting parameters.
 *
 */
@Data
public class PaymentDTO {
	
	/**
	 * Payment Id
	 */
	private String paymentId;
	
	/**
	 * AgencyCode
	 */
	private String agencyCode;
	
	/**
	 * Payment Amount
	 */
	private String paymentAmount;
	
	/**
	 * Request Date Time
	 */
	private String requestDateTime;
	
	/**
	 * Payment Date
	 */
	private String paymentDate;
	
	/**
	 * Payment Method
	 */
	private String paymentMethod;
	
	/**
	 * Account Id
	 */
	private String accountId;
}
