/**
 *
 */
package io.mosip.kernel.smsserviceprovider.msg91.impl;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import io.mosip.kernel.core.notification.exception.InvalidNumberException;
import io.mosip.kernel.core.notification.model.SMSResponseDto;
import io.mosip.kernel.core.notification.spi.SMSServiceProvider;
import io.mosip.kernel.core.util.StringUtils;
import io.mosip.kernel.smsserviceprovider.msg91.constant.SmsExceptionConstant;
import io.mosip.kernel.smsserviceprovider.msg91.constant.SmsPropertyConstant;

import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

/**
 * @author Ritesh Sinha
 * @since 1.0.0
 */
@Component
public class SMSServiceProviderImpl implements SMSServiceProvider {

	@Autowired
	RestTemplate restTemplate;

	@Value("${mosip.kernel.sms.enabled:false}")
	boolean smsEnabled;

	@Value("${mosip.kernel.sms.country.code}")
	String countryCode;

	@Value("${mosip.kernel.sms.number.length}")
	int numberLength;

	@Value("${mosip.kernel.sms.api}")
	String api;

	@Value("${mosip.kernel.sms.sender}")
	String sender;

	@Value("${mosip.kernel.sms.password:null}")
	private String password;

	@Value("${mosip.kernel.sms.route:null}")
	String route;

	@Value("${mosip.kernel.sms.authkey:null}")
	String authkey;

	@Value("${mosip.kernel.sms.unicode:1}")
	String unicode;

	@Override
	public SMSResponseDto sendSms(String contactNumber, String message) {
		SMSResponseDto smsResponseDTO = new SMSResponseDto();
//		validateInput(contactNumber);
//		UriComponentsBuilder sms = UriComponentsBuilder.fromHttpUrl(api)
//				.queryParam(SmsPropertyConstant.AUTH_KEY.getProperty(), authkey)
//				.queryParam(SmsPropertyConstant.SMS_MESSAGE.getProperty(), message)
//				.queryParam(SmsPropertyConstant.ROUTE.getProperty(), route)
//				.queryParam(SmsPropertyConstant.SENDER_ID.getProperty(), sender)
//				.queryParam(SmsPropertyConstant.RECIPIENT_NUMBER.getProperty(), contactNumber)
//				.queryParam(SmsPropertyConstant.UNICODE.getProperty(), unicode)
//				.queryParam(SmsPropertyConstant.COUNTRY_CODE.getProperty(), countryCode);
		try {
			String smsUrl="";
			String encoding="";
			try {
				if (Pattern.compile("([ሀ-ፖ]+)").matcher(message).find()) {
					//System.out.println("Amharic Found");
					encoding = "&coding=2&charset=utf-8";
//					byte[] bytesEncoded =Base64.encodeBase64(message.getBytes("UTF-16LE"));
//					message = new String(bytesEncoded);
				} else {
					//System.out.println("English");
					encoding = "";
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			smsUrl="http://172.16.127.234:13013/cgi-bin/sendsms?user=tester&pass=foobar&to="+contactNumber+"&text="+message+"&from=9779"+encoding;
			restTemplate.getForEntity(smsUrl, String.class);
		} catch (HttpClientErrorException | HttpServerErrorException e) {
			smsResponseDTO.setMessage(e.getResponseBodyAsString());
			smsResponseDTO.setStatus("error");
		}
		smsResponseDTO.setMessage(SmsPropertyConstant.SUCCESS_RESPONSE.getProperty());
		smsResponseDTO.setStatus("success");
		return smsResponseDTO;
	}

	private void validateInput(String contactNumber) {
		if (!StringUtils.isNumeric(contactNumber) || contactNumber.length() < numberLength
				|| contactNumber.length() > numberLength) {
			throw new InvalidNumberException(SmsExceptionConstant.SMS_INVALID_CONTACT_NUMBER.getErrorCode(),
					SmsExceptionConstant.SMS_INVALID_CONTACT_NUMBER.getErrorMessage() + numberLength
							+ SmsPropertyConstant.SUFFIX_MESSAGE.getProperty());
		}
	}


	public static void main(String[] args) {
		String phone="0911319132";
		String message="Kannel SMS Test";
		if(args.length==1)
			phone=args[0];
		if(args.length==2)
			message=args[1];

		SMSServiceProviderImpl sender = new SMSServiceProviderImpl();
		sender.restTemplate= new RestTemplate();

		SMSResponseDto smsResponseDTO=null;
		System.out.printf("SMS TEST: Sending Message To:"+phone+", Message: "+message+"" );
		smsResponseDTO =sender.sendSms(phone,message);
		System.out.println("TEST RESULT: "+"Status:"+smsResponseDTO.getStatus()+" Message:"+smsResponseDTO.getMessage());

		message="ውድ ደንበኛችን ይህ ከደረሶ አማርኝ  ይስራል ማለት ነው\n" +
				"አናመሰገናልን።";
		System.out.printf("SMS AMHARIC TEST: Sending Message To:"+phone+", Message: "+message+"" );
		smsResponseDTO =sender.sendSms(phone,message);
		System.out.println("TEST RESULT: "+"Status:"+smsResponseDTO.getStatus()+" Message:"+smsResponseDTO.getMessage());
	}

}