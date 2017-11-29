package com.sample.spring.batch.payments.processor;

import org.springframework.batch.item.ItemProcessor;

import com.sample.spring.batch.payments.dto.PaymentDTO;
/** 
 * Processor for PaymentCSVMerge job 
 */
public class PaymentEntityProcessor implements ItemProcessor<PaymentDTO, PaymentDTO> {

	public PaymentDTO process(PaymentDTO paymentDTO) throws Exception {
		return paymentDTO;
	}
	

}
